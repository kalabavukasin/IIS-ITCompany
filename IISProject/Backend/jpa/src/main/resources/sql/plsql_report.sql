

CREATE OR REPLACE FUNCTION generate_comprehensive_recruitment_report(p_start_date DATE, p_end_date DATE)
RETURNS TABLE(
    report_section VARCHAR, 
    metric_name VARCHAR, 
    metric_value NUMERIC, 
    additional_info TEXT
)
AS $$
DECLARE
    v_start_datetime TIMESTAMP WITH TIME ZONE;
    v_end_datetime TIMESTAMP WITH TIME ZONE;
    v_recruitment_metrics recruitment_metrics_type;
    
    
    stages_cursor CURSOR FOR
        SELECT 
            ws.name as stage_name,
            COUNT(*) as total_entries,
            AVG(EXTRACT(EPOCH FROM (ash.exited_at - ash.entered_at))/86400) as avg_days,
            COUNT(CASE WHEN ash.exited_at IS NOT NULL THEN 1 END) as completed_count
        FROM application_status_history ash
        JOIN workflow_stages ws ON ash.stage_id = ws.id
        LEFT JOIN interviews i ON ash.application_id = i.application_id 
            AND ws.name = 'Intervju'
        WHERE ash.entered_at >= v_start_datetime 
          AND ash.entered_at <= v_end_datetime
        GROUP BY ws.name
        ORDER BY total_entries DESC;
    
    v_stage_record RECORD;
    
BEGIN
    v_start_datetime := p_start_date::timestamp with time zone;
    v_end_datetime := p_end_date::timestamp with time zone + interval '23:59:59';
    
    SELECT * INTO v_recruitment_metrics 
    FROM calculate_recruitment_metrics(p_start_date, p_end_date);
    
    RETURN QUERY SELECT 
        'BASIC_METRICS'::VARCHAR(100),
        'Total Applications'::VARCHAR(255),
        v_recruitment_metrics.total_applications::NUMERIC,
        'Period: ' || p_start_date::TEXT || ' to ' || p_end_date::TEXT;
    
    RETURN QUERY SELECT 
        'BASIC_METRICS'::VARCHAR(100),
        'Total Hired'::VARCHAR(255),
        v_recruitment_metrics.total_hired::NUMERIC,
        'Success Rate: ' || ROUND((v_recruitment_metrics.total_hired::NUMERIC / NULLIF(v_recruitment_metrics.total_applications, 0)) * 100, 2) || '%';
    
    RETURN QUERY SELECT 
        'BASIC_METRICS'::VARCHAR(100),
        'Average Time to Hire (days)'::VARCHAR(255),
        v_recruitment_metrics.average_time_to_hire,
        'From application to final stage completion';
    
    RETURN QUERY SELECT 
        'BASIC_METRICS'::VARCHAR(100),
        'Offer Rejection Rate (%)'::VARCHAR(255),
        v_recruitment_metrics.offer_rejection_percentage,
        'Percentage of declined job offers';
    
    RETURN QUERY SELECT 
        'BASIC_METRICS'::VARCHAR(100),
        'Application Rejection Ratio (%)'::VARCHAR(255),
        v_recruitment_metrics.invitation_rejection_ratio,
        'Percentage of rejected applications';
    
    FOR v_stage_record IN stages_cursor LOOP
        RETURN QUERY SELECT 
            'STAGE_PERFORMANCE'::VARCHAR(100),
            v_stage_record.stage_name::VARCHAR(255),
            COALESCE(v_stage_record.avg_days, 0)::NUMERIC,
            'Entries: ' || v_stage_record.total_entries || 
            ', Completed: ' || v_stage_record.completed_count ||
            ', Conversion Rate: ' || ROUND(
                (v_stage_record.completed_count::NUMERIC / NULLIF(v_stage_record.total_entries, 0)) * 100, 2
            ) || '%';
    END LOOP;
    
    RETURN QUERY
    WITH job_posting_stats AS (
        SELECT 
            jp.id as job_posting_id,
            r.name as job_posting_name,
            COUNT(a.id) as total_applications,
            COUNT(CASE WHEN a.application_status = 'HIRED' THEN 1 END) as hired_count,
            COUNT(CASE WHEN a.application_status = 'REJECTED' THEN 1 END) as rejected_count,
            COUNT(CASE WHEN a.application_status = 'IN_PROGRESS' THEN 1 END) as in_progress_count,
            CASE 
                WHEN COUNT(a.id) > 0 
                THEN (COUNT(CASE WHEN a.application_status = 'HIRED' THEN 1 END)::NUMERIC / COUNT(a.id)) * 100 
                ELSE 0 
            END as success_rate
        FROM job_postings jp
        JOIN requestions r ON jp.requestion_id = r.id
        LEFT JOIN applications a ON jp.id = a.job_posting_id
        WHERE a.applied_at >= v_start_datetime 
          AND a.applied_at <= v_end_datetime
        GROUP BY jp.id, r.name
        HAVING COUNT(a.id) > 0 
        ORDER BY total_applications DESC
    )
    SELECT 
        'JOB_POSTING_ANALYSIS'::VARCHAR(100),
        jps.job_posting_name::VARCHAR(255),
        jps.total_applications::NUMERIC,
        'Hired: ' || jps.hired_count || 
        ', Rejected: ' || jps.rejected_count || 
        ', In Progress: ' || jps.in_progress_count || 
        ', Success Rate: ' || ROUND(jps.success_rate, 2) || '%'
    FROM job_posting_stats jps;
    
    RETURN QUERY
    WITH quick_insights AS (
        SELECT 
            COUNT(*) as total_apps,
            COUNT(CASE WHEN application_status = 'IN_PROGRESS' 
                  AND EXTRACT(EPOCH FROM (NOW() - applied_at))/86400 > 30 THEN 1 END) as stuck_apps,
            COUNT(CASE WHEN application_status = 'ACTIVE' THEN 1 END) as active_apps,
            AVG(EXTRACT(EPOCH FROM (NOW() - applied_at))/86400) as avg_age_days
        FROM applications
        WHERE applied_at >= v_start_datetime 
          AND applied_at <= v_end_datetime
    )
    SELECT 
        'QUICK_INSIGHTS'::VARCHAR(100),
        'Applications Status Overview'::VARCHAR(255),
        qi.total_apps::NUMERIC,
        'Stuck (>30d): ' || qi.stuck_apps || 
        ', Active: ' || qi.active_apps || 
        ', Avg Age: ' || ROUND(qi.avg_age_days, 1) || ' days'
    FROM quick_insights qi;
    
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION generate_comprehensive_recruitment_report(DATE, DATE) IS 
'Kompleksna funkcija za izvještaj - koristi JEDAN kursor, NESTED tabelu za mesečne trendove, WITH klauzule, GROUP BY, HAVING, složene SQL upite sa 3+ tabela';

