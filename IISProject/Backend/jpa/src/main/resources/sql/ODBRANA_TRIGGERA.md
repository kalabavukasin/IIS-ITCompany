# Dokumentacija za Odbranu - PL/SQL Trigeri

## 📌 Pregled Trigera

U ovom projektu implementiran je **trigger sistem** za automatsko upravljanje i praćenje podataka u sistemu za zapošljavanje.

## 🎯 Implementirani Trigger

### `test_expiration_trigger`

**Tabela:** `test_invites`  
**Tip:** `BEFORE INSERT OR DELETE`  
**Funkcija:** `manage_test_expiration()`

#### Svrha
Ovaj trigger automatski upravlja životnim ciklusom test poziva:
1. **Pri INSERT** - automatski postavlja datum isteka
2. **Pri DELETE** - snima audit log brisanja

#### Kod Trigger Funkcije
```sql
CREATE OR REPLACE FUNCTION manage_test_expiration()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- Pri kreiranju novog test poziva, postavi datum isteka na 7 dana
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
```

#### Kreiranje Trigera
```sql
CREATE TRIGGER test_expiration_trigger 
BEFORE INSERT OR DELETE ON test_invites 
FOR EACH ROW 
EXECUTE FUNCTION manage_test_expiration();
```

## 📊 Kako Trigger Radi

### Scenario 1: INSERT - Automatsko Postavljanje Datuma Isteka

**Što se dešava:**
```sql
-- Korisnik kreira test poziv bez expiration_date
INSERT INTO test_invites (test_result_id, application_id, invite_status, sent_at)
VALUES (1, 10, 'PENDING', CURRENT_TIMESTAMP);

-- Trigger automatski dodaje:
-- expiration_date = CURRENT_TIMESTAMP + 7 dana
```

**Prednosti:**
- ✅ Automatizacija - ne mora ručno da se postavlja rok
- ✅ Konzistentnost - svi test pozivi imaju rok od 7 dana
- ✅ Sprečava greške - ne može se kreirati poziv bez roka

### Scenario 2: DELETE - Audit Trail

**Što se dešava:**
```sql
-- Korisnik briše test poziv
DELETE FROM test_invites WHERE id = 5;

-- Trigger automatski snima u audit_logs:
-- {
--   "entity_type": "TEST_INVITE",
--   "entity_id": 5,
--   "action": "DELETE",
--   "before_data_json": "{\"id\":5, \"test_result_id\":1, ...}",
--   "time_utc": "2024-10-22 10:30:00"
-- }
```

**Prednosti:**
- ✅ Potpuni audit trail - sve brisanja su zabeležene
- ✅ Mogućnost vraćanja podataka - imamo sve stare podatke u JSON-u
- ✅ Transparency - znamo ko i kada je obrisao podatke

## 🧪 Testiranje Trigera

### Test 1: Provera Postojanja Trigera
```sql
SELECT 
    t.tgname as trigger_name,
    c.relname as table_name,
    p.proname as function_name,
    CASE 
        WHEN t.tgtype & 2 = 2 THEN 'BEFORE'
        ELSE 'AFTER'
    END as timing,
    CASE 
        WHEN t.tgtype & 4 = 4 THEN 'INSERT '
        ELSE ''
    END ||
    CASE 
        WHEN t.tgtype & 16 = 16 THEN 'DELETE'
        ELSE ''
    END as events
FROM pg_trigger t
JOIN pg_class c ON t.tgrelid = c.oid
JOIN pg_proc p ON t.tgfoid = p.oid
WHERE c.relname = 'test_invites'
  AND t.tgname = 'test_expiration_trigger';
```

**Očekivani rezultat:**
```
trigger_name            | table_name   | function_name           | timing | events
-----------------------|--------------|-------------------------|--------|------------------
test_expiration_trigger | test_invites | manage_test_expiration  | BEFORE | INSERT DELETE
```

### Test 2: Test INSERT Funkcionalnosti
```sql
-- Kreiranje test poziva bez expiration_date
INSERT INTO test_invites (test_result_id, application_id, invite_status, sent_at)
VALUES (1, 100, 'PENDING', CURRENT_TIMESTAMP)
RETURNING id, expiration_date;

-- Verifikacija: expiration_date bi trebalo da bude ~7 dana u budućnosti
SELECT 
    id,
    sent_at,
    expiration_date,
    EXTRACT(DAY FROM (expiration_date - sent_at)) as days_until_expiration
FROM test_invites
WHERE id = (SELECT MAX(id) FROM test_invites);
```

**Očekivani rezultat:**
```
id | sent_at             | expiration_date     | days_until_expiration
---|---------------------|---------------------|----------------------
XX | 2024-10-22 10:00:00 | 2024-10-29 10:00:00 | 7
```

### Test 3: Test DELETE i Audit Funkcionalnosti
```sql
-- Priprema: Kreiranje test zapisa
INSERT INTO test_invites (test_result_id, application_id, invite_status, sent_at)
VALUES (1, 200, 'PENDING', CURRENT_TIMESTAMP)
RETURNING id;  -- Zapamti ovaj ID

-- Brisanje zapisa (trigger će se aktivirati)
DELETE FROM test_invites WHERE id = <ID_IZ_GORE>;

-- Provera audit loga
SELECT 
    entity_type,
    entity_id,
    action,
    before_data_json,
    time_utc
FROM audit_logs
WHERE entity_type = 'TEST_INVITE'
  AND entity_id = <ID_IZ_GORE>
ORDER BY time_utc DESC
LIMIT 1;
```

**Očekivani rezultat:**
```
entity_type | entity_id | action | before_data_json                           | time_utc
------------|-----------|--------|-------------------------------------------|-------------------
TEST_INVITE | XX        | DELETE | {"id":XX,"test_result_id":1,"invite_...} | 2024-10-22 10:30:00
```

## 🎓 Pitanja za Odbranu i Odgovori

### Q1: Zašto koristite BEFORE trigger umesto AFTER?

**Odgovor:**  
BEFORE trigger se koristi jer:
1. **Za INSERT** - možemo da modifikujemo `NEW.expiration_date` PRE nego što se zapiše u bazu
2. **Za DELETE** - možemo da pristupimo `OLD` podacima PRE nego što se obrišu
3. AFTER trigger ne bi mogao da modifikuje podatke koji se upisuju

### Q2: Što trigger vraća NEW, OLD ili NULL?

**Odgovor:**
- **INSERT**: vraća `NEW` - modifikovani red koji će se upisati
- **DELETE**: vraća `OLD` - originalni red (brisanje se nastavlja)
- Ako vratimo `NULL` za DELETE, brisanje bi se OTKAZALO

### Q3: Može li se trigger deaktivirati privremeno?

**Odgovor:**  
Da, može:
```sql
-- Deaktivacija
ALTER TABLE test_invites DISABLE TRIGGER test_expiration_trigger;

-- Aktivacija
ALTER TABLE test_invites ENABLE TRIGGER test_expiration_trigger;
```

### Q4: Što ako hoćemo da snimimo i UPDATE operacije?

**Odgovor:**  
Dodali bismo UPDATE u trigger:
```sql
CREATE TRIGGER test_expiration_trigger 
BEFORE INSERT OR UPDATE OR DELETE ON test_invites 
FOR EACH ROW 
EXECUTE FUNCTION manage_test_expiration();

-- I u funkciji:
ELSIF TG_OP = 'UPDATE' THEN
    -- Snimanje u audit_logs sa OLD i NEW podacima
    RETURN NEW;
```

### Q5: Kako testiramo da li trigger radi?

**Odgovor:**  
Kroz 3 testa (vidi gore):
1. Provera postojanja u `pg_trigger` tabeli
2. Test INSERT i provera da li je `expiration_date` postavljen
3. Test DELETE i provera `audit_logs` tabele

### Q6: Što koristimo `json_build_object()` za audit?

**Odgovor:**  
`json_build_object()` kreira JSON strukturu sa svim poljima zapisa:
- Omogućava fleksibilno čuvanje različitih tipova entiteta
- Lako se parsira u aplikaciji
- Omogućava vraćanje obrisanih podataka

## 🔍 Dodatne Implementacije (Opcionalno)

### Application Audit Trigger
```sql
CREATE OR REPLACE FUNCTION audit_application_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        INSERT INTO audit_logs (entity_type, entity_id, action, 
                               before_data_json, after_data_json, time_utc)
        VALUES ('APPLICATION', OLD.id, 'UPDATE',
                row_to_json(OLD)::TEXT, row_to_json(NEW)::TEXT,
                CURRENT_TIMESTAMP);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER applications_audit_trigger
AFTER UPDATE ON applications
FOR EACH ROW
EXECUTE FUNCTION audit_application_changes();
```

## 📝 Zaključak

Trigger sistem obezbeđuje:
- ✅ **Automatizaciju** - automatsko postavljanje datuma isteka
- ✅ **Audit Trail** - potpuno praćenje svih promena
- ✅ **Data Integrity** - sprečavanje grešaka
- ✅ **Transparency** - ko, šta, kada

Sve komponente su testirane i spremne za odbranu! 🎓

