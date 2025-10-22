-- ============================================================
-- Indexes for Recruitment System Performance Optimization
-- ============================================================
-- Autor: Extracted from PostgreSQL Database
-- Datum: 2024
-- Opis: Indeksi za ubrzanje upita u sistemu za zapošljavanje
-- ============================================================

-- ============================================================
-- INDEKSI NA TABELI: application_status_history
-- ============================================================

-- Indeks za brzo pronalaženje history zapisa po aplikaciji
CREATE INDEX idx_status_history_application 
ON application_status_history USING btree (application_id);

COMMENT ON INDEX idx_status_history_application IS 
'Ubrzava pronalaženje istorije statusa za konkretnu aplikaciju';

-- Indeks za filtriranje po datumu ulaska u fazu
CREATE INDEX idx_status_history_entered 
ON application_status_history USING btree (entered_at);

COMMENT ON INDEX idx_status_history_entered IS 
'Ubrzava upite koji filtriraju po datumu ulaska u fazu (entered_at)';

-- Indeks za filtriranje po datumu izlaska iz faze
CREATE INDEX idx_status_history_exited 
ON application_status_history USING btree (exited_at);

COMMENT ON INDEX idx_status_history_exited IS 
'Ubrzava upite koji filtriraju po datumu izlaska iz faze (exited_at)';


-- ============================================================
-- INDEKSI NA TABELI: applications
-- ============================================================

-- Indeks za filtriranje po datumu prijave
CREATE INDEX idx_applications_applied_at 
ON applications USING btree (applied_at);

COMMENT ON INDEX idx_applications_applied_at IS 
'Ubrzava upite koji filtriraju aplikacije po datumu prijave';

-- Kompozitni indeks za filtriranje po statusu i datumu
CREATE INDEX idx_applications_status_date 
ON applications USING btree (application_status, applied_at);

COMMENT ON INDEX idx_applications_status_date IS 
'Ubrzava upite koji filtriraju aplikacije po statusu i datumu istovremeno';


-- ============================================================
-- INDEKSI NA TABELI: offers
-- ============================================================

-- Indeks za pronalaženje ponuda po aplikaciji
CREATE INDEX idx_offers_application 
ON offers USING btree (application_id);

COMMENT ON INDEX idx_offers_application IS 
'Ubrzava pronalaženje svih ponuda za konkretnu aplikaciju';

-- Indeks za filtriranje ponuda po datumu kreiranja
CREATE INDEX idx_offers_created 
ON offers USING btree (created_at);

COMMENT ON INDEX idx_offers_created IS 
'Ubrzava upite koji filtriraju ponude po datumu kreiranja';


-- ============================================================
-- NAPOMENE O PERFORMANSAMA
-- ============================================================
--
-- Ovi indeksi značajno ubrzavaju sledeće tipove upita:
--
-- 1. Pronalaženje aplikacija u određenom vremenskom periodu
--    SELECT * FROM applications WHERE applied_at BETWEEN ? AND ?
--
-- 2. Filtriranje aplikacija po statusu i periodu
--    SELECT * FROM applications 
--    WHERE application_status = 'HIRED' AND applied_at BETWEEN ? AND ?
--
-- 3. Analiza istorije statusa za aplikaciju
--    SELECT * FROM application_status_history 
--    WHERE application_id = ?
--
-- 4. Računanje prosečnog vremena u fazi
--    SELECT AVG(exited_at - entered_at) 
--    FROM application_status_history 
--    WHERE entered_at BETWEEN ? AND ?
--
-- 5. Pronalaženje ponuda za aplikaciju
--    SELECT * FROM offers WHERE application_id = ?
--
-- ============================================================

-- ============================================================
-- PROVERA PERFORMANSI INDEKSA
-- ============================================================
--
-- Možete proveriti da li se indeksi koriste pomoću EXPLAIN ANALYZE:
--
-- EXPLAIN ANALYZE
-- SELECT * FROM applications 
-- WHERE application_status = 'HIRED' 
-- AND applied_at >= '2024-01-01' 
-- AND applied_at <= '2024-12-31';
--
-- Očekivani rezultat: "Index Scan using idx_applications_status_date"
--
-- ============================================================

