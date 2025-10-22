-- ============================================================
-- PL/SQL Complex Report Function
-- ============================================================
-- Autor: Extracted from PostgreSQL Database
-- Datum: 2024
-- Opis: Kompleksna funkcija za generisanje detaljnog izvještaja o zapošljavanju
--       Koristi: kursore, WITH klauzule, složene SQL upite, GROUP BY, HAVING
-- ============================================================

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
    v_stage_performance stage_performance_type;
    v_job_posting_summary job_posting_summary_type;
    
    -- Kursor za iteraciju kroz aplikacije
    applications_cursor CURSOR FOR
        SELECT 
            a.id,
            a.application_status,
            a.applied_at,
            jp.id as job_posting_id,
            r.name as job_posting_name,
            u.first_name || ' ' || u.last_name as candidate_name
        FROM applications a
        JOIN job_postings jp ON a.job_posting_id = jp.id
        JOIN requestions r ON jp.requestion_id = r.id
        JOIN candidate_profiles cp ON a.candidate_id = cp.id
        JOIN users u ON cp.user_id = u.id
        WHERE a.applied_at >= v_start_datetime 
          AND a.applied_at <= v_end_datetime
        ORDER BY a.applied_at DESC;
    
    -- Kursor za analizu faza
    stages_cursor CURSOR FOR
        SELECT 
            CASE 
                WHEN ws.name = 'Intervju' AND i.interview_type IS NOT NULL 
                THEN ws.name || ' (' || i.interview_type || ')'
                WHEN ws.name = 'Intervju' AND i.interview_type IS NULL
                THEN ws.name || ' (Workflow #' || ws.workflow_id || ')'
                ELSE ws.name
            END as stage_name,
            COUNT(*) as total_entries,
            AVG(EXTRACT(EPOCH FROM (ash.exited_at - ash.entered_at))/86400) as avg_days,
            COUNT(CASE WHEN ash.exited_at IS NOT NULL THEN 1 END) as completed_count
        FROM application_status_history ash
        JOIN workflow_stages ws ON ash.stage_id = ws.id
        LEFT JOIN interviews i ON ash.application_id = i.application_id 
            AND ws.name = 'Intervju'
        WHERE ash.entered_at >= v_start_datetime 
          AND ash.entered_at <= v_end_datetime
        GROUP BY ws.name, ws.workflow_id, i.interview_type
        ORDER BY total_entries DESC;
    
    v_app_record RECORD;
    v_stage_record RECORD;
    v_bottleneck_stages TEXT[];
    v_total_applications BIGINT := 0;
    v_stuck_applications BIGINT := 0;
    
BEGIN
    v_start_datetime := p_start_date::timestamp with time zone;
    v_end_datetime := p_end_date::timestamp with time zone + interval '23:59:59';
    
    -- ============================================================
    -- SEKCIJA 1: OSNOVNE METRIKE
    -- ============================================================
    -- Koristi funkciju calculate_recruitment_metrics
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
        'Based on completed hiring processes';
    
    RETURN QUERY SELECT 
        'BASIC_METRICS'::VARCHAR(100),
        'Offer Rejection Rate (%)'::VARCHAR(255),
        v_recruitment_metrics.offer_rejection_percentage,
        'Percentage of declined offers';
    
    -- ============================================================
    -- SEKCIJA 2: ANALIZA PERFORMANSI PO FAZAMA
    -- ============================================================
    -- Koristi WITH klauzule za identifikaciju bottleneck-a
    RETURN QUERY
    WITH stage_durations AS (
        SELECT 
            ash.stage_id,
            ws.name as stage_name,
            CASE 
                WHEN ws.name = 'Intervju' AND i.interview_type IS NOT NULL 
                THEN ws.name || ' (' || i.interview_type || ')'
                WHEN ws.name = 'Intervju' AND i.interview_type IS NULL
                THEN ws.name || ' (Workflow #' || ws.workflow_id || ')'
                ELSE ws.name
            END as stage_name_with_type,
            AVG(EXTRACT(EPOCH FROM (ash.exited_at - ash.entered_at))/86400) as avg_days,
            COUNT(*) as total_entries,
            COUNT(CASE WHEN ash.exited_at IS NOT NULL THEN 1 END) as completed_count
        FROM application_status_history ash
        JOIN workflow_stages ws ON ash.stage_id = ws.id
        LEFT JOIN interviews i ON ash.application_id = i.application_id 
            AND ws.name = 'Intervju'
        WHERE ash.entered_at >= v_start_datetime 
          AND ash.entered_at <= v_end_datetime
        GROUP BY ash.stage_id, ws.name, ws.workflow_id, i.interview_type
    ),
    bottleneck_stages AS (
        SELECT 
            stage_name_with_type,
            avg_days,
            total_entries,
            completed_count,
            CASE 
                WHEN avg_days > (SELECT AVG(avg_days) * 1.5 FROM stage_durations WHERE avg_days IS NOT NULL)
                THEN 'BOTTLENECK'
                ELSE 'NORMAL'
            END as status
        FROM stage_durations
        WHERE avg_days IS NOT NULL
    ),
    stage_conversion_rates AS (
        SELECT 
            bs.stage_name_with_type,
            bs.avg_days,
            bs.total_entries,
            bs.completed_count,
            CASE 
                WHEN bs.total_entries > 0 
                THEN (bs.completed_count::NUMERIC / bs.total_entries) * 100 
                ELSE 0 
            END as conversion_rate,
            bs.status
        FROM bottleneck_stages bs
    )
    SELECT 
        'STAGE_ANALYSIS'::VARCHAR(100),
        scr.stage_name_with_type::VARCHAR(255),
        scr.avg_days::NUMERIC,
        'Entries: ' || scr.total_entries || ', Completed: ' || scr.completed_count || 
        ', Conversion: ' || ROUND(scr.conversion_rate, 2) || '%, Status: ' || scr.status
    FROM stage_conversion_rates scr;
    
    -- ============================================================
    -- SEKCIJA 3: ANALIZA PO OGLASIMA ZA POSAO
    -- ============================================================
    -- Koristi složene SQL upite sa GROUP BY i HAVING
    RETURN QUERY
    WITH job_posting_analysis AS (
        SELECT 
            jp.id as job_posting_id,
            r.name as job_posting_name,
            COUNT(a.id) as total_applications,
            COUNT(CASE WHEN a.application_status = 'HIRED' THEN 1 END) as hired_count,
            COUNT(CASE WHEN a.application_status = 'REJECTED' THEN 1 END) as rejected_count,
            COUNT(CASE WHEN a.application_status = 'IN_PROGRESS' THEN 1 END) as in_progress_count,
            AVG(CASE 
                WHEN a.application_status = 'HIRED' 
                THEN EXTRACT(EPOCH FROM (a.applied_at - a.applied_at))/86400 
            END) as avg_processing_days
        FROM job_postings jp
        JOIN requestions r ON jp.requestion_id = r.id
        LEFT JOIN applications a ON jp.id = a.job_posting_id
        WHERE a.applied_at >= v_start_datetime 
          AND a.applied_at <= v_end_datetime
        GROUP BY jp.id, r.name
        HAVING COUNT(a.id) > 0  -- HAVING klauzula: Samo oglasi sa prijavama
    ),
    job_posting_rankings AS (
        SELECT 
            jpa.*,
            CASE 
                WHEN jpa.total_applications > 0 
                THEN (jpa.hired_count::NUMERIC / jpa.total_applications) * 100 
                ELSE 0 
            END as success_rate,
            ROW_NUMBER() OVER (ORDER BY jpa.total_applications DESC) as popularity_rank,
            ROW_NUMBER() OVER (ORDER BY 
                CASE 
                    WHEN jpa.total_applications > 0 
                    THEN (jpa.hired_count::NUMERIC / jpa.total_applications) * 100 
                    ELSE 0 
                END DESC
            ) as success_rank
        FROM job_posting_analysis jpa
    )
    SELECT 
        'JOB_POSTING_ANALYSIS'::VARCHAR(100),
        jpr.job_posting_name::VARCHAR(255),
        jpr.total_applications::NUMERIC,
        'Hired: ' || jpr.hired_count || ', Rejected: ' || jpr.rejected_count || 
        ', In Progress: ' || jpr.in_progress_count || 
        ', Success Rate: ' || ROUND(jpr.success_rate, 2) || '%' ||
        ', Popularity Rank: ' || jpr.popularity_rank ||
        ', Success Rank: ' || jpr.success_rank
    FROM job_posting_rankings jpr;
    
    -- ============================================================
    -- SEKCIJA 4: DETEKCIJA PROBLEMA
    -- ============================================================
    -- Koristi KURSOR za iteraciju kroz aplikacije
    v_total_applications := 0;
    v_stuck_applications := 0;
    
    FOR v_app_record IN applications_cursor LOOP
        v_total_applications := v_total_applications + 1;
        
        -- Provera da li je prijava "zaglavljena" (duže od 30 dana u istoj fazi)
        IF v_app_record.application_status = 'IN_PROGRESS' AND 
           EXTRACT(EPOCH FROM (NOW() - v_app_record.applied_at))/86400 > 30 THEN
            v_stuck_applications := v_stuck_applications + 1;
        END IF;
    END LOOP;
    
    RETURN QUERY SELECT 
        'PROBLEM_DETECTION'::VARCHAR(100),
        'Stuck Applications (>30 days)'::VARCHAR(255),
        v_stuck_applications::NUMERIC,
        'Out of ' || v_total_applications || ' total applications';
    
    -- ============================================================
    -- SEKCIJA 5: ANALIZA PERFORMANSI PO FAZAMA
    -- ============================================================
    -- Koristi KURSOR za analizu faza
    FOR v_stage_record IN stages_cursor LOOP
        RETURN QUERY SELECT 
            'STAGE_PERFORMANCE'::VARCHAR(100),
            v_stage_record.stage_name::VARCHAR(255),
            v_stage_record.avg_days::NUMERIC,
            'Total Entries: ' || v_stage_record.total_entries || 
            ', Completed: ' || v_stage_record.completed_count ||
            ', Completion Rate: ' || ROUND(
                (v_stage_record.completed_count::NUMERIC / NULLIF(v_stage_record.total_entries, 0)) * 100, 2
            ) || '%';
    END LOOP;
    
    -- ============================================================
    -- SEKCIJA 6: SUMARNI PREGLED
    -- ============================================================
    RETURN QUERY SELECT 
        'SUMMARY'::VARCHAR(100),
        'Report Generated At'::VARCHAR(255),
        EXTRACT(EPOCH FROM NOW())::NUMERIC,
        'Comprehensive recruitment analysis completed';
    
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION generate_comprehensive_recruitment_report(DATE, DATE) IS 
'Kompleksna funkcija za izvještaj koja koristi: kursore, WITH klauzule, GROUP BY, HAVING, složene SQL upite';

