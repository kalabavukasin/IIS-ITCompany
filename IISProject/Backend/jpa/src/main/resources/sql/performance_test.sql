-- =====================================================
-- SKRIPT ZA TESTIRANJE PERFORMANSI FUNKCIJE
-- =====================================================
-- Ovaj skript testira performanse calculate_recruitment_metrics funkcije
-- pre i posle dodavanja indeksa

-- =====================================================
-- 1. UZIMANJE BAZELINE PERFORMANSI (BEZ INDEKSA)
-- =====================================================

-- Prvo uklanjamo postojeće indekse za testiranje
DROP INDEX IF EXISTS idx_applications_applied_at;
DROP INDEX IF EXISTS idx_applications_status_date;
DROP INDEX IF EXISTS idx_status_history_application;
DROP INDEX IF EXISTS idx_status_history_entered;
DROP INDEX IF EXISTS idx_status_history_exited;
DROP INDEX IF EXISTS idx_offers_application;
DROP INDEX IF EXISTS idx_offers_created;

-- Testiranje performansi bez indeksa
\timing on

-- Test 1: Ceo period (2024-2025)
SELECT 'TEST 1: Ceo period bez indeksa' as test_name;
SELECT * FROM calculate_recruitment_metrics('2024-01-01'::date, '2025-12-31'::date);

-- Test 2: Konkretan job posting (koristimo originalni ID 1)
SELECT 'TEST 2: Konkretan job posting bez indeksa' as test_name;
SELECT * FROM calculate_recruitment_metrics('2024-01-01'::date, '2025-12-31'::date, 1);

-- Test 3: Kraći period (poslednji mesec)
SELECT 'TEST 3: Poslednji mesec bez indeksa' as test_name;
SELECT * FROM calculate_recruitment_metrics('2025-11-01'::date, '2025-11-30'::date);

-- Test 4: Analiza pojedinačnih upita
SELECT 'TEST 4: Analiza pojedinačnih upita bez indeksa' as test_name;

-- Upit za broj aplikacija
EXPLAIN (ANALYZE, BUFFERS) 
SELECT COUNT(*)
FROM applications a
WHERE a.applied_at >= '2024-01-01'::timestamp with time zone
  AND a.applied_at <= '2025-12-31 23:59:59'::timestamp with time zone;

-- Upit za broj zaposlenih
EXPLAIN (ANALYZE, BUFFERS)
SELECT COUNT(*)
FROM applications a
WHERE a.applied_at >= '2024-01-01'::timestamp with time zone
  AND a.applied_at <= '2025-12-31 23:59:59'::timestamp with time zone
  AND a.application_status = 'HIRED';

-- Upit za prosečno vreme zapošljavanja
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
-- 2. DODAVANJE OPTIMIZOVANIH INDEKSA
-- =====================================================

SELECT 'DODAVANJE OPTIMIZOVANIH INDEKSA...' as status;

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
-- 3. TESTIRANJE PERFORMANSI SA INDEKSIMA
-- =====================================================

SELECT 'TESTIRANJE SA INDEKSIMA...' as status;

-- Test 1: Ceo period (2024-2025) sa indeksima
SELECT 'TEST 1: Ceo period sa indeksima' as test_name;
SELECT * FROM calculate_recruitment_metrics('2024-01-01'::date, '2025-12-31'::date);

-- Test 2: Konkretan job posting sa indeksima (koristimo originalni ID 1)
SELECT 'TEST 2: Konkretan job posting sa indeksima' as test_name;
SELECT * FROM calculate_recruitment_metrics('2024-01-01'::date, '2025-12-31'::date, 1);

-- Test 3: Kraći period (poslednji mesec) sa indeksima
SELECT 'TEST 3: Poslednji mesec sa indeksima' as test_name;
SELECT * FROM calculate_recruitment_metrics('2025-11-01'::date, '2025-11-30'::date);

-- Test 4: Analiza pojedinačnih upita sa indeksima
SELECT 'TEST 4: Analiza pojedinačnih upita sa indeksima' as test_name;

-- Upit za broj aplikacija sa indeksima
EXPLAIN (ANALYZE, BUFFERS) 
SELECT COUNT(*)
FROM applications a
WHERE a.applied_at >= '2024-01-01'::timestamp with time zone
  AND a.applied_at <= '2025-12-31 23:59:59'::timestamp with time zone;

-- Upit za broj zaposlenih sa indeksima
EXPLAIN (ANALYZE, BUFFERS)
SELECT COUNT(*)
FROM applications a
WHERE a.applied_at >= '2024-01-01'::timestamp with time zone
  AND a.applied_at <= '2025-12-31 23:59:59'::timestamp with time zone
  AND a.application_status = 'HIRED';

-- Upit za prosečno vreme zapošljavanja sa indeksima
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

-- Upit za procenat odbijanja ponuda sa indeksima
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
-- 4. ANALIZA INDEKSA I STATISTIKE
-- =====================================================

SELECT 'ANALIZA INDEKSA...' as status;

-- Prikaz svih indeksa na tabelama
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename IN ('applications', 'application_status_history', 'offers')
ORDER BY tablename, indexname;

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
-- 5. FINALNE STATISTIKE
-- =====================================================

SELECT 'FINALNE STATISTIKE...' as status;

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

-- Distribucija statusova
SELECT 
    'Application Status Distribution' as metric,
    application_status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM applications
GROUP BY application_status
ORDER BY count DESC;

SELECT 
    'Offer Status Distribution' as metric,
    offer_status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM offers
GROUP BY offer_status
ORDER BY count DESC;

\timing off
