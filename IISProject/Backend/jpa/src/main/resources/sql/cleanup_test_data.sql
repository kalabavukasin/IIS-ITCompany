-- =====================================================
-- SKRIPT ZA ČIŠĆENJE TEST PODATAKA
-- =====================================================
-- PAŽNJA: Ovaj skript briše sve test podatke!
-- Koristiti samo ako želite da resetujete bazu na početno stanje

-- =====================================================
-- 1. BRISANJE TEST PODATAKA (ZADRŽAVANJE ORIGINALNIH)
-- =====================================================

-- Brisanje test aplikacija (zadržavamo originalne ID 1-21)
DELETE FROM applications WHERE id >= 100;

-- Brisanje test status history (zadržavamo originalne)
DELETE FROM application_status_history WHERE application_id >= 100;

-- Brisanje test ponuda (zadržavamo originalne)
DELETE FROM offers WHERE application_id >= 100;

-- Brisanje test job postings (zadržavamo originalne ID 1-7)
DELETE FROM job_postings WHERE id >= 100;

-- Brisanje test kandidata (zadržavamo originalne ID 1-7)
DELETE FROM candidate_profiles WHERE id >= 100;

-- Brisanje test korisnika (zadržavamo originalne ID 1-3)
DELETE FROM users WHERE id >= 100;

-- =====================================================
-- 2. RESETOVANJE SEQUENCE-OVA
-- =====================================================

-- Resetovanje sequence-ova na originalne vrednosti
SELECT setval('applications_id_seq', 21);
SELECT setval('application_status_history_id_seq', 12);
SELECT setval('offers_id_seq', 1);
SELECT setval('job_postings_id_seq', 7);
SELECT setval('candidate_profiles_id_seq', 7);
SELECT setval('users_id_seq', 3);

-- =====================================================
-- 3. UKLANJANJE TEST INDEKSA
-- =====================================================

-- Uklanjanje indeksa kreiranih za testiranje
DROP INDEX IF EXISTS idx_applications_date_status_job;
DROP INDEX IF EXISTS idx_status_history_app_exit;
DROP INDEX IF EXISTS idx_offers_date_status;
DROP INDEX IF EXISTS idx_offers_application_id;
DROP INDEX IF EXISTS idx_status_history_entered_at;
DROP INDEX IF EXISTS idx_applications_status_date_job;

-- =====================================================
-- 4. PONOVNO DODAVANJE ORIGINALNIH INDEKSA
-- =====================================================

-- Dodavanje originalnih indeksa
CREATE INDEX IF NOT EXISTS idx_status_history_application 
ON application_status_history USING btree (application_id);

CREATE INDEX IF NOT EXISTS idx_status_history_entered 
ON application_status_history USING btree (entered_at);

CREATE INDEX IF NOT EXISTS idx_status_history_exited 
ON application_status_history USING btree (exited_at);

CREATE INDEX IF NOT EXISTS idx_applications_applied_at 
ON applications USING btree (applied_at);

CREATE INDEX IF NOT EXISTS idx_applications_status_date 
ON applications USING btree (application_status, applied_at);

CREATE INDEX IF NOT EXISTS idx_offers_application 
ON offers USING btree (application_id);

CREATE INDEX IF NOT EXISTS idx_offers_created 
ON offers USING btree (created_at);

-- =====================================================
-- 5. VERIFIKACIJA ČIŠĆENJA
-- =====================================================

SELECT 'VERIFIKACIJA ČIŠĆENJA...' as status;

-- Broj zapisa po tabeli nakon čišćenja
SELECT 
    'Applications' as table_name,
    COUNT(*) as total_records
FROM applications
UNION ALL
SELECT 
    'Application Status History' as table_name,
    COUNT(*) as total_records
FROM application_status_history
UNION ALL
SELECT 
    'Offers' as table_name,
    COUNT(*) as total_records
FROM offers
UNION ALL
SELECT 
    'Job Postings' as table_name,
    COUNT(*) as total_records
FROM job_postings
UNION ALL
SELECT 
    'Candidates' as table_name,
    COUNT(*) as total_records
FROM candidate_profiles
UNION ALL
SELECT 
    'Users' as table_name,
    COUNT(*) as total_records
FROM users;

-- Test da li funkcija radi sa originalnim podacima
SELECT 'Test funkcije sa originalnim podacima:' as test;
SELECT * FROM calculate_recruitment_metrics('2025-10-01'::date, '2025-10-31'::date);

SELECT 'Čišćenje završeno!' as status;
