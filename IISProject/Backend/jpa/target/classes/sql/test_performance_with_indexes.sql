-- =====================================================
-- TESTIRANJE PERFORMANSI SA INDEKSIMA
-- =====================================================
-- Ovaj skript testira performanse funkcije calculate_recruitment_metrics
-- sa optimizovanim indeksima

-- =====================================================
-- 1. KREIRANJE OPTIMIZOVANIH INDEKSA
-- =====================================================
SELECT 'Kreiranje optimizovanih indeksa...' as status;

-- Indeks za glavni upit po datumu i statusu
CREATE INDEX CONCURRENTLY idx_applications_date_status_job 
ON applications (applied_at, application_status, job_posting_id);

-- Indeks za JOIN sa application_status_history
CREATE INDEX CONCURRENTLY idx_status_history_app_exit 
ON application_status_history (application_id, exited_at);

-- Indeks za offers upit
CREATE INDEX CONCURRENTLY idx_offers_date_status 
ON offers (created_at, offer_status);

-- Indeks za offers JOIN sa applications
CREATE INDEX CONCURRENTLY idx_offers_application_id 
ON offers (application_id);

-- Dodatni indeks za application_status_history entered_at
CREATE INDEX CONCURRENTLY idx_status_history_entered_at 
ON application_status_history (entered_at);

-- Kompozitni indeks za kompleksnije upite
CREATE INDEX CONCURRENTLY idx_applications_status_date_job 
ON applications (application_status, applied_at, job_posting_id);

-- =====================================================
-- 2. PROVERA KREIRANIH INDEKSA
-- =====================================================
SELECT 'Provera kreiranih indeksa...' as status;

SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename IN ('applications', 'application_status_history', 'offers')
ORDER BY tablename, indexname;

-- =====================================================
-- 3. TESTIRANJE PERFORMANSI SA INDEKSIMA
-- =====================================================
SELECT 'Testiranje performansi sa indeksima...' as status;

-- Uključujemo timing
\timing on

-- Test 1: Ceo period (2024-2025) sa indeksima
SELECT 'TEST 1: Ceo period sa indeksima' as test_name;
SELECT * FROM calculate_recruitment_metrics('2024-01-01'::date, '2025-12-31'::date);

-- Test 2: Konkretan job posting sa indeksima
SELECT 'TEST 2: Konkretan job posting sa indeksima' as test_name;
SELECT * FROM calculate_recruitment_metrics('2024-01-01'::date, '2025-12-31'::date, 1);

-- Test 3: Kraći period (poslednji mesec) sa indeksima
SELECT 'TEST 3: Poslednji mesec sa indeksima' as test_name;
SELECT * FROM calculate_recruitment_metrics('2025-11-01'::date, '2025-11-30'::date);

-- =====================================================
-- 4. ANALIZA POJEDINAČNIH UPITA SA INDEKSIMA
-- =====================================================
SELECT 'TEST 4: Analiza pojedinačnih upita sa indeksima' as test_name;

-- Upit za broj aplikacija
SELECT 'Upit za broj aplikacija sa indeksima:' as upit;
EXPLAIN (ANALYZE, BUFFERS) 
SELECT COUNT(*)
FROM applications a
WHERE a.applied_at >= '2024-01-01'::timestamp with time zone
  AND a.applied_at <= '2025-12-31 23:59:59'::timestamp with time zone;

-- Upit za broj zaposlenih
SELECT 'Upit za broj zaposlenih sa indeksima:' as upit;
EXPLAIN (ANALYZE, BUFFERS)
SELECT COUNT(*)
FROM applications a
WHERE a.applied_at >= '2024-01-01'::timestamp with time zone
  AND a.applied_at <= '2025-12-31 23:59:59'::timestamp with time zone
  AND a.application_status = 'HIRED';

-- Upit za prosečno vreme zapošljavanja
SELECT 'Upit za prosečno vreme zapošljavanja sa indeksima:' as upit;
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
SELECT 'Upit za procenat odbijanja ponuda sa indeksima:' as upit;
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
-- 5. ANALIZA INDEKSA I STATISTIKE
-- =====================================================
SELECT 'Analiza indeksa...' as status;

-- Statistike korišćenja indeksa
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan as times_used,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes 
WHERE tablename IN ('applications', 'application_status_history', 'offers')
ORDER BY times_used DESC;

-- Veličina indeksa
SELECT 
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes 
WHERE tablename IN ('applications', 'application_status_history', 'offers')
ORDER BY pg_relation_size(indexrelid) DESC;

-- =====================================================
-- 6. FINALNE STATISTIKE
-- =====================================================
SELECT 'Finalne statistike...' as status;

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
-- 7. ZAVRŠETAK TESTIRANJA
-- =====================================================
\timing off

SELECT 'Testiranje sa indeksima završeno!' as status;
SELECT 'Poredite rezultate sa testiranjem bez indeksa.' as sledeći_korak;
