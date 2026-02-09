
CREATE OR REPLACE FUNCTION calculate_recruitment_metrics(
    p_start_date DATE, 
    p_end_date DATE, 
    p_job_posting_id BIGINT DEFAULT NULL
)
RETURNS recruitment_metrics_type
AS $$
DECLARE
    v_result recruitment_metrics_type;
    v_total_applications BIGINT;
    v_total_hired BIGINT;
    v_average_time_to_hire NUMERIC;
    v_offer_rejection_percentage NUMERIC;
    v_invitation_rejection_ratio NUMERIC;
    v_start_datetime TIMESTAMP WITH TIME ZONE;
    v_end_datetime TIMESTAMP WITH TIME ZONE;
BEGIN
    v_start_datetime := p_start_date::timestamp with time zone;
    v_end_datetime := p_end_date::timestamp with time zone + interval '23:59:59';
    
    SELECT COUNT(*)
    INTO v_total_applications
    FROM applications a
    WHERE a.applied_at >= v_start_datetime 
      AND a.applied_at <= v_end_datetime
      AND (p_job_posting_id IS NULL OR a.job_posting_id = p_job_posting_id);
    
    SELECT COUNT(*)
    INTO v_total_hired
    FROM applications a
    WHERE a.applied_at >= v_start_datetime 
      AND a.applied_at <= v_end_datetime
      AND a.application_status = 'HIRED'
      AND (p_job_posting_id IS NULL OR a.job_posting_id = p_job_posting_id);
    
    SELECT COALESCE(
        AVG(final_times.time_to_hire), 0
    )
    INTO v_average_time_to_hire
    FROM (
        SELECT 
            a.id,
            EXTRACT(EPOCH FROM (MAX(ash.exited_at) - a.applied_at)) / 86400 as time_to_hire
        FROM applications a
        JOIN application_status_history ash ON a.id = ash.application_id
        WHERE a.applied_at >= v_start_datetime 
          AND a.applied_at <= v_end_datetime
          AND a.application_status = 'HIRED'
          AND ash.exited_at IS NOT NULL
          AND (p_job_posting_id IS NULL OR a.job_posting_id = p_job_posting_id)
        GROUP BY a.id, a.applied_at
    ) final_times;
    
    SELECT COALESCE(
        (COUNT(CASE WHEN o.offer_status = 'DECLINED' THEN 1 END)::NUMERIC / 
         NULLIF(COUNT(*), 0)) * 100, 0
    )
    INTO v_offer_rejection_percentage
    FROM offers o
    JOIN applications a ON o.application_id = a.id
    WHERE o.created_at >= v_start_datetime 
      AND o.created_at <= v_end_datetime
      AND (p_job_posting_id IS NULL OR a.job_posting_id = p_job_posting_id);
    
    SELECT COALESCE(
        (COUNT(CASE WHEN a.application_status = 'REJECTED' THEN 1 END)::NUMERIC / 
         NULLIF(COUNT(*), 0)) * 100, 0
    )
    INTO v_invitation_rejection_ratio
    FROM applications a
    WHERE a.applied_at >= v_start_datetime 
      AND a.applied_at <= v_end_datetime
      AND (p_job_posting_id IS NULL OR a.job_posting_id = p_job_posting_id);
    
    v_result := (
        v_total_applications,
        v_total_hired,
        v_average_time_to_hire,
        v_offer_rejection_percentage,
        v_invitation_rejection_ratio,
        p_start_date,
        p_end_date
    );
    
    RETURN v_result;
END;
$$ LANGUAGE plpgsql;

