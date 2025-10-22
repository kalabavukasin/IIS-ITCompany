-- ============================================================
-- PL/SQL Triggers for Recruitment System
-- ============================================================
-- Autor: Extracted from PostgreSQL Database
-- Datum: 2024
-- Opis: Trigeri za automatsko praćenje promena i upravljanje test pozivima
-- ============================================================

-- ============================================================
-- TRIGGER FUNKCIJA: manage_test_expiration
-- ============================================================
-- Opis: Upravlja expiracijom test poziva
--       - Automatski postavlja datum isteka pri INSERT
--       - Snima audit log pri DELETE
-- ============================================================

CREATE OR REPLACE FUNCTION manage_test_expiration()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- Pri kreiranju novog test poziva, postavi datum isteka na 7 dana od sada
        IF NEW.expiration_date IS NULL THEN
            NEW.expiration_date := CURRENT_TIMESTAMP + INTERVAL '7 days';
        END IF;
        RETURN NEW;
        
    ELSIF TG_OP = 'DELETE' THEN
        -- Pri brisanju, snimi u audit log
        INSERT INTO audit_logs (
            entity_type,
            entity_id,
            action,
            before_data_json,
            time_utc
        ) VALUES (
            'TEST_INVITE',
            OLD.id,
            'DELETE',
            json_build_object(
                'id', OLD.id,
                'test_result_id', OLD.test_result_id,
                'application_id', OLD.application_id,
                'invite_status', OLD.invite_status,
                'sent_at', OLD.sent_at,
                'expiration_date', OLD.expiration_date
            )::TEXT,
            CURRENT_TIMESTAMP
        );
        RETURN OLD;
    END IF;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION manage_test_expiration() IS 
'Trigger funkcija za upravljanje test pozivima - postavlja datum isteka i snima brisanja u audit log';


-- ============================================================
-- TRIGGER: test_expiration_trigger
-- ============================================================

CREATE TRIGGER test_expiration_trigger 
BEFORE INSERT OR DELETE ON test_invites 
FOR EACH ROW 
EXECUTE FUNCTION manage_test_expiration();

COMMENT ON TRIGGER test_expiration_trigger ON test_invites IS 
'Trigger koji automatski upravlja test pozivima - postavlja datum isteka i prati brisanja';


-- ============================================================
-- NAPOMENE O DODATNIM TRIGERIMA
-- ============================================================
-- 
-- Dodatni trigeri mogu biti definisani u Java kodu ili direktno u bazi
-- kroz Spring Data JPA @EntityListeners ili JPA Lifecycle callbacks:
--
-- 1. ApplicationAuditTrigger - prati promene na applications tabeli
-- 2. OfferAuditTrigger - prati promene na offers tabeli  
-- 3. StatusHistoryAuditTrigger - prati promene u application_status_history
--
-- Ovi trigeri obično snimaju podatke u audit_logs tabelu sa sledećim podacima:
-- - entity_type (APPLICATION, OFFER, STATUS_HISTORY)
-- - entity_id (ID entiteta)
-- - action (INSERT, UPDATE, DELETE)
-- - before_data_json (staro stanje u JSON formatu)
-- - after_data_json (novo stanje u JSON formatu)
-- - time_utc (vreme promene)
-- - user_id (ko je napravio promenu - može biti NULL)
--
-- ============================================================

