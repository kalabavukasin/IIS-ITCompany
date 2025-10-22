-- ============================================================
-- PL/SQL Functions for Recruitment System
-- ============================================================
-- Autor: Extracted from PostgreSQL Database
-- Datum: 2024
-- Opis: Funkcije za izračunavanje kompleksnih metrika i analizu procesa zapošljavanja
-- ============================================================

-- ============================================================
-- 1. FUNKCIJA ZA SUMARNE PODATKE PO JOB POSTING-U
-- ============================================================

CREATE OR REPLACE FUNCTION calculate_job_posting_summary(p_start_date DATE, p_end_date DATE)
RETURNS TABLE(
    job_posting_id BIGINT, 
    job_posting_name VARCHAR, 
    total_applications BIGINT, 
    hired_count BIGINT, 
    rejection_count BIGINT, 
    average_processing_days NUMERIC, 
    success_rate NUMERIC
)
AS $$
DECLARE
    v_start_datetime TIMESTAMP WITH TIME ZONE;
    v_end_datetime TIMESTAMP WITH TIME ZONE;
BEGIN
    v_start_datetime := p_start_date::timestamp with time zone;
    v_end_datetime := p_end_date::timestamp with time zone + interval '23:59:59';
    
    RETURN QUERY
    WITH job_stats AS (
        SELECT 
            jp.id as job_posting_id,
            r.name as job_posting_name,
            COUNT(a.id) as total_applications,
            COUNT(CASE WHEN a.application_status = 'HIRED' THEN 1 END) as hired_count,
            COUNT(CASE WHEN a.application_status = 'REJECTED' THEN 1 END) as rejection_count,
            AVG(CASE 
                WHEN a.application_status = 'HIRED' 
                THEN EXTRACT(EPOCH FROM (a.applied_at - a.applied_at))/86400 
            END) as avg_days
        FROM job_postings jp
        JOIN requestions r ON jp.requestion_id = r.id
        LEFT JOIN applications a ON jp.id = a.job_posting_id
        WHERE a.applied_at >= v_start_datetime 
          AND a.applied_at <= v_end_datetime
        GROUP BY jp.id, r.name
    )
    SELECT 
        js.job_posting_id,
        js.job_posting_name,
        js.total_applications,
        js.hired_count,
        js.rejection_count,
        COALESCE(js.avg_days, 0) as average_processing_days,
        CASE 
            WHEN js.total_applications > 0 
            THEN (js.hired_count::NUMERIC / js.total_applications) * 100 
            ELSE 0 
        END as success_rate
    FROM job_stats js
    ORDER BY js.total_applications DESC;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION calculate_job_posting_summary(DATE, DATE) IS 
'Vraća sumarne metrike za sve job posting-e u zadatom periodu';


-- ============================================================
-- 2. GLAVNA FUNKCIJA ZA RECRUITMENT METRICS
-- ============================================================

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
    -- Konvertujemo datume u timestamp sa timezone
    v_start_datetime := p_start_date::timestamp with time zone;
    v_end_datetime := p_end_date::timestamp with time zone + interval '23:59:59';
    
    -- Ukupan broj prijava u periodu
    SELECT COUNT(*)
    INTO v_total_applications
    FROM applications a
    WHERE a.applied_at >= v_start_datetime 
      AND a.applied_at <= v_end_datetime
      AND (p_job_posting_id IS NULL OR a.job_posting_id = p_job_posting_id);
    
    -- Ukupan broj zaposlenih u periodu
    SELECT COUNT(*)
    INTO v_total_hired
    FROM applications a
    WHERE a.applied_at >= v_start_datetime 
      AND a.applied_at <= v_end_datetime
      AND a.application_status = 'HIRED'
      AND (p_job_posting_id IS NULL OR a.job_posting_id = p_job_posting_id);
    
    -- Prosečno vreme do zaposlenja (u danima)
    -- Računa se od trenutka prijave (applied_at) do završetka finalne faze (MAX exited_at)
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
    
    -- Procent odbijanja ponuda
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
    
    -- Odnos pozivani/odbijeni
    SELECT COALESCE(
        (COUNT(CASE WHEN a.application_status = 'REJECTED' THEN 1 END)::NUMERIC / 
         NULLIF(COUNT(*), 0)) * 100, 0
    )
    INTO v_invitation_rejection_ratio
    FROM applications a
    WHERE a.applied_at >= v_start_datetime 
      AND a.applied_at <= v_end_datetime
      AND (p_job_posting_id IS NULL OR a.job_posting_id = p_job_posting_id);
    
    -- Kreiranje rezultata
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

COMMENT ON FUNCTION calculate_recruitment_metrics(DATE, DATE, BIGINT) IS 
'Izračunava kompleksne metrike performansi procesa zapošljavanja za zadati period';


-- ============================================================
-- 3. FUNKCIJA ZA ANALIZU PERFORMANSI PO FAZAMA
-- ============================================================

CREATE OR REPLACE FUNCTION calculate_stage_performance(p_start_date DATE, p_end_date DATE)
RETURNS TABLE(
    stage_name VARCHAR, 
    entered_count BIGINT, 
    completed_count BIGINT, 
    conversion_rate NUMERIC, 
    average_time_in_days NUMERIC, 
    total_applications_in_stage BIGINT
)
AS $$
DECLARE
    v_start_datetime TIMESTAMP WITH TIME ZONE;
    v_end_datetime TIMESTAMP WITH TIME ZONE;
BEGIN
    v_start_datetime := p_start_date::timestamp with time zone;
    v_end_datetime := p_end_date::timestamp with time zone + interval '23:59:59';
    
    RETURN QUERY
    WITH stage_stats AS (
        SELECT 
            CASE 
                WHEN ws.name = 'Intervju' AND i.interview_type IS NOT NULL 
                THEN ws.name || ' (' || i.interview_type || ')'
                WHEN ws.name = 'Intervju' AND i.interview_type IS NULL
                THEN ws.name || ' (Workflow #' || ws.workflow_id || ')'
                ELSE ws.name
            END as stage_name,
            COUNT(*) as entered_count,
            COUNT(CASE WHEN ash.exited_at IS NOT NULL THEN 1 END) as completed_count,
            AVG(CASE 
                WHEN ash.exited_at IS NOT NULL 
                THEN EXTRACT(EPOCH FROM (ash.exited_at - ash.entered_at))/86400 
            END) as avg_days,
            COUNT(DISTINCT ash.application_id) as total_apps
        FROM application_status_history ash
        JOIN workflow_stages ws ON ash.stage_id = ws.id
        LEFT JOIN interviews i ON ash.application_id = i.application_id 
            AND ws.name = 'Intervju'
        WHERE ash.entered_at >= v_start_datetime 
          AND ash.entered_at <= v_end_datetime
        GROUP BY ws.name, ws.workflow_id, i.interview_type
    )
    SELECT 
        ss.stage_name,
        ss.entered_count,
        ss.completed_count,
        CASE 
            WHEN ss.entered_count > 0 
            THEN (ss.completed_count::NUMERIC / ss.entered_count) * 100 
            ELSE 0 
        END as conversion_rate,
        COALESCE(ss.avg_days, 0) as average_time_in_days,
        ss.total_apps as total_applications_in_stage
    FROM stage_stats ss
    ORDER BY ss.entered_count DESC;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION calculate_stage_performance(DATE, DATE) IS 
'Analizira performanse po fazama workflow-a, vraća metrike po svakoj fazi';
