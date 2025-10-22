# 🧪 KOMPLETAN PLAN TESTIRANJA PL/SQL IMPLEMENTACIJE

## 📌 PRIPREMA

### 1. Pokretanje SQL Skripti

Prvo se povežite na PostgreSQL bazu i pokrenite setup skript:

```bash
# Navigirajte do SQL foldera
cd E:\IIS-ITCompany\IISProject\Backend\jpa\src\main\resources\sql

# Pokrenite psql i povežite se na vašu bazu
psql -U your_username -d your_database_name

# U psql konzoli, pokrenite:
\i plsql_setup.sql
```

**ALTERNATIVA** - Pokrenite skripte pojedinačno (istim redosledom):
```sql
\i plsql_types.sql
\i plsql_functions.sql  
\i plsql_triggers.sql
\i plsql_indexes.sql
\i plsql_report.sql
```

### 2. Provera da su skripte uspešno pokrenute

```sql
-- Provera tipova
\dT recruitment_metrics_type

-- Provera funkcija
\df calculate_recruitment_metrics
\df generate_comprehensive_recruitment_report

-- Provera trigera
SELECT tgname FROM pg_trigger WHERE tgname LIKE '%audit%';

-- Provera indeksa
SELECT indexname FROM pg_indexes WHERE tablename = 'applications';
```

---

## 🧪 TESTIRANJE KOMPONENTI

### **TEST 1: PL/SQL IZVEŠTAJI (Preko Fronta)**

#### 1.1. Pokrenite Backend
```bash
cd E:\IIS-ITCompany\IISProject\Backend\jpa
mvn spring-boot:run
```

#### 1.2. Pokrenite Frontend
```bash
cd E:\IIS-FE\IIS_Projekat_FE
npm start
```

#### 1.3. Testiranje u pregledaču

**OPCIJA A: Kroz modifikovani Report Modal** (ako ste implementirali integraciju - vidite ispod)

1. Otvorite aplikaciju u pregledaču
2. Kliknite na "Generiši Izveštaj" ili gde god imate Report Modal
3. Izaberite "PL/SQL Izveštaj" opciju
4. Unesite datume (npr. poslednji mesec)
5. Kliknite "Generiši"
6. Proverite konzolu pregledača za JSON odgovor

**OPCIJA B: Direktno preko HTTP klijenata (Postman/Thunder Client/Browser)**

Testirajte sledeće endpoint-e:

```http
### 1. Kompleksan izveštaj - poslednjih 30 dana
GET http://localhost:8080/api/plsql-reports/last-30-days

### 2. Kompleksan izveštaj - tekući mesec  
GET http://localhost:8080/api/plsql-reports/current-month

### 3. Kompleksan izveštaj - prilagođeni period
GET http://localhost:8080/api/plsql-reports/comprehensive?startDate=2024-01-01&endDate=2024-12-31

### 4. Osnovne metrike
GET http://localhost:8080/api/plsql-reports/metrics?startDate=2024-01-01&endDate=2024-12-31

### 5. Performanse po fazama
GET http://localhost:8080/api/plsql-reports/stage-performance?startDate=2024-01-01&endDate=2024-12-31

### 6. Sumarni podaci po oglasima
GET http://localhost:8080/api/plsql-reports/job-posting-summary?startDate=2024-01-01&endDate=2024-12-31

### 7. Test performansi indeksa
GET http://localhost:8080/api/plsql-reports/performance-test
```

**ŠTA PROVERAVATE:**
- ✅ Da li vraća JSON sa metrikama
- ✅ Da li su vrednosti logične (broj aplikacija, zaposlenih, procenat, itd.)
- ✅ Da li ima različite sekcije (BASIC_METRICS, STAGE_ANALYSIS, JOB_POSTING_ANALYSIS, itd.)

---

### **TEST 2: PL/SQL TRIGGERI (Audit Log)**

Triggeri se automatski aktiviraju kada radite INSERT/UPDATE/DELETE na tabelama. Testirajte kroz normalan rad aplikacije:

#### 2.1. Testiranje APPLICATIONS trigera

**Kroz Frontend:**
1. Prijavite se na aplikaciju kao kandidat
2. Aplicirajte na neki oglas za posao (INSERT operacija)
3. HR menja status vaše aplikacije (UPDATE operacija)

**Provera u bazi:**
```sql
-- Proverite audit_logs tabelu
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
ORDER BY time_utc DESC
LIMIT 10;
```

**ŠTA PROVERAVATE:**
- ✅ Da li se INSERT zapisao u audit_logs (action = 'INSERT')
- ✅ Da li se UPDATE zapisao (action = 'UPDATE')
- ✅ Da li `before_data_json` i `after_data_json` sadrže tačne podatke
- ✅ Da li je `time_utc` tačan

#### 2.2. Testiranje OFFERS trigera

**Kroz Frontend:**
1. HR kreira ponudu za kandidata (INSERT operacija)
2. Kandidat prihvata/odbija ponudu (UPDATE operacija)

**Provera u bazi:**
```sql
SELECT 
    action,
    entity_type,
    entity_id,
    before_data_json,
    after_data_json,
    time_utc
FROM audit_logs
WHERE entity_type = 'OFFER'
ORDER BY time_utc DESC
LIMIT 10;
```

#### 2.3. Testiranje STATUS_HISTORY trigera

**Kroz Frontend:**
1. Kada se menja status aplikacije (automatski se pravi zapis u application_status_history)

**Provera u bazi:**
```sql
SELECT 
    action,
    entity_type,
    entity_id,
    before_data_json,
    after_data_json,
    time_utc
FROM audit_logs
WHERE entity_type = 'STATUS_HISTORY'
ORDER BY time_utc DESC
LIMIT 10;
```

---

### **TEST 3: SQL INDEKSI (Performanse)**

#### 3.1. Preko Backend Endpoint-a
```http
GET http://localhost:8080/api/plsql-reports/performance-test
```

**Očekivani rezultat:**
```json
{
  "timeWithIndex": 5,
  "improvementPercentage": 0
}
```
- `timeWithIndex` treba biti mala vrednost (< 100ms)

#### 3.2. Direktno u SQL-u

**Provera korišćenja indeksa:**
```sql
-- Provera da li se indeksi koriste
EXPLAIN ANALYZE
SELECT COUNT(*) 
FROM applications a
JOIN application_status_history ash ON a.id = ash.application_id
WHERE a.application_status = 'HIRED' 
  AND ash.entered_at > NOW() - INTERVAL '30 days';
```

**ŠTA PROVERAVATE:**
- ✅ Da li u EXPLAIN ANALYZE piše "Index Scan" (a ne "Seq Scan")
- ✅ Da li je vreme izvršenja brzo (< 50ms za male baze)

**Provera statistike indeksa:**
```sql
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes 
WHERE tablename IN ('applications', 'application_status_history', 'offers')
ORDER BY idx_scan DESC;
```

---

### **TEST 4: Kreiranje Test Podataka (Opciono)**

Ako nemate dovoljno podataka za testiranje, možete kreirati test podatke:

```http
POST http://localhost:8080/api/plsql-reports/create-test-data
```

**UPOZORENJE:** Ovo će kreirati 100 test aplikacija, ponuda i status zapisa!

---

## ✅ CHECKLIST TESTIRANJA

### PL/SQL Funkcije
- [ ] `calculate_recruitment_metrics` - Vraća osnovne metrike
- [ ] `calculate_stage_performance` - Vraća performanse po fazama  
- [ ] `calculate_job_posting_summary` - Vraća sumarne podatke po oglasima

### PL/SQL Kompleksan Izveštaj
- [ ] `generate_comprehensive_recruitment_report` - Vraća kompletan izveštaj
- [ ] Izveštaj sadrži sekciju BASIC_METRICS
- [ ] Izveštaj sadrži sekciju STAGE_ANALYSIS  
- [ ] Izveštaj sadrži sekciju JOB_POSTING_ANALYSIS
- [ ] Izveštaj sadrži sekciju PROBLEM_DETECTION
- [ ] Izveštaj sadrži sekciju STAGE_PERFORMANCE
- [ ] Izveštaj sadrži sekciju SUMMARY

### PL/SQL Triggeri
- [ ] Trigger za applications radi (INSERT)
- [ ] Trigger za applications radi (UPDATE)
- [ ] Trigger za applications radi (DELETE)
- [ ] Trigger za offers radi (INSERT)
- [ ] Trigger za offers radi (UPDATE)
- [ ] Trigger za status_history radi (INSERT)
- [ ] Svi trigeri snimaju podatke u `audit_logs`
- [ ] `before_data_json` i `after_data_json` su tačni

### SQL Indeksi
- [ ] Indeksi su kreirani
- [ ] Indeksi se koriste u upitima (EXPLAIN ANALYZE pokazuje Index Scan)
- [ ] Performanse su bolje sa indeksima

### PL/SQL Tipovi
- [ ] `recruitment_metrics_type` je kreiran
- [ ] `stage_performance_type` je kreiran
- [ ] `job_posting_summary_type` je kreiran
- [ ] `audit_data_type` je kreiran

### PL/SQL Kursori i WITH klauzule
- [ ] Kompleksan izveštaj koristi kursore (`applications_cursor`, `stages_cursor`)
- [ ] Kompleksan izveštaj koristi WITH klauzule (CTE)
- [ ] Kompleksan izveštaj koristi složene SQL upite (4+ tabele JOIN)

---

## 🐛 TROUBLESHOOTING

### Problem: "Function does not exist"
**Rešenje:** Pokrenite ponovo `plsql_setup.sql`

### Problem: "Type does not exist"  
**Rešenje:** Pokrenite ponovo `plsql_types.sql` pa onda ostale skripte

### Problem: "Trigger doesn't fire"
**Rešenje:** 
```sql
-- Provera da li je trigger aktivan
SELECT * FROM pg_trigger WHERE tgname LIKE '%audit%';

-- Ponovo kreirajte trigger
\i plsql_triggers.sql
```

### Problem: "No data in report"
**Rešenje:** Kreirajte test podatke:
```http
POST http://localhost:8080/api/plsql-reports/create-test-data
```

---

## 📊 PRIMER REZULTATA

### Kompleksan izveštaj (JSON odgovor):
```json
[
  {
    "reportSection": "BASIC_METRICS",
    "metricName": "Total Applications",
    "metricValue": 150,
    "additionalInfo": "Period: 2024-01-01 to 2024-12-31"
  },
  {
    "reportSection": "BASIC_METRICS",
    "metricName": "Total Hired",
    "metricValue": 25,
    "additionalInfo": "Success Rate: 16.67%"
  },
  {
    "reportSection": "STAGE_ANALYSIS",
    "metricName": "Application Review",
    "metricValue": 5.5,
    "additionalInfo": "Entries: 150, Completed: 100, Conversion: 66.67%, Status: NORMAL"
  }
]
```

### Audit log (SQL rezultat):
```
 id | action |  entity_type  | entity_id | before_data_json | after_data_json |        time_utc         
----+--------+---------------+-----------+------------------+-----------------+-------------------------
  1 | INSERT | APPLICATION   |       123 | null             | {"id":123,...}  | 2024-10-15 10:30:00+00
  2 | UPDATE | APPLICATION   |       123 | {"status":...}   | {"status":...}  | 2024-10-15 11:00:00+00
```

---

## 🎯 ZAKLJUČAK

Nakon testiranja, treba da imate:
1. ✅ Funkcionalan PL/SQL kompleksan izveštaj sa svim metrikama
2. ✅ Radne triggere koji automatski snimaju audit log
3. ✅ Indekse koji ubrzavaju upite
4. ✅ REST API endpoint-e koji vraćaju PL/SQL podatke
5. ✅ (Opciono) Frontend integraciju za prikaz PL/SQL izveštaja

**NAPOMENA:** Frontend integracija je opciona - možete testirati sve preko Postman/Thunder Client/Browser konzole!

