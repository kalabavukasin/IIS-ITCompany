-- ============================================================
-- PL/SQL Setup Script - Complete Initialization
-- ============================================================
-- Autor: Generated for Recruitment System
-- Datum: 2024
-- Opis: Glavni skripta za inicijalizaciju svih PL/SQL komponenti
--       Izvršava se automatski pri startovanju aplikacije
-- ============================================================

-- ============================================================
-- KORAK 1: KREIRANJE CUSTOM TIPOVA
-- ============================================================
\echo 'Creating custom types...'
\i plsql_types.sql

-- ============================================================
-- KORAK 2: KREIRANJE FUNKCIJA
-- ============================================================
\echo 'Creating functions...'
\i plsql_functions.sql

-- ============================================================
-- KORAK 3: KREIRANJE REPORT FUNKCIJE
-- ============================================================
\echo 'Creating report function...'
\i plsql_report.sql

-- ============================================================
-- KORAK 4: KREIRANJE TRIGERA
-- ============================================================
\echo 'Creating triggers...'
\i plsql_triggers.sql

-- ============================================================
-- KORAK 5: KREIRANJE INDEKSA
-- ============================================================
\echo 'Creating indexes...'
\i plsql_indexes.sql

-- ============================================================
-- VERIFIKACIJA INSTALACIJE
-- ============================================================
\echo 'Verifying installation...'

-- Provera tipova
SELECT 
    typname as type_name,
    'Type' as object_type
FROM pg_type t
WHERE t.typnamespace = 'public'::regnamespace
  AND t.typtype = 'c'
  AND t.typname LIKE '%type'
ORDER BY typname;

-- Provera funkcija
SELECT 
    proname as function_name,
    'Function' as object_type,
    pg_get_function_result(p.oid) as return_type
FROM pg_proc p
WHERE p.pronamespace = 'public'::regnamespace
  AND p.prolang = (SELECT oid FROM pg_language WHERE lanname = 'plpgsql')
  AND p.prorettype != 'trigger'::regtype::oid
  AND (proname LIKE 'calculate%' 
       OR proname LIKE 'generate%')
ORDER BY proname;

-- Provera trigera
SELECT 
    t.tgname as trigger_name,
    c.relname as table_name,
    'Trigger' as object_type
FROM pg_trigger t
JOIN pg_class c ON t.tgrelid = c.oid
WHERE c.relnamespace = 'public'::regnamespace
  AND NOT t.tgisinternal
ORDER BY c.relname, t.tgname;

-- Provera indeksa
SELECT 
    indexname as index_name,
    tablename as table_name,
    'Index' as object_type
FROM pg_indexes
WHERE schemaname = 'public'
  AND indexname LIKE 'idx_%'
ORDER BY tablename, indexname;

\echo 'PL/SQL setup completed successfully!'

-- ============================================================
-- NAPOMENE ZA KORIŠĆENJE
-- ============================================================
--
-- Da pokrenete ovu skriptu u psql konzoli:
--   \i plsql_setup.sql
--
-- Ili iz command line-a:
--   psql -U username -d database_name -f plsql_setup.sql
--
-- Za Spring Boot aplikaciju, skripta se automatski izvršava
-- preko PlSqlInitializationService pri startovanju.
--
-- ============================================================

