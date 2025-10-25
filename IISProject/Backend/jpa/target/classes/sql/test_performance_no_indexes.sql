-- =====================================================
-- TESTIRANJE PERFORMANSI BEZ INDEKSA
-- =====================================================
-- Ovaj skript testira performanse funkcije calculate_recruitment_metrics
-- bez indeksa da bi se videla razlika

-- =====================================================
-- 1. UKLANJANJE POSTOJEĆIH INDEKSA
-- =====================================================
SELECT 'Uklanjanje postojećih indeksa...' as status;

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
-- 2. PROVERA DA LI SU INDEKSI UKLONJENI
-- =====================================================
SELECT 'Provera uklonjenih indeksa...' as status;

SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename IN ('applications', 'application_status_history', 'offers')
ORDER BY tablename, indexname;

-- =====================================================
-- 3. TESTIRANJE PERFORMANSI BEZ INDEKSA
-- =====================================================
SELECT 'Testiranje performansi bez indeksa...' as status;

-- Uključujemo timing
\timing on

-- Test 1: Ceo period (2024-2025) bez indeksa
SELECT 'TEST 1: Ceo period bez indeksa' as test_name;
SELECT * FROM calculate_recruitment_metrics('2024-01-01'::date, '2025-12-31'::date);

-- Test 2: Konkretan job posting bez indeksa
SELECT 'TEST 2: Konkretan job posting bez indeksa' as test_name;
SELECT * FROM calculate_recruitment_metrics('2024-01-01'::date, '2025-12-31'::date, 1);

-- Test 3: Kraći period (poslednji mesec) bez indeksa
SELECT 'TEST 3: Poslednji mesec bez indeksa' as test_name;
SELECT * FROM calculate_recruitment_metrics('2025-11-01'::date, '2025-11-30'::date);

-- =====================================================
-- 4. ANALIZA POJEDINAČNIH UPITA BEZ INDEKSA
-- =====================================================
SELECT 'TEST 4: Analiza pojedinačnih upita bez indeksa' as test_name;

-- Upit za broj aplikacija
SELECT 'Upit za broj aplikacija bez indeksa:' as upit;
EXPLAIN (ANALYZE, BUFFERS) 
SELECT COUNT(*)
FROM applications a
WHERE a.applied_at >= '2024-01-01'::timestamp with time zone
  AND a.applied_at <= '2025-12-31 23:59:59'::timestamp with time zone;

-- Upit za broj zaposlenih
SELECT 'Upit za broj zaposlenih bez indeksa:' as upit;
EXPLAIN (ANALYZE, BUFFERS)
SELECT COUNT(*)
FROM applications a
WHERE a.applied_at >= '2024-01-01'::timestamp with time zone
  AND a.applied_at <= '2025-12-31 23:59:59'::timestamp with time zone
  AND a.application_status = 'HIRED';

-- Upit za prosečno vreme zapošljavanja
SELECT 'Upit za prosečno vreme zapošljavanja bez indeksa:' as upit;
EXPLAIN (ANALYZE, BUFFERS)
SELECT COALESCE(AVG(final_times.time_to_hire), 0)
FROM (
    SELECT 
        a.id,
        EXTRACT(EPOCH FROM (MAX(ash.exited_at) - a.applied_at)) / 86400 as time_to_hire
    FROM applications a
    JOIN application_status_history ash ON a.id = ash.application_id
    WHERE a.applied_at >= '2024-01-01'::timestamp with time zone
      AND a.applied_at <= '2025-12-31 23:59:59'::timestamp with time zone
      AND a.application_status = 'HIRED'
      AND ash.exited_at IS NOT NULL
    GROUP BY a.id, a.applied_at
) final_times;

-- Upit za procenat odbijanja ponuda
SELECT 'Upit za procenat odbijanja ponuda bez indeksa:' as upit;
EXPLAIN (ANALYZE, BUFFERS)
SELECT COALESCE(
    (COUNT(CASE WHEN o.offer_status = 'DECLINED' THEN 1 END)::NUMERIC / 
     NULLIF(COUNT(*), 0)) * 100, 0
)
FROM offers o
JOIN applications a ON o.application_id = a.id
WHERE o.created_at >= '2024-01-01'::timestamp with time zone
  AND o.created_at <= '2025-12-31 23:59:59'::timestamp with time zone;

-- =====================================================
-- 5. STATISTIKE TABELA BEZ INDEKSA
-- =====================================================
SELECT 'Statistike tabela bez indeksa:' as status;

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
-- 6. ZAVRŠETAK TESTIRANJA
-- =====================================================
\timing off

SELECT 'Testiranje bez indeksa završeno!' as status;
SELECT 'Sada možete pokrenuti testiranje sa indeksima.' as sledeći_korak;
