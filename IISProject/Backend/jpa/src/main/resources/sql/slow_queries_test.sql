-- =====================================================
-- SKRIPT ZA KREIRANJE SPORIH UPITA ZA TESTIRANJE OPTIMIZACIJE
-- =====================================================
-- Ovaj skript sadrži upite koji će se izvršavati duže i omogućiti testiranje optimizacije

-- =====================================================
-- 1. UKLANJANJE POSTOJEĆIH INDEKSA ZA TESTIRANJE
-- =====================================================
SELECT 'Uklanjanje postojećih indeksa za testiranje...' as status;

-- Uklanjamo sve postojeće indekse
DROP INDEX IF EXISTS idx_status_history_application;
DROP INDEX IF EXISTS idx_status_history_entered;
DROP INDEX IF EXISTS idx_status_history_exited;
DROP INDEX IF EXISTS idx_applications_applied_at;
DROP INDEX IF EXISTS idx_applications_status_date;
DROP INDEX IF EXISTS idx_offers_application;
DROP INDEX IF EXISTS idx_offers_created;

-- Uklanjamo i test indekse ako postoje
DROP INDEX IF EXISTS idx_applications_date_status_job;
DROP INDEX IF EXISTS idx_status_history_app_exit;
DROP INDEX IF EXISTS idx_offers_date_status;
DROP INDEX IF EXISTS idx_offers_application_id;
DROP INDEX IF EXISTS idx_status_history_entered_at;
DROP INDEX IF EXISTS idx_applications_status_date_job;

-- =====================================================
-- 2. PROVERA TRENUTNOG STANJA BAZE
-- =====================================================
SELECT 'Provera trenutnog stanja baze...' as status;

-- Broj zapisa po tabeli
SELECT 
    'Applications' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('applications')) as table_size
FROM applications
UNION ALL
SELECT 
    'Application Status History' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('application_status_history')) as table_size
FROM application_status_history
UNION ALL
SELECT 
    'Offers' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('offers')) as table_size
FROM offers;

-- =====================================================
-- 3. SPORI UPITI ZA TESTIRANJE OPTIMIZACIJE
-- =====================================================
SELECT 'Pokretanje sporih upita...' as status;

-- Uključujemo timing
\timing on

-- =====================================================
-- UPIT 1: Kompleksan JOIN sa više tabela bez indeksa - USPOREN
-- =====================================================
SELECT 'UPIT 1: Kompleksan JOIN sa više tabela - USPOREN' as test_name;

EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT 
    a.id as application_id,
    a.applied_at,
    a.application_status,
    cp.first_name,
    cp.last_name,
    cp.email,
    jp.id as job_posting_id,
    wd.name as workflow_name,
    ws.name as current_stage_name,
    COUNT(ash.id) as status_history_count,
    MAX(ash.entered_at) as last_status_change,
    o.offer_status,
    o.created_at as offer_created_at,
    -- Dodajemo kompleksne kalkulacije
    EXTRACT(EPOCH FROM (a.applied_at - '2024-01-01'::timestamp)) / 86400 as days_since_start,
    CASE 
        WHEN a.application_status = 'HIRED' THEN 'SUCCESS'
        WHEN a.application_status = 'REJECTED' THEN 'FAILED'
        ELSE 'PENDING'
    END as process_status,
    -- Kompleksna agregacija
    COUNT(DISTINCT ash.stage_id) as unique_stages_visited,
    STRING_AGG(DISTINCT ash.comment, ' | ') as all_comments
FROM applications a
LEFT JOIN candidate_profiles cp ON a.candidate_id = cp.id
LEFT JOIN job_postings jp ON a.job_posting_id = jp.id
LEFT JOIN workflow_defs wd ON a.workflow_id = wd.id
LEFT JOIN workflow_stages ws ON a.current_stage_id = ws.id
LEFT JOIN application_status_history ash ON a.id = ash.application_id
LEFT JOIN offers o ON a.id = o.application_id
-- Uklanjamo LIMIT da se izvršava na svim podacima
WHERE a.applied_at >= '2024-01-01'::timestamp
  AND a.applied_at <= '2025-12-31'::timestamp
GROUP BY a.id, a.applied_at, a.application_status, cp.first_name, cp.last_name, 
         cp.email, jp.id, wd.name, ws.name, o.offer_status, o.created_at
ORDER BY a.applied_at DESC;

-- =====================================================
-- UPIT 2: Kompleksna agregacija sa subquery-jima
-- =====================================================
SELECT 'UPIT 2: Kompleksna agregacija sa subquery-jima' as test_name;

EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT 
    DATE_TRUNC('month', a.applied_at) as month,
    COUNT(*) as total_applications,
    COUNT(CASE WHEN a.application_status = 'HIRED' THEN 1 END) as hired_count,
    COUNT(CASE WHEN a.application_status = 'REJECTED' THEN 1 END) as rejected_count,
    COUNT(CASE WHEN a.application_status = 'WITHDRAWN' THEN 1 END) as withdrawn_count,
    ROUND(
        COUNT(CASE WHEN a.application_status = 'HIRED' THEN 1 END)::NUMERIC / 
        NULLIF(COUNT(*), 0) * 100, 2
    ) as hire_rate_percentage,
    AVG(
        CASE 
            WHEN a.application_status = 'HIRED' AND ash.exited_at IS NOT NULL 
            THEN EXTRACT(EPOCH FROM (ash.exited_at - a.applied_at)) / 86400
        END
    ) as avg_time_to_hire_days,
    COUNT(DISTINCT a.candidate_id) as unique_candidates,
    COUNT(DISTINCT a.job_posting_id) as unique_job_postings
FROM applications a
LEFT JOIN application_status_history ash ON a.id = ash.application_id 
    AND ash.stage_id = (SELECT id FROM workflow_stages WHERE name LIKE '%Hired%' LIMIT 1)
WHERE a.applied_at >= '2024-01-01'::timestamp
  AND a.applied_at <= '2025-12-31'::timestamp
GROUP BY DATE_TRUNC('month', a.applied_at)
ORDER BY month;

-- =====================================================
-- UPIT 3: Window funkcije sa kompleksnim kalkulacijama
-- =====================================================
SELECT 'UPIT 3: Window funkcije sa kompleksnim kalkulacijama' as test_name;

EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
WITH application_metrics AS (
    SELECT 
        a.id,
        a.applied_at,
        a.application_status,
        a.job_posting_id,
        cp.first_name,
        cp.last_name,
        COUNT(ash.id) as status_changes,
        MAX(ash.entered_at) as last_status_change,
        MIN(ash.entered_at) as first_status_change,
        CASE 
            WHEN a.application_status = 'HIRED' AND MAX(ash.exited_at) IS NOT NULL
            THEN EXTRACT(EPOCH FROM (MAX(ash.exited_at) - a.applied_at)) / 86400
        END as time_to_hire_days,
        COUNT(o.id) as offer_count,
        MAX(o.created_at) as last_offer_date
    FROM applications a
    LEFT JOIN candidate_profiles cp ON a.candidate_id = cp.id
    LEFT JOIN application_status_history ash ON a.id = ash.application_id
    LEFT JOIN offers o ON a.id = o.application_id
    WHERE a.applied_at >= '2024-01-01'::timestamp
      AND a.applied_at <= '2025-12-31'::timestamp
    GROUP BY a.id, a.applied_at, a.application_status, a.job_posting_id, 
             cp.first_name, cp.last_name
),
ranked_applications AS (
    SELECT 
        *,
        ROW_NUMBER() OVER (PARTITION BY job_posting_id ORDER BY applied_at) as application_sequence,
        RANK() OVER (PARTITION BY job_posting_id ORDER BY time_to_hire_days) as hire_speed_rank,
        PERCENT_RANK() OVER (PARTITION BY application_status ORDER BY time_to_hire_days) as time_percentile,
        LAG(applied_at) OVER (PARTITION BY job_posting_id ORDER BY applied_at) as prev_application_date,
        LEAD(applied_at) OVER (PARTITION BY job_posting_id ORDER BY applied_at) as next_application_date
    FROM application_metrics
)
SELECT 
    job_posting_id,
    COUNT(*) as total_applications,
    AVG(status_changes) as avg_status_changes,
    AVG(time_to_hire_days) as avg_time_to_hire,
    STDDEV(time_to_hire_days) as stddev_time_to_hire,
    MIN(time_to_hire_days) as min_time_to_hire,
    MAX(time_to_hire_days) as max_time_to_hire,
    COUNT(CASE WHEN application_status = 'HIRED' THEN 1 END) as hired_count,
    AVG(CASE WHEN prev_application_date IS NOT NULL 
        THEN EXTRACT(EPOCH FROM (applied_at - prev_application_date)) / 86400 END) as avg_days_between_applications
FROM ranked_applications
GROUP BY job_posting_id
HAVING COUNT(*) > 5  -- Samo job postings sa više od 5 aplikacija
ORDER BY total_applications DESC;

-- =====================================================
-- UPIT 4: Kompleksan upit sa EXISTS i NOT EXISTS
-- =====================================================
SELECT 'UPIT 4: Kompleksan upit sa EXISTS i NOT EXISTS' as test_name;

EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT 
    a.id as application_id,
    a.applied_at,
    a.application_status,
    cp.first_name,
    cp.last_name,
    jp.id as job_posting_id,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM application_status_history ash2 
            WHERE ash2.application_id = a.id 
              AND ash2.stage_id IN (SELECT id FROM workflow_stages WHERE name LIKE '%Interview%')
        ) THEN 'Had Interview'
        ELSE 'No Interview'
    END as interview_status,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM offers o 
            WHERE o.application_id = a.id 
              AND o.offer_status IN ('SENT', 'ACCEPTED', 'DECLINED')
        ) THEN 'Had Offer'
        ELSE 'No Offer'
    END as offer_status,
    CASE 
        WHEN NOT EXISTS (
            SELECT 1 FROM application_status_history ash3 
            WHERE ash3.application_id = a.id 
              AND ash3.exited_at IS NOT NULL
        ) THEN 'Still Active'
        ELSE 'Completed Process'
    END as process_status
FROM applications a
LEFT JOIN candidate_profiles cp ON a.candidate_id = cp.id
LEFT JOIN job_postings jp ON a.job_posting_id = jp.id
WHERE a.applied_at >= '2024-01-01'::timestamp
  AND a.applied_at <= '2025-12-31'::timestamp
  AND a.application_status IN ('ACTIVE', 'HIRED', 'REJECTED')
ORDER BY a.applied_at DESC
LIMIT 2000;

-- =====================================================
-- UPIT 5: Kompleksan upit sa string operacijama i pattern matching
-- =====================================================
SELECT 'UPIT 5: Kompleksan upit sa string operacijama' as test_name;

EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT 
    SUBSTRING(cp.email FROM '@(.+)$') as email_domain,
    COUNT(*) as applications_count,
    COUNT(CASE WHEN a.application_status = 'HIRED' THEN 1 END) as hired_count,
    ROUND(
        COUNT(CASE WHEN a.application_status = 'HIRED' THEN 1 END)::NUMERIC / 
        NULLIF(COUNT(*), 0) * 100, 2
    ) as hire_rate,
    AVG(
        CASE 
            WHEN a.application_status = 'HIRED' AND ash.exited_at IS NOT NULL 
            THEN EXTRACT(EPOCH FROM (ash.exited_at - a.applied_at)) / 86400
        END
    ) as avg_time_to_hire,
    STRING_AGG(DISTINCT a.application_status, ', ' ORDER BY a.application_status) as all_statuses,
    COUNT(DISTINCT a.job_posting_id) as unique_jobs_applied
FROM applications a
LEFT JOIN candidate_profiles cp ON a.candidate_id = cp.id
LEFT JOIN application_status_history ash ON a.id = ash.application_id 
    AND ash.stage_id = (SELECT id FROM workflow_stages WHERE name LIKE '%Hired%' LIMIT 1)
WHERE a.applied_at >= '2024-01-01'::timestamp
  AND a.applied_at <= '2025-12-31'::timestamp
  AND cp.email IS NOT NULL
  AND cp.email LIKE '%@%'
GROUP BY SUBSTRING(cp.email FROM '@(.+)$')
HAVING COUNT(*) >= 2  -- Samo domeni sa 2+ aplikacije
ORDER BY applications_count DESC;

-- =====================================================
-- UPIT 6: Originalna funkcija calculate_recruitment_metrics
-- =====================================================
SELECT 'UPIT 6: Originalna funkcija calculate_recruitment_metrics' as test_name;

EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT * FROM calculate_recruitment_metrics('2024-01-01'::date, '2025-12-31'::date);

-- =====================================================
-- UPIT 6.1: EKSTREMNO SPORI UPIT - Cross Join sa svim podacima
-- =====================================================
SELECT 'UPIT 6.1: EKSTREMNO SPORI UPIT - Cross Join' as test_name;

EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
WITH application_stats AS (
    SELECT 
        a.id,
        a.applied_at,
        a.application_status,
        COUNT(ash.id) as status_count,
        MAX(ash.entered_at) as last_status
    FROM applications a
    LEFT JOIN application_status_history ash ON a.id = ash.application_id
    WHERE a.applied_at >= '2024-01-01'::timestamp
      AND a.applied_at <= '2025-12-31'::timestamp
    GROUP BY a.id, a.applied_at, a.application_status
),
candidate_stats AS (
    SELECT 
        cp.id,
        cp.first_name,
        cp.last_name,
        COUNT(a.id) as total_applications,
        COUNT(CASE WHEN a.application_status = 'HIRED' THEN 1 END) as hired_count
    FROM candidate_profiles cp
    LEFT JOIN applications a ON cp.id = a.candidate_id
    WHERE a.applied_at >= '2024-01-01'::timestamp
      AND a.applied_at <= '2025-12-31'::timestamp
    GROUP BY cp.id, cp.first_name, cp.last_name
)
SELECT 
    as1.id as app1_id,
    as1.applied_at as app1_date,
    as1.application_status as app1_status,
    as1.status_count as app1_status_count,
    as2.id as app2_id,
    as2.applied_at as app2_date,
    as2.application_status as app2_status,
    as2.status_count as app2_status_count,
    cs1.first_name as candidate1_name,
    cs1.total_applications as candidate1_total_apps,
    cs2.first_name as candidate2_name,
    cs2.total_applications as candidate2_total_apps,
    -- Kompleksne kalkulacije
    EXTRACT(EPOCH FROM (as1.applied_at - as2.applied_at)) / 86400 as days_difference,
    CASE 
        WHEN as1.status_count > as2.status_count THEN 'App1 more complex'
        WHEN as1.status_count < as2.status_count THEN 'App2 more complex'
        ELSE 'Equal complexity'
    END as complexity_comparison
FROM application_stats as1
CROSS JOIN application_stats as2
LEFT JOIN candidate_stats cs1 ON as1.id = cs1.id
LEFT JOIN candidate_stats cs2 ON as2.id = cs2.id
WHERE as1.id != as2.id
  AND as1.id < as2.id  -- Da izbegnemo duplikate
ORDER BY days_difference DESC
LIMIT 5000;  -- Ograničavamo na 5000 redova da ne bude previše

-- =====================================================
-- UPIT 6.2: EKSTREMNO SPORI UPIT - Nested Subqueries
-- =====================================================
SELECT 'UPIT 6.2: EKSTREMNO SPORI UPIT - Nested Subqueries' as test_name;

EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT 
    a.id,
    a.applied_at,
    a.application_status,
    -- Kompleksni nested subquery
    (SELECT COUNT(*) 
     FROM application_status_history ash2 
     WHERE ash2.application_id = a.id 
       AND ash2.entered_at >= a.applied_at
       AND ash2.entered_at <= a.applied_at + interval '30 days'
    ) as status_changes_in_first_month,
    
    -- Još jedan nested subquery
    (SELECT AVG(EXTRACT(EPOCH FROM (ash3.exited_at - ash3.entered_at)) / 86400)
     FROM application_status_history ash3 
     WHERE ash3.application_id = a.id 
       AND ash3.exited_at IS NOT NULL
    ) as avg_days_per_stage,
    
    -- Još jedan nested subquery
    (SELECT COUNT(DISTINCT o.offer_status)
     FROM offers o 
     WHERE o.application_id = a.id
    ) as unique_offer_statuses,
    
    -- Još jedan nested subquery
    (SELECT COUNT(*)
     FROM applications a2 
     WHERE a2.candidate_id = a.candidate_id 
       AND a2.applied_at < a.applied_at
    ) as previous_applications_by_candidate,
    
    -- Još jedan nested subquery
    (SELECT COUNT(*)
     FROM applications a3 
     WHERE a3.job_posting_id = a.job_posting_id 
       AND a3.applied_at < a.applied_at
    ) as previous_applications_for_job,
    
    -- Još jedan nested subquery
    (SELECT COUNT(*)
     FROM applications a4 
     WHERE a4.job_posting_id = a.job_posting_id 
       AND a4.application_status = 'HIRED'
       AND a4.applied_at < a.applied_at
    ) as previous_hires_for_job,
    
    -- Još jedan nested subquery
    (SELECT AVG(EXTRACT(EPOCH FROM (a5.applied_at - a.applied_at)) / 86400)
     FROM applications a5 
     WHERE a5.candidate_id = a.candidate_id 
       AND a5.applied_at > a.applied_at
       AND a5.applied_at <= a.applied_at + interval '90 days'
    ) as avg_days_to_next_application,
    
    -- Još jedan nested subquery
    (SELECT COUNT(*)
     FROM application_status_history ash4 
     WHERE ash4.application_id IN (
         SELECT a6.id 
         FROM applications a6 
         WHERE a6.candidate_id = a.candidate_id
     )
    ) as total_status_changes_for_candidate

FROM applications a
WHERE a.applied_at >= '2024-01-01'::timestamp
  AND a.applied_at <= '2025-12-31'::timestamp
ORDER BY a.applied_at DESC
LIMIT 1000;

-- =====================================================
-- UPIT 7: Kompleksan upit sa više JOIN-ova i agregacijom
-- =====================================================
SELECT 'UPIT 7: Kompleksan upit sa više JOIN-ova' as test_name;

EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)
SELECT 
    wd.name as workflow_name,
    ws.name as stage_name,
    COUNT(a.id) as applications_in_stage,
    COUNT(CASE WHEN a.application_status = 'HIRED' THEN 1 END) as hired_from_stage,
    AVG(
        CASE 
            WHEN ash.exited_at IS NOT NULL 
            THEN EXTRACT(EPOCH FROM (ash.exited_at - ash.entered_at)) / 86400
        END
    ) as avg_days_in_stage,
    COUNT(DISTINCT a.candidate_id) as unique_candidates,
    COUNT(DISTINCT a.job_posting_id) as unique_job_postings,
    STRING_AGG(DISTINCT a.application_status, ', ') as statuses_in_stage
FROM applications a
JOIN workflow_defs wd ON a.workflow_id = wd.id
JOIN workflow_stages ws ON a.current_stage_id = ws.id
LEFT JOIN application_status_history ash ON a.id = ash.application_id 
    AND ash.stage_id = ws.id
WHERE a.applied_at >= '2024-01-01'::timestamp
  AND a.applied_at <= '2025-12-31'::timestamp
GROUP BY wd.name, ws.name, ws.id
ORDER BY applications_in_stage DESC;

-- =====================================================
-- 4. ZAVRŠETAK TESTIRANJA
-- =====================================================
\timing off

SELECT 'Spori upiti završeni!' as status;
SELECT 'Sada možete dodati indekse i testirati optimizaciju.' as sledeći_korak;
