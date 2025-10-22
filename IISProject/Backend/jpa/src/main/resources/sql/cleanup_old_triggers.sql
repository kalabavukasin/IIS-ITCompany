-- ============================================================
-- Cleanup Script for Old Triggers and PL/SQL Components
-- ============================================================
-- Autor: Generated for Recruitment System
-- Datum: 2024
-- Opis: Skripta za brisanje starih PL/SQL komponenti pre reinstalacije
--       PAŽNJA: Ova skripta briše sve custom komponente!
-- ============================================================

\echo '================================================'
\echo 'CLEANUP: Removing old PL/SQL components...'
\echo '================================================'

-- ============================================================
-- KORAK 1: BRISANJE TRIGERA
-- ============================================================
\echo 'Step 1: Dropping triggers...'

DROP TRIGGER IF EXISTS test_expiration_trigger ON test_invites;
DROP TRIGGER IF EXISTS applications_audit_trigger ON applications;
DROP TRIGGER IF EXISTS offers_audit_trigger ON offers;
DROP TRIGGER IF EXISTS status_history_audit_trigger ON application_status_history;

\echo 'Triggers dropped ✓'


-- ============================================================
-- KORAK 2: BRISANJE TRIGGER FUNKCIJA
-- ============================================================
\echo 'Step 2: Dropping trigger functions...'

DROP FUNCTION IF EXISTS manage_test_expiration() CASCADE;
DROP FUNCTION IF EXISTS audit_application_changes() CASCADE;
DROP FUNCTION IF EXISTS audit_offer_changes() CASCADE;
DROP FUNCTION IF EXISTS audit_status_history_changes() CASCADE;

\echo 'Trigger functions dropped ✓'


-- ============================================================
-- KORAK 3: BRISANJE FUNKCIJA
-- ============================================================
\echo 'Step 3: Dropping functions...'

DROP FUNCTION IF EXISTS calculate_recruitment_metrics(DATE, DATE, BIGINT) CASCADE;
DROP FUNCTION IF EXISTS calculate_job_posting_summary(DATE, DATE) CASCADE;
DROP FUNCTION IF EXISTS calculate_stage_performance(DATE, DATE) CASCADE;
DROP FUNCTION IF EXISTS generate_comprehensive_recruitment_report(DATE, DATE) CASCADE;

-- Dodatne funkcije (ako postoje)
DROP FUNCTION IF EXISTS analyze_stage_performance(DATE, DATE, BIGINT) CASCADE;
DROP FUNCTION IF EXISTS get_job_posting_summary(DATE, DATE) CASCADE;
DROP FUNCTION IF EXISTS identify_bottlenecks(INTEGER) CASCADE;
DROP FUNCTION IF EXISTS calculate_stage_conversions(DATE, DATE) CASCADE;
DROP FUNCTION IF EXISTS get_application_details(DATE, DATE, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS calculate_offer_metrics(DATE, DATE) CASCADE;
DROP FUNCTION IF EXISTS get_stuck_applications(INTEGER) CASCADE;
DROP FUNCTION IF EXISTS generate_comprehensive_metrics(DATE, DATE) CASCADE;

\echo 'Functions dropped ✓'


-- ============================================================
-- KORAK 4: BRISANJE INDEKSA
-- ============================================================
\echo 'Step 4: Dropping indexes...'

DROP INDEX IF EXISTS idx_status_history_application;
DROP INDEX IF EXISTS idx_status_history_entered;
DROP INDEX IF EXISTS idx_status_history_exited;
DROP INDEX IF EXISTS idx_applications_applied_at;
DROP INDEX IF EXISTS idx_applications_status_date;
DROP INDEX IF EXISTS idx_offers_application;
DROP INDEX IF EXISTS idx_offers_created;

-- Dodatni indeksi (ako postoje)
DROP INDEX IF EXISTS idx_applications_job_posting;
DROP INDEX IF EXISTS idx_applications_candidate;
DROP INDEX IF EXISTS idx_status_history_app_stage;
DROP INDEX IF EXISTS idx_offers_status_date;
DROP INDEX IF EXISTS idx_audit_time;
DROP INDEX IF EXISTS idx_audit_entity;

\echo 'Indexes dropped ✓'


-- ============================================================
-- KORAK 5: BRISANJE CUSTOM TIPOVA
-- ============================================================
\echo 'Step 5: Dropping custom types...'

DROP TYPE IF EXISTS recruitment_metrics_type CASCADE;
DROP TYPE IF EXISTS stage_performance_type CASCADE;
DROP TYPE IF EXISTS job_posting_summary_type CASCADE;
DROP TYPE IF EXISTS audit_data_type CASCADE;

-- Dodatni tipovi (ako postoje)
DROP TYPE IF EXISTS application_detail_type CASCADE;
DROP TYPE IF EXISTS bottleneck_info_type CASCADE;
DROP TYPE IF EXISTS conversion_metrics_type CASCADE;
DROP TYPE IF EXISTS job_posting_performance_type CASCADE;
DROP TYPE IF EXISTS stage_timing_type CASCADE;
DROP TYPE IF EXISTS user_performance_type CASCADE;
DROP TYPE IF EXISTS offer_metrics_type CASCADE;
DROP TYPE IF EXISTS time_series_point_type CASCADE;

\echo 'Custom types dropped ✓'


-- ============================================================
-- VERIFIKACIJA BRISANJA
-- ============================================================
\echo ''
\echo 'Verification: Checking for remaining components...'

-- Provera tipova
\echo 'Remaining custom types:'
SELECT 
    typname as type_name,
    'STILL EXISTS ⚠' as status
FROM pg_type t
WHERE t.typnamespace = 'public'::regnamespace
  AND t.typtype = 'c'
  AND t.typname LIKE '%type';

-- Provera funkcija
\echo 'Remaining custom functions:'
SELECT 
    proname as function_name,
    'STILL EXISTS ⚠' as status
FROM pg_proc p
WHERE p.pronamespace = 'public'::regnamespace
  AND p.prolang = (SELECT oid FROM pg_language WHERE lanname = 'plpgsql')
  AND (proname LIKE 'calculate%' 
       OR proname LIKE 'generate%'
       OR proname LIKE 'analyze%'
       OR proname LIKE 'get_%'
       OR proname LIKE 'identify%'
       OR proname LIKE 'manage%'
       OR proname LIKE 'audit%');

-- Provera trigera
\echo 'Remaining triggers:'
SELECT 
    t.tgname as trigger_name,
    c.relname as table_name,
    'STILL EXISTS ⚠' as status
FROM pg_trigger t
JOIN pg_class c ON t.tgrelid = c.oid
WHERE c.relnamespace = 'public'::regnamespace
  AND NOT t.tgisinternal
  AND (t.tgname LIKE '%audit%' OR t.tgname LIKE '%expiration%');

-- Provera indeksa
\echo 'Remaining custom indexes:'
SELECT 
    indexname as index_name,
    tablename as table_name,
    'STILL EXISTS ⚠' as status
FROM pg_indexes
WHERE schemaname = 'public'
  AND indexname LIKE 'idx_%';


-- ============================================================
-- ZAVRŠETAK
-- ============================================================
\echo ''
\echo '================================================'
\echo 'CLEANUP COMPLETED!'
\echo '================================================'
\echo ''
\echo 'All old PL/SQL components have been removed.'
\echo 'You can now run plsql_setup.sql to reinstall.'
\echo ''
\echo 'To reinstall, run:'
\echo '  \\i plsql_setup.sql'
\echo ''
\echo '================================================'


-- ============================================================
-- NAPOMENE
-- ============================================================
--
-- Ova skripta je korisna u sledećim situacijama:
--
-- 1. PRE REINSTALACIJE
--    Kada želite da ponovo instalirate sve komponente
--    sa izmenjenim definicijama
--
-- 2. MIGRACIJA
--    Kada prelazite na novu verziju PL/SQL komponenti
--
-- 3. TROUBLESHOOTING
--    Kada imate probleme sa PL/SQL komponentama i želite
--    da počnete ispočetka
--
-- 4. DEVELOPMENT
--    Tokom razvoja kada često menjate definicije
--
-- PAŽNJA:
-- - Ova skripta NE BRIŠE podatke iz tabela!
-- - Briše samo PL/SQL komponente (funkcije, trigere, tipove, indekse)
-- - Audit logovi u audit_logs tabeli ostaju netaknuti
-- - Nakon brisanja, morate ponovo pokrenuti plsql_setup.sql
--
-- ============================================================

