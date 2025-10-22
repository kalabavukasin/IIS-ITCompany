-- ============================================================
-- PL/SQL Triggers for Recruitment System
-- ============================================================
-- Autor: Extracted from PostgreSQL Database
-- Datum: 2024
-- Opis: Kompleksni trigeri za automatsko upravljanje testovima i validaciju
-- ============================================================

-- ============================================================
-- TRIGGER FUNKCIJA: manage_test_expiration
-- ============================================================
-- Opis: Kompleksna biznis logika za upravljanje testovima:
--       INSERT: Validacija (deadline, status aplikacije, duplikati)
--       UPDATE: Automatsko odbijanje pri expiraciji, provere statusa
-- ============================================================

CREATE OR REPLACE FUNCTION manage_test_expiration()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_current_time TIMESTAMP WITH TIME ZONE;
    v_application_status TEXT;
    v_test_result_exists BOOLEAN;
BEGIN
    v_current_time := NOW();
    
    -- ==========================================================================
    -- INSERT: Validacija pri kreiranju testa
    -- ==========================================================================
    IF (TG_OP = 'INSERT') THEN
        -- Provera 1: Deadline mora biti u budućnosti (minimum 1 sat)
        IF NEW.deadline < v_current_time + INTERVAL '1 hour' THEN
            RAISE EXCEPTION 'Test deadline mora biti najmanje 1 sat u budućnosti. Deadline: %, Sada: %', 
                NEW.deadline, v_current_time;
        END IF;
        
        -- Provera 2: Aplikacija mora biti ACTIVE
        SELECT application_status INTO v_application_status
        FROM applications WHERE id = NEW.application_id;
        
        IF v_application_status != 'ACTIVE' THEN
            RAISE EXCEPTION 'Test se može kreirati samo za ACTIVE aplikacije. Status: %', 
                v_application_status;
        END IF;
        
        -- Provera 3: Duplikat testa za istu aplikaciju
        IF EXISTS (
            SELECT 1 FROM test_invites 
            WHERE application_id = NEW.application_id 
              AND id != NEW.id
              AND test_status IN ('SENT', 'COMPLETED')
        ) THEN
            RAISE EXCEPTION 'Već postoji aktivan test za aplikaciju ID %', NEW.application_id;
        END IF;
        
        RETURN NEW;
    END IF;
    
    -- ==========================================================================
    -- UPDATE: Automatsko odbijanje pri expiraciji
    -- ==========================================================================
    IF (TG_OP = 'UPDATE' AND OLD.test_status != NEW.test_status) THEN
        
        -- Scenario 1: Test postavljen na EXPIRED → automatski odbij aplikaciju
        IF NEW.test_status = 'EXPIRED' THEN
            -- Proveri da li je deadline zaista prošao
            IF v_current_time <= NEW.deadline THEN
                RAISE EXCEPTION 'Ne možete postaviti test na EXPIRED pre deadline-a';
            END IF;
            
            -- Proveri status aplikacije
            SELECT application_status INTO v_application_status
            FROM applications WHERE id = NEW.application_id;
            
            -- Automatsko odbijanje ako je aplikacija još uvek ACTIVE
            IF v_application_status = 'ACTIVE' THEN
                -- 1. Odbij aplikaciju
                UPDATE applications
                SET application_status = 'REJECTED',
                    note = COALESCE(note || ' | ', '') || 
                           'Automatski odbijeno - test nije kompletiran do roka: ' || 
                           NEW.deadline::TEXT
                WHERE id = NEW.application_id;
                
                -- 2. Zatvori trenutni workflow stage
                UPDATE application_status_history
                SET exited_at = v_current_time,
                    comment = COALESCE(comment || ' | ', '') || 'Auto-rejected: Test expired'
                WHERE application_id = NEW.application_id
                  AND exited_at IS NULL;
                
                -- 3. Kreiraj test result sa neuspehom
                INSERT INTO test_results (test_invite_id, score, passed)
                VALUES (NEW.id, 0, FALSE)
                ON CONFLICT (test_invite_id) DO NOTHING;
                
                -- 4. Audit log
                INSERT INTO audit_logs (
                    user_id, action, entity_type, entity_id,
                    before_data_json, after_data_json, time_utc, source, ip_addr
                ) VALUES (
                    COALESCE(current_setting('app.current_user_id', true)::BIGINT, 1),
                    'AUTO_REJECT_EXPIRED_TEST',
                    'APPLICATION',
                    NEW.application_id,
                    jsonb_build_object('status', v_application_status, 'test_id', NEW.id)::TEXT,
                    jsonb_build_object('status', 'REJECTED', 'reason', 'Test expired')::TEXT,
                    v_current_time,
                    'PL/SQL_TRIGGER',
                    inet_client_addr()
                );
                
                RAISE NOTICE 'Aplikacija % automatski odbijena - test % istekao', 
                    NEW.application_id, NEW.id;
            END IF;
        END IF;
        
        -- Scenario 2: Test postavljen na VERIFIED → mora postojati test_result
        IF NEW.test_status = 'VERIFIED' THEN
            SELECT EXISTS(
                SELECT 1 FROM test_results WHERE test_invite_id = NEW.id
            ) INTO v_test_result_exists;
            
            IF NOT v_test_result_exists THEN
                RAISE EXCEPTION 'Test ne može biti VERIFIED bez test_result zapisa';
            END IF;
        END IF;
        
        -- Scenario 3: Test postavljen na COMPLETED → proveri deadline
        IF NEW.test_status = 'COMPLETED' THEN
            IF v_current_time > NEW.deadline THEN
                RAISE EXCEPTION 'Ne možete kompletirati test nakon isteka deadline-a. Deadline: %, Sada: %',
                    NEW.deadline, v_current_time;
            END IF;
        END IF;
        
    END IF;
    
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION manage_test_expiration() IS 
'Kompleksna trigger funkcija za upravljanje test pozivima:
- INSERT: Validira deadline, status aplikacije i proverava duplikate
- UPDATE: Automatski odbija aplikaciju kada test istekne, proverava statuse i deadline
- Kreira audit log, zatvara workflow stages, i održava integritet podataka';


-- ============================================================
-- TRIGGER: test_expiration_trigger
-- ============================================================

CREATE TRIGGER test_expiration_trigger 
BEFORE INSERT OR UPDATE ON test_invites 
FOR EACH ROW 
EXECUTE FUNCTION manage_test_expiration();

COMMENT ON TRIGGER test_expiration_trigger ON test_invites IS 
'Trigger koji automatski validira testove pri INSERT i upravlja životnim ciklusom testa pri UPDATE.
Automatski odbija aplikacije kada test istekne.';


-- ============================================================
-- BUSINESS LOGIKA TRIGERA
-- ============================================================
--
-- VALIDACIJE PRI INSERT:
-- 1. Deadline mora biti minimum 1 sat u budućnosti
-- 2. Aplikacija mora biti u ACTIVE statusu
-- 3. Ne može postojati više aktivnih testova za istu aplikaciju
--
-- AUTOMATIZACIJA PRI UPDATE (status → EXPIRED):
-- 1. Provera da li je deadline zaista prošao
-- 2. Automatsko odbijanje aplikacije (status → REJECTED)
-- 3. Zatvaranje trenutnog workflow stage-a
-- 4. Kreiranje test_result zapisa sa score=0, passed=FALSE
-- 5. Snimanje detaljnog audit loga
--
-- DODATNE PROVERE:
-- - VERIFIED status zahteva postojanje test_result zapisa
-- - COMPLETED status se ne može postaviti nakon deadline-a
--
-- ============================================================
