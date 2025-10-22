# 🧪 KAKO TESTIRATI PL/SQL - KORAK PO KORAK

## 📋 PREDUSLOVI

### 1. Pokretanje SQL Skripti

**OPCIJA 1: Kroz psql (Preporučeno)**
```bash
# Otvorite Command Prompt ili PowerShell
# Navigirajte do SQL foldera
cd E:\IIS-ITCompany\IISProject\Backend\jpa\src\main\resources\sql

# Povežite se na bazu
psql -U postgres -d your_database_name

# U psql konzoli, pokrenite:
\i plsql_setup.sql
```

**OPCIJA 2: Kroz pgAdmin**
1. Otvorite pgAdmin
2. Povežite se na vašu bazu
3. Desni klik na bazu → Query Tool
4. Otvorite fajl `plsql_setup.sql`
5. Kliknite Execute (F5)

**OPCIJA 3: Pojedinačno izvršavanje**
```sql
-- Redosledom izvršite:
\i plsql_types.sql
\i plsql_functions.sql
\i plsql_triggers.sql
\i plsql_indexes.sql
\i plsql_report.sql
```

### 2. Provera da su skripte uspešno pokrenute

```sql
-- Provera tipova
\dT+ recruitment_metrics_type

-- Provera funkcija
\df calculate_recruitment_metrics

-- Provera trigera
SELECT tgname, tgrelid::regclass, tgenabled 
FROM pg_trigger 
WHERE tgname LIKE '%audit%';

-- Provera indeksa
SELECT indexname, tablename 
FROM pg_indexes 
WHERE schemaname = 'public' 
  AND tablename IN ('applications', 'application_status_history', 'offers')
ORDER BY tablename, indexname;
```

---

## 🚀 TESTIRANJE KROZ FRONTEND

### **KORAK 1: Pokretanje Aplikacije**

#### 1.1. Pokrenite Backend
```bash
cd E:\IIS-ITCompany\IISProject\Backend\jpa
mvn spring-boot:run
```

**Proverite da backend radi:**
- Otvorite browser: `http://localhost:8080`
- Trebalo bi da vidite Spring Boot poruku ili API

#### 1.2. Pokrenite Frontend
```bash
cd E:\IIS-FE\IIS_Projekat_FE
npm start
# ili
ng serve
```

**Proverite da frontend radi:**
- Otvorite browser: `http://localhost:4200`
- Trebalo bi da vidite vašu Angular aplikaciju

---

### **KORAK 2: Testiranje PL/SQL IZVEŠTAJA**

#### 2.1. Preko Frontend-a (Sada integrisano!)

1. **Prijavite se na aplikaciju** (kao HR ili Admin korisnik)

2. **Otvorite Report Modal:**
   - Kliknite na dugme "Generiši Izveštaj" ili gde god imate Report opciju

3. **Izaberite PL/SQL format:**
   - U dropdown-u "Format izveštaja" izaberite: **"PL/SQL Analitički Izveštaj (JSON)"**

4. **Izaberite period:**
   - **Opcija 1:** "Poslednjih 30 dana" (najbrže za testiranje)
   - **Opcija 2:** "Tekući mesec"
   - **Opcija 3:** "Tekuća godina"
   - **Opcija 4:** "Prilagođeni period" (izaberite datume)

5. **Kliknite "Generiši PL/SQL Izveštaj"**

6. **Proverite rezultate:**
   - Trebalo bi da vidite sekcije:
     - 📈 **Osnovne Metrike** (Total Applications, Total Hired, itd.)
     - 🔄 **Analiza po Fazama** (Stage Analysis)
     - 💼 **Analiza po Oglasima** (Job Posting Analysis)
     - ⚠️ **Detekcija Problema** (Stuck Applications)
     - 📊 **Performanse po Fazama** (Stage Performance)

7. **Preuzmite JSON:**
   - Kliknite "💾 Preuzmi kao JSON" da sačuvate izveštaj

8. **Otvorite Browser Console (F12):**
   - Trebalo bi da vidite: `PL/SQL Report Data: [...]` sa svim podacima

#### 2.2. Direktno preko Browser-a (bez frontend UI)

Otvorite novi tab i ukucajte:

```
http://localhost:8080/api/plsql-reports/last-30-days
```

**Trebalo bi da vidite JSON odgovor poput:**
```json
[
  {
    "reportSection": "BASIC_METRICS",
    "metricName": "Total Applications",
    "metricValue": 150,
    "additionalInfo": "Period: 2024-10-01 to 2024-10-15"
  },
  ...
]
```

**Drugi endpoint-i za testiranje:**
```
http://localhost:8080/api/plsql-reports/current-month
http://localhost:8080/api/plsql-reports/comprehensive?startDate=2024-01-01&endDate=2024-12-31
http://localhost:8080/api/plsql-reports/metrics?startDate=2024-01-01&endDate=2024-12-31
http://localhost:8080/api/plsql-reports/stage-performance?startDate=2024-01-01&endDate=2024-12-31
http://localhost:8080/api/plsql-reports/job-posting-summary?startDate=2024-01-01&endDate=2024-12-31
```

---

### **KORAK 3: Testiranje PL/SQL TRIGERA (Audit Log)**

Triggeri se automatski aktiviraju kada radite akcije u aplikaciji. Ne trebate da pišete testove!

#### 3.1. Test APPLICATIONS Trigera

**Scenario 1: Kreiranje aplikacije (INSERT)**
1. Prijavite se kao **Kandidat**
2. Pronađite aktivan oglas za posao
3. Kliknite "Apply" / "Apliciraj"
4. Popunite formu i pošaljite aplikaciju

**Provera u bazi:**
```sql
SELECT 
    id,
    action,
    entity_type,
    entity_id,
    before_data_json,
    after_data_json,
    time_utc
FROM audit_logs
WHERE entity_type = 'APPLICATION'
  AND action = 'INSERT'
ORDER BY time_utc DESC
LIMIT 5;
```

**ŠTA OČEKUJETE:**
- Novi red u `audit_logs` tabeli
- `action` = 'INSERT'
- `entity_type` = 'APPLICATION'
- `before_data_json` = null (jer je INSERT)
- `after_data_json` = JSON sa podacima nove aplikacije
- `time_utc` = vreme kada ste kreirali aplikaciju

**Scenario 2: Izmena statusa aplikacije (UPDATE)**
1. Prijavite se kao **HR**
2. Otvorite aplikaciju kandidata
3. Promenite status (npr. sa "PENDING" na "IN_PROGRESS")
4. Sačuvajte

**Provera u bazi:**
```sql
SELECT 
    action,
    entity_id,
    before_data_json->>'application_status' as old_status,
    after_data_json->>'application_status' as new_status,
    time_utc
FROM audit_logs
WHERE entity_type = 'APPLICATION'
  AND action = 'UPDATE'
ORDER BY time_utc DESC
LIMIT 5;
```

**ŠTA OČEKUJETE:**
- `action` = 'UPDATE'
- `old_status` = stari status (npr. "PENDING")
- `new_status` = novi status (npr. "IN_PROGRESS")
- `before_data_json` i `after_data_json` sadrže kompletne podatke

#### 3.2. Test OFFERS Trigera

**Scenario 1: Kreiranje ponude (INSERT)**
1. Prijavite se kao **HR**
2. Izaberite aplikaciju sa statusom koji dozvoljava kreiranje ponude
3. Kreirajte novu ponudu (Offer)
4. Popunite detalje (start date, salary, itd.)
5. Sačuvajte

**Provera u bazi:**
```sql
SELECT 
    action,
    entity_type,
    entity_id,
    after_data_json->>'offer_status' as status,
    after_data_json->>'start_date' as start_date,
    time_utc
FROM audit_logs
WHERE entity_type = 'OFFER'
  AND action = 'INSERT'
ORDER BY time_utc DESC
LIMIT 5;
```

**Scenario 2: Prihvatanje/Odbijanje ponude (UPDATE)**
1. Prijavite se kao **Kandidat**
2. Otvorite ponudu koja vam je poslata
3. Kliknite "Accept" ili "Decline"
4. Potvrdite

**Provera u bazi:**
```sql
SELECT 
    action,
    before_data_json->>'offer_status' as old_status,
    after_data_json->>'offer_status' as new_status,
    time_utc
FROM audit_logs
WHERE entity_type = 'OFFER'
  AND action = 'UPDATE'
ORDER BY time_utc DESC
LIMIT 5;
```

**ŠTA OČEKUJETE:**
- `old_status` = 'PENDING'
- `new_status` = 'ACCEPTED' ili 'DECLINED'

#### 3.3. Test STATUS_HISTORY Trigera

Status history se automatski kreira kada se menja status aplikacije.

**Scenario: Promena statusa**
1. Prijavite se kao **HR**
2. Promenite status neke aplikacije
3. Automatski će se kreirati zapis u `application_status_history`

**Provera u bazi:**
```sql
SELECT 
    action,
    entity_type,
    entity_id,
    after_data_json->>'stage_id' as stage_id,
    after_data_json->>'application_id' as application_id,
    time_utc
FROM audit_logs
WHERE entity_type = 'STATUS_HISTORY'
  AND action = 'INSERT'
ORDER BY time_utc DESC
LIMIT 5;
```

---

### **KORAK 4: Testiranje SQL INDEKSA**

#### 4.1. Preko Browser-a
```
http://localhost:8080/api/plsql-reports/performance-test
```

**Očekivani odgovor:**
```json
{
  "timeWithIndex": 5,
  "improvementPercentage": 0
}
```
- `timeWithIndex` treba biti mala vrednost (< 100ms)

#### 4.2. Direktno u SQL-u

**Test 1: Provera da se indeksi koriste**
```sql
EXPLAIN ANALYZE
SELECT COUNT(*) 
FROM applications a
JOIN application_status_history ash ON a.id = ash.application_id
WHERE a.application_status = 'HIRED' 
  AND ash.entered_at > NOW() - INTERVAL '30 days';
```

**ŠTA PROVERAVATE:**
- U rezultatu treba da vidite: **"Index Scan"** (DOBRO ✅)
- Ako vidite: **"Seq Scan"** (Loše ❌ - indeks se ne koristi)
- Execution Time treba biti < 50ms (za male baze)

**Test 2: Statistika indeksa**
```sql
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan as "Broj korišćenja",
    idx_tup_read as "Redova pročitano",
    idx_tup_fetch as "Redova vraćeno"
FROM pg_stat_user_indexes 
WHERE tablename IN ('applications', 'application_status_history', 'offers')
ORDER BY idx_scan DESC;
```

**ŠTA OČEKUJETE:**
- Indeksi sa najvećim brojem `idx_scan` su najčešće korišćeni
- Ako je `idx_scan` = 0, indeks se ne koristi (možda nije potreban)

---

## 📊 KREIRANJE TEST PODATAKA (Opciono)

Ako nemate dovoljno podataka za testiranje, možete kreirati test podatke:

### Opcija 1: Preko Browser-a
```
POST http://localhost:8080/api/plsql-reports/create-test-data
```

**Koristite Postman ili Thunder Client:**
1. Otvorite Postman/Thunder Client
2. Kreirajte novi POST zahtev
3. URL: `http://localhost:8080/api/plsql-reports/create-test-data`
4. Kliknite Send

**Opcija 2: Preko curl (Command Prompt)**
```bash
curl -X POST http://localhost:8080/api/plsql-reports/create-test-data
```

**ŠTA OVO RADI:**
- Kreira 100 test aplikacija
- Kreira test ponude
- Kreira status history zapise
- Kreira različite statuse (HIRED, REJECTED, IN_PROGRESS, itd.)

**⚠️ UPOZORENJE:** Ovo će dodati test podatke u vašu bazu!

---

## ✅ KOMPLETAN CHECKLIST TESTIRANJA

### PL/SQL Funkcije (preko frontend-a)
- [ ] Pokrenute su sve SQL skripte
- [ ] Backend radi na `http://localhost:8080`
- [ ] Frontend radi na `http://localhost:4200`
- [ ] Report modal ima opciju "PL/SQL Analitički Izveštaj"
- [ ] Mogu da generišem PL/SQL izveštaj za "Poslednjih 30 dana"
- [ ] Mogu da generišem PL/SQL izveštaj za "Tekući mesec"
- [ ] Mogu da generišem PL/SQL izveštaj za "Prilagođeni period"
- [ ] Vidim sekciju "Osnovne Metrike" u rezultatima
- [ ] Vidim sekciju "Analiza po Fazama" u rezultatima
- [ ] Vidim sekciju "Analiza po Oglasima" u rezultatima
- [ ] Mogu da preuzmem JSON fajl sa rezultatima

### PL/SQL Triggeri (automatsko testiranje kroz frontend)
- [ ] Kada apliciram na oglas, kreira se zapis u `audit_logs` (APPLICATION, INSERT)
- [ ] Kada HR promeni status aplikacije, kreira se zapis u `audit_logs` (APPLICATION, UPDATE)
- [ ] Kada HR kreira ponudu, kreira se zapis u `audit_logs` (OFFER, INSERT)
- [ ] Kada kandidat prihvati/odbije ponudu, kreira se zapis u `audit_logs` (OFFER, UPDATE)
- [ ] Kada se menja status, kreira se zapis u `audit_logs` (STATUS_HISTORY, INSERT)
- [ ] `before_data_json` i `after_data_json` su tačni

### SQL Indeksi
- [ ] Endpoint `/api/plsql-reports/performance-test` vraća vreme < 100ms
- [ ] EXPLAIN ANALYZE pokazuje "Index Scan" (ne "Seq Scan")
- [ ] `pg_stat_user_indexes` pokazuje da se indeksi koriste

### Kompleksan Izveštaj (sve komponente)
- [ ] Koristi PL/SQL tipove (recruitment_metrics_type, itd.)
- [ ] Koristi kursore (applications_cursor, stages_cursor)
- [ ] Koristi WITH klauzule (CTE)
- [ ] Koristi složene SQL upite (4+ tabele JOIN)
- [ ] Koristi agregacione funkcije (COUNT, AVG, SUM)
- [ ] Koristi GROUP BY i HAVING

---

## 🐛 ŠTA AKO NEŠTO NE RADI?

### Problem: "Function does not exist"
```bash
# Rešenje: Pokrenite ponovo skripte
cd E:\IIS-ITCompany\IISProject\Backend\jpa\src\main\resources\sql
psql -U postgres -d your_database_name
\i plsql_setup.sql
```

### Problem: "No data in report"
```bash
# Rešenje: Kreirajte test podatke
curl -X POST http://localhost:8080/api/plsql-reports/create-test-data

# ILI kroz frontend:
# 1. Aplicirajte na nekoliko oglasa
# 2. Kreirajte ponude
# 3. Menjajte statuse aplikacija
```

### Problem: "Trigger doesn't fire"
```sql
-- Proverite da li je trigger aktivan
SELECT tgname, tgrelid::regclass, tgenabled 
FROM pg_trigger 
WHERE tgname LIKE '%audit%';

-- Ako nije aktivan:
\i plsql_triggers.sql
```

### Problem: "Index not used"
```sql
-- Proverite da li postoje indeksi
SELECT indexname, tablename 
FROM pg_indexes 
WHERE schemaname = 'public';

-- Ponovo kreirajte indekse
\i plsql_indexes.sql

-- Analizirajte tabele
ANALYZE applications;
ANALYZE application_status_history;
ANALYZE offers;
```

### Problem: "CORS error" u browser-u
```java
// Proverite da li imate CORS config u Spring Boot-u
// Backend treba da ima:
@CrossOrigin(origins = "http://localhost:4200")
```

---

## 📝 PRIMER AUDIT LOG PROVERE

Nakon što ste radili akcije u aplikaciji:

```sql
-- Pregled svih audit logova iz poslednjih 24h
SELECT 
    id,
    action,
    entity_type,
    entity_id,
    time_utc,
    source
FROM audit_logs
WHERE time_utc > NOW() - INTERVAL '24 hours'
ORDER BY time_utc DESC;

-- Filtriranje po tipu akcije
SELECT 
    entity_type,
    action,
    COUNT(*) as broj_akcija
FROM audit_logs
WHERE time_utc > NOW() - INTERVAL '24 hours'
GROUP BY entity_type, action
ORDER BY broj_akcija DESC;

-- Detaljan pregled jedne akcije
SELECT 
    id,
    action,
    entity_type,
    entity_id,
    before_data_json::jsonb as "Staro stanje",
    after_data_json::jsonb as "Novo stanje",
    time_utc
FROM audit_logs
WHERE entity_type = 'APPLICATION'
  AND action = 'UPDATE'
ORDER BY time_utc DESC
LIMIT 1;
```

---

## 🎯 ZAVRŠNI KORACI

1. ✅ Pokrenite SQL skripte
2. ✅ Pokrenite Backend i Frontend
3. ✅ Testirajte PL/SQL izveštaj kroz Report Modal
4. ✅ Radite normalne akcije u aplikaciji (apply, create offer, change status)
5. ✅ Proverite `audit_logs` tabelu
6. ✅ Testirajte performanse indeksa
7. ✅ Preuzmite JSON izveštaje
8. ✅ (Opciono) Kreirajte screenshot-ove rezultata

**GOTOVO! 🎉**

Sve je testirano bez pisanja ijednog test fajla - sve kroz frontend i SQL provere!

