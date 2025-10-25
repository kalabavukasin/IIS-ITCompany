
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
    
    IF (TG_OP = 'INSERT') THEN
        IF NEW.deadline < v_current_time + INTERVAL '1 hour' THEN
            RAISE EXCEPTION 'Test deadline mora biti najmanje 1 sat u buducnosti. Deadline: %, Sada: %', 
                NEW.deadline, v_current_time;
        END IF;
        
        SELECT application_status INTO v_application_status
        FROM applications WHERE id = NEW.application_id;
        
        IF v_application_status != 'ACTIVE' THEN
            RAISE EXCEPTION 'Test se moze kreirati samo za ACTIVE aplikacije. Status: %', 
                v_application_status;
        END IF;
        
        IF EXISTS (
            SELECT 1 FROM test_invites 
            WHERE application_id = NEW.application_id 
              AND id != NEW.id
              AND test_status IN ('SENT', 'COMPLETED')
        ) THEN
            RAISE EXCEPTION 'Vec postoji aktivan test za aplikaciju ID %', NEW.application_id;
        END IF;
        
        RETURN NEW;
    END IF;
    
    IF (TG_OP = 'UPDATE' AND OLD.test_status != NEW.test_status) THEN
        
        IF NEW.test_status = 'EXPIRED' THEN
            IF v_current_time <= NEW.deadline THEN
                RAISE EXCEPTION 'Ne mozete postaviti test na EXPIRED pre deadline-a';
            END IF;
            
            SELECT application_status INTO v_application_status
            FROM applications WHERE id = NEW.application_id;
            
            IF v_application_status = 'ACTIVE' THEN
                UPDATE applications
                SET application_status = 'REJECTED',
                    note = COALESCE(note || ' | ', '') || 
                           'Automatski odbijeno - test nije kompletiran do roka: ' || 
                           NEW.deadline::TEXT
                WHERE id = NEW.application_id;
                
                UPDATE application_status_history
                SET exited_at = v_current_time,
                    comment = COALESCE(comment || ' | ', '') || 'Auto-rejected: Test expired'
                WHERE application_id = NEW.application_id
                  AND exited_at IS NULL;
                
                INSERT INTO test_results (test_invite_id, score, passed)
                VALUES (NEW.id, 0, FALSE)
                ON CONFLICT (test_invite_id) DO NOTHING;
                
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
        
        IF NEW.test_status = 'VERIFIED' THEN
            SELECT EXISTS(
                SELECT 1 FROM test_results WHERE test_invite_id = NEW.id
            ) INTO v_test_result_exists;
            
            IF NOT v_test_result_exists THEN
                RAISE EXCEPTION 'Test ne moze biti VERIFIED bez test_result zapisa';
            END IF;
        END IF;
        
        IF NEW.test_status = 'COMPLETED' THEN
            IF v_current_time > NEW.deadline THEN
                RAISE EXCEPTION 'Ne mozete kompletirati test nakon isteka deadline-a. Deadline: %, Sada: %',
                    NEW.deadline, v_current_time;
            END IF;
        END IF;
        
    END IF;
    
    RETURN NEW;
END;
$$;


CREATE TRIGGER test_expiration_trigger 
BEFORE INSERT OR UPDATE ON test_invites 
FOR EACH ROW 
EXECUTE FUNCTION manage_test_expiration();

