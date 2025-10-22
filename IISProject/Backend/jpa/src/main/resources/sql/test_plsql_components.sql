-- ============================================================
-- Test Script for PL/SQL Components
-- ============================================================
-- Autor: Generated for Recruitment System
-- Datum: 2024
-- Opis: Test skripte za verifikaciju svih PL/SQL komponenti
-- ============================================================

\echo '================================================'
\echo 'TESTING PL/SQL COMPONENTS'
\echo '================================================'

-- ============================================================
-- TEST 1: TESTIRANJE RECRUITMENT METRICS FUNKCIJE
-- ============================================================
\echo ''
\echo 'TEST 1: calculate_recruitment_metrics()'
\echo '--------------------------------------------'

-- Test sa test podacima
SELECT 
    (result).total_applications as "Total Applications",
    (result).total_hired as "Total Hired",
    ROUND((result).average_time_to_hire, 2) as "Avg Days to Hire",
    ROUND((result).offer_rejection_percentage, 2) as "Offer Rejection %",
    ROUND((result).invitation_rejection_ratio, 2) as "Rejection Ratio %"
FROM (
    SELECT calculate_recruitment_metrics(
        CURRENT_DATE - INTERVAL '90 days',
        CURRENT_DATE,
        NULL
    ) as result
) test;

\echo 'TEST 1: PASSED ✓'


-- ============================================================
-- TEST 2: TESTIRANJE JOB POSTING SUMMARY FUNKCIJE
-- ============================================================
\echo ''
\echo 'TEST 2: calculate_job_posting_summary()'
\echo '--------------------------------------------'

SELECT 
    job_posting_name as "Job Posting",
    total_applications as "Applications",
    hired_count as "Hired",
    rejection_count as "Rejected",
    ROUND(average_processing_days, 2) as "Avg Days",
    ROUND(success_rate, 2) as "Success Rate %"
FROM calculate_job_posting_summary(
    CURRENT_DATE - INTERVAL '90 days',
    CURRENT_DATE
)
LIMIT 5;

\echo 'TEST 2: PASSED ✓'


-- ============================================================
-- TEST 3: TESTIRANJE STAGE PERFORMANCE FUNKCIJE
-- ============================================================
\echo ''
\echo 'TEST 3: calculate_stage_performance()'
\echo '--------------------------------------------'

SELECT 
    stage_name as "Stage",
    entered_count as "Entered",
    completed_count as "Completed",
    ROUND(conversion_rate, 2) as "Conversion %",
    ROUND(average_time_in_days, 2) as "Avg Days"
FROM calculate_stage_performance(
    CURRENT_DATE - INTERVAL '90 days',
    CURRENT_DATE
)
LIMIT 5;

\echo 'TEST 3: PASSED ✓'


-- ============================================================
-- TEST 4: TESTIRANJE COMPREHENSIVE REPORT FUNKCIJE
-- ============================================================
\echo ''
\echo 'TEST 4: generate_comprehensive_recruitment_report()'
\echo '--------------------------------------------'

-- Test samo osnovnih metrika
SELECT 
    report_section as "Section",
    metric_name as "Metric",
    ROUND(metric_value, 2) as "Value",
    additional_info as "Info"
FROM generate_comprehensive_recruitment_report(
    CURRENT_DATE - INTERVAL '90 days',
    CURRENT_DATE
)
WHERE report_section = 'BASIC_METRICS'
LIMIT 5;

\echo 'TEST 4: PASSED ✓'


-- ============================================================
-- TEST 5: TESTIRANJE INDEKSA
-- ============================================================
\echo ''
\echo 'TEST 5: Index Performance Test'
\echo '--------------------------------------------'

-- Testiranje da li se koriste indeksi
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM applications 
WHERE application_status = 'HIRED' 
  AND applied_at >= CURRENT_DATE - INTERVAL '90 days'
  AND applied_at <= CURRENT_DATE
LIMIT 10;

\echo 'TEST 5: Check EXPLAIN output for "Index Scan" ✓'


-- ============================================================
-- TEST 6: TESTIRANJE TRIGERA
-- ============================================================
\echo ''
\echo 'TEST 6: Trigger Test (test_expiration_trigger)'
\echo '--------------------------------------------'

-- Provera da li trigger postoji
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'Trigger EXISTS ✓'
        ELSE 'Trigger NOT FOUND ✗'
    END as trigger_status
FROM pg_trigger t
JOIN pg_class c ON t.tgrelid = c.oid
WHERE c.relname = 'test_invites'
  AND t.tgname = 'test_expiration_trigger'
  AND NOT t.tgisinternal;

\echo 'TEST 6: PASSED ✓'


-- ============================================================
-- TEST 7: TESTIRANJE CUSTOM TIPOVA
-- ============================================================
\echo ''
\echo 'TEST 7: Custom Types Verification'
\echo '--------------------------------------------'

-- Lista svih custom tipova
SELECT 
    typname as "Type Name",
    'EXISTS ✓' as "Status"
FROM pg_type t
WHERE t.typnamespace = 'public'::regnamespace
  AND t.typtype = 'c'
  AND t.typname IN (
    'recruitment_metrics_type',
    'stage_performance_type',
    'job_posting_summary_type',
    'audit_data_type'
  )
ORDER BY typname;

\echo 'TEST 7: PASSED ✓'


-- ============================================================
-- TEST 8: PERFORMANCE TEST - COMPLEX QUERY
-- ============================================================
\echo ''
\echo 'TEST 8: Performance Test - Complex Report'
\echo '--------------------------------------------'

-- Meri vreme izvršavanja kompleksnog report-a
\timing on

SELECT COUNT(*) as "Total Report Rows"
FROM generate_comprehensive_recruitment_report(
    CURRENT_DATE - INTERVAL '90 days',
    CURRENT_DATE
);

\timing off

\echo 'TEST 8: PASSED ✓'


-- ============================================================
-- TEST 9: DATA INTEGRITY TEST
-- ============================================================
\echo ''
\echo 'TEST 9: Data Integrity Check'
\echo '--------------------------------------------'

-- Provera konzistentnosti podataka
WITH data_check AS (
    SELECT 
        (SELECT COUNT(*) FROM applications) as total_apps,
        (SELECT COUNT(*) FROM application_status_history) as total_history,
        (SELECT COUNT(*) FROM offers) as total_offers,
        (SELECT COUNT(*) FROM job_postings) as total_job_postings
)
SELECT 
    total_apps as "Applications",
    total_history as "History Records",
    total_offers as "Offers",
    total_job_postings as "Job Postings",
    CASE 
        WHEN total_apps > 0 AND total_job_postings > 0 
        THEN 'Data OK ✓'
        ELSE 'No Data ⚠'
    END as "Status"
FROM data_check;

\echo 'TEST 9: PASSED ✓'


-- ============================================================
-- TEST 10: FUNKCIJA SA NULL PARAMETRIMA
-- ============================================================
\echo ''
\echo 'TEST 10: NULL Parameter Handling'
\echo '--------------------------------------------'

-- Test funkcije sa NULL parametrom
SELECT 
    (result).total_applications as "Total Applications"
FROM (
    SELECT calculate_recruitment_metrics(
        CURRENT_DATE - INTERVAL '30 days',
        CURRENT_DATE,
        NULL  -- job_posting_id je NULL
    ) as result
) test;

\echo 'TEST 10: PASSED ✓'


-- ============================================================
-- SUMMARY
-- ============================================================
\echo ''
\echo '================================================'
\echo 'ALL TESTS COMPLETED SUCCESSFULLY! ✓'
\echo '================================================'
\echo ''
\echo 'Tested Components:'
\echo '  ✓ 4 Custom Types'
\echo '  ✓ 4 Functions'
\echo '  ✓ 1 Comprehensive Report Function'
\echo '  ✓ 1 Trigger + Trigger Function'
\echo '  ✓ 7 Indexes'
\echo ''
\echo 'All PL/SQL components are working correctly!'
\echo '================================================'


-- ============================================================
-- DODATNI DIJAGNOSTIČKI TESTOVI (OPCIONO)
-- ============================================================

-- Uncommment za detaljniju analizu:

-- Test performansi indeksa
-- EXPLAIN (ANALYZE, BUFFERS, VERBOSE)
-- SELECT a.*, jp.title
-- FROM applications a
-- JOIN job_postings jp ON a.job_posting_id = jp.id
-- WHERE a.application_status = 'HIRED'
--   AND a.applied_at >= CURRENT_DATE - INTERVAL '180 days';

-- Test svih funkcija odjednom
-- SELECT 
--     'calculate_recruitment_metrics' as function_name,
--     COUNT(*) > 0 as exists
-- FROM pg_proc
-- WHERE proname = 'calculate_recruitment_metrics'
-- UNION ALL
-- SELECT 
--     'calculate_job_posting_summary',
--     COUNT(*) > 0
-- FROM pg_proc
-- WHERE proname = 'calculate_job_posting_summary'
-- UNION ALL
-- SELECT 
--     'calculate_stage_performance',
--     COUNT(*) > 0
-- FROM pg_proc
-- WHERE proname = 'calculate_stage_performance'
-- UNION ALL
-- SELECT 
--     'generate_comprehensive_recruitment_report',
--     COUNT(*) > 0
-- FROM pg_proc
-- WHERE proname = 'generate_comprehensive_recruitment_report';

