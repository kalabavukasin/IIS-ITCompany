# PL/SQL Komponente - Sistem za Zapošljavanje

## 📋 Pregled

Ovaj direktorijum sadrži sve PL/SQL komponente za sistem upravljanja zapošljavanjem, uključujući funkcije, trigere, indekse i kompleksne izvještaje.

## 📁 Struktura Fajlova

```
sql/
├── plsql_types.sql              # Custom tipovi podataka (4 tipa)
├── plsql_functions.sql          # Funkcije za metrike i analizu (4 funkcije)
├── plsql_report.sql             # Kompleksna report funkcija (kursori, WITH)
├── plsql_triggers.sql           # Trigeri za audit i automatizaciju
├── plsql_indexes.sql            # Indeksi za optimizaciju performansi (7 indeksa)
├── plsql_setup.sql              # Inicijalizaciona skripta
├── test_plsql_components.sql    # Test skripte
└── cleanup_old_triggers.sql     # Skripta za čišćenje
```

## 🚀 Brzo Pokretanje

### 1. Instalacija Svih Komponenti

```bash
# U psql konzoli
\i sql/plsql_setup.sql

# Ili iz command line-a
psql -U username -d database_name -f sql/plsql_setup.sql
```

### 2. Testiranje Instalacije

```bash
\i sql/test_plsql_components.sql
```

### 3. Čišćenje Pre Reinstalacije

```bash
\i sql/cleanup_old_triggers.sql
```

## 📊 Komponente

### 1. Custom Tipovi (`plsql_types.sql`)

#### `recruitment_metrics_type`
Osnovne metrike performansi procesa zapošljavanja
```sql
- total_applications: BIGINT
- total_hired: BIGINT
- average_time_to_hire: NUMERIC
- offer_rejection_percentage: NUMERIC
- invitation_rejection_ratio: NUMERIC
- report_start_date: DATE
- report_end_date: DATE
```

#### `job_posting_summary_type`
Sumarni podaci po oglasima za posao
```sql
- job_posting_id: BIGINT
- job_posting_name: VARCHAR(255)
- total_applications: BIGINT
- hired_count: BIGINT
- rejection_count: BIGINT
- average_processing_days: NUMERIC
- success_rate: NUMERIC
```

#### `stage_performance_type`
Metrike performansi po fazama workflow-a
```sql
- stage_name: VARCHAR(255)
- entered_count: BIGINT
- completed_count: BIGINT
- conversion_rate: NUMERIC
- average_time_in_days: NUMERIC
- total_applications_in_stage: BIGINT
```

#### `audit_data_type`
Audit log podaci
```sql
- user_id: BIGINT
- action: VARCHAR(100)
- entity_type: VARCHAR(100)
- entity_id: BIGINT
- before_data_json: TEXT
- after_data_json: TEXT
- time_utc: TIMESTAMP WITH TIME ZONE
```

### 2. Funkcije (`plsql_functions.sql`)

#### `calculate_recruitment_metrics(start_date, end_date, job_posting_id)`
Izračunava kompleksne metrike performansi za zadati period.

**Primer:**
```sql
SELECT * FROM calculate_recruitment_metrics(
    '2024-01-01'::DATE,
    '2024-12-31'::DATE,
    NULL  -- svi oglasi
);
```

#### `calculate_job_posting_summary(start_date, end_date)`
Vraća sumarne metrike za sve job posting-e.

**Primer:**
```sql
SELECT * FROM calculate_job_posting_summary(
    '2024-01-01'::DATE,
    '2024-12-31'::DATE
)
ORDER BY total_applications DESC;
```

#### `calculate_stage_performance(start_date, end_date)`
Analizira performanse po fazama workflow-a.

**Primer:**
```sql
SELECT * FROM calculate_stage_performance(
    '2024-01-01'::DATE,
    '2024-12-31'::DATE
)
ORDER BY conversion_rate DESC;
```

### 3. Report Funkcija (`plsql_report.sql`)

#### `generate_comprehensive_recruitment_report(start_date, end_date)`
Kompleksna funkcija koja koristi:
- ✅ **Kursore** za iteraciju kroz aplikacije i faze
- ✅ **WITH klauzule** za identifikaciju bottleneck-a
- ✅ **GROUP BY** za grupisanje po kategorijama
- ✅ **HAVING** za filtriranje rezultata
- ✅ **Složene SQL upite** sa multiple JOIN-ovima

**Primer:**
```sql
SELECT 
    report_section,
    metric_name,
    ROUND(metric_value, 2) as value,
    additional_info
FROM generate_comprehensive_recruitment_report(
    '2024-01-01'::DATE,
    '2024-12-31'::DATE
)
WHERE report_section = 'BASIC_METRICS';
```

**Sekcije izvještaja:**
- `BASIC_METRICS` - Osnovne metrike
- `STAGE_ANALYSIS` - Analiza po fazama sa bottleneck detekcijom
- `JOB_POSTING_ANALYSIS` - Analiza po oglasima za posao
- `PROBLEM_DETECTION` - Detekcija problema (zaglavljene aplikacije)
- `STAGE_PERFORMANCE` - Detaljne performanse po fazama
- `SUMMARY` - Sumarni pregled

### 4. Trigeri (`plsql_triggers.sql`)

#### `test_expiration_trigger`
Automatski upravlja test pozivima:
- Postavlja datum isteka pri kreiranju (7 dana)
- Snima audit log pri brisanju

**Tabela:** `test_invites`  
**Tip:** `BEFORE INSERT OR DELETE`

### 5. Indeksi (`plsql_indexes.sql`)

Ukupno **7 indeksa** za ubrzanje upita:

#### Na tabeli `application_status_history`:
- `idx_status_history_application` - brzo pronalaženje po aplikaciji
- `idx_status_history_entered` - filtriranje po datumu ulaska
- `idx_status_history_exited` - filtriranje po datumu izlaska

#### Na tabeli `applications`:
- `idx_applications_applied_at` - filtriranje po datumu prijave
- `idx_applications_status_date` - kompozitni (status + datum)

#### Na tabeli `offers`:
- `idx_offers_application` - pronalaženje ponuda po aplikaciji
- `idx_offers_created` - filtriranje po datumu kreiranja

## 🧪 Testiranje

### Pokretanje Svih Testova
```bash
\i sql/test_plsql_components.sql
```

### Individualni Testovi

**Test osnovnih metrika:**
```sql
SELECT 
    (result).total_applications,
    (result).total_hired,
    (result).average_time_to_hire
FROM (
    SELECT calculate_recruitment_metrics(
        CURRENT_DATE - INTERVAL '30 days',
        CURRENT_DATE,
        NULL
    ) as result
) test;
```

**Test performansi indeksa:**
```sql
EXPLAIN ANALYZE
SELECT * FROM applications 
WHERE application_status = 'HIRED' 
  AND applied_at >= CURRENT_DATE - INTERVAL '90 days';
```

## 🔧 Održavanje

### Reinstalacija Komponenti
```bash
# 1. Očisti stare komponente
\i sql/cleanup_old_triggers.sql

# 2. Instaliraj nove
\i sql/plsql_setup.sql

# 3. Testiraj
\i sql/test_plsql_components.sql
```

### Provera Performansi Indeksa
```sql
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan as "Index Scans",
    idx_tup_read as "Tuples Read"
FROM pg_stat_user_indexes 
WHERE schemaname = 'public'
  AND indexname LIKE 'idx_%'
ORDER BY idx_scan DESC;
```

### Monitoring Korišćenja Funkcija
```sql
SELECT 
    proname as function_name,
    prokind,
    pronargs as num_arguments,
    prorettype::regtype as return_type
FROM pg_proc p
WHERE p.pronamespace = 'public'::regnamespace
  AND p.prolang = (SELECT oid FROM pg_language WHERE lanname = 'plpgsql')
ORDER BY proname;
```

## 📈 Primeri Korišćenja

### 1. Generisanje Mesečnog Izvještaja
```sql
SELECT 
    report_section,
    metric_name,
    ROUND(metric_value, 2) as value
FROM generate_comprehensive_recruitment_report(
    date_trunc('month', CURRENT_DATE)::DATE,
    CURRENT_DATE
)
ORDER BY report_section, metric_name;
```

### 2. Identifikacija Najsporijih Faza
```sql
SELECT 
    stage_name,
    ROUND(average_time_in_days, 2) as avg_days,
    ROUND(conversion_rate, 2) as conversion_pct
FROM calculate_stage_performance(
    CURRENT_DATE - INTERVAL '90 days',
    CURRENT_DATE
)
WHERE conversion_rate < 50  -- Faze sa niskom konverzijom
ORDER BY average_time_in_days DESC;
```

### 3. Top 5 Najuspešnijih Oglasa
```sql
SELECT 
    job_posting_name,
    total_applications,
    hired_count,
    ROUND(success_rate, 2) as success_pct
FROM calculate_job_posting_summary(
    CURRENT_DATE - INTERVAL '180 days',
    CURRENT_DATE
)
WHERE total_applications >= 5
ORDER BY success_rate DESC
LIMIT 5;
```

## 🐛 Troubleshooting

### Problem: Funkcije ne postoje
```sql
-- Provera da li su funkcije kreirane
SELECT proname FROM pg_proc 
WHERE pronamespace = 'public'::regnamespace 
  AND proname LIKE 'calculate%';

-- Rešenje: Ponovo pokreni plsql_setup.sql
```

### Problem: Spori upiti
```sql
-- Provera da li se indeksi koriste
EXPLAIN ANALYZE SELECT ...;

-- Trebalo bi da vidiš "Index Scan using idx_..."
-- Ako ne, možda su indeksi obrisani
```

### Problem: Trigeri ne rade
```sql
-- Provera trigera
SELECT tgname, tgtype FROM pg_trigger t
JOIN pg_class c ON t.tgrelid = c.oid
WHERE c.relname = 'test_invites';

-- Rešenje: Ponovo pokreni plsql_triggers.sql
```

## 📚 Dodatna Dokumentacija

- `INTEGRATION_GUIDE.md` - Vodič za integraciju sa Spring Boot aplikacijom
- `PL_SQL_Specifikacija.md` - Detaljna specifikacija projekta
- `ODBRANA_TRIGGERA.md` - Dokumentacija za odbranu trigera

## ✅ Checklist za Odbranu

- [x] 4 Custom tipa podataka
- [x] 4 Osnovne funkcije za metrike
- [x] 1 Kompleksna report funkcija sa kursorima i WITH klauzulama
- [x] 1+ Trigera za automatizaciju
- [x] 7 Indeksa za optimizaciju
- [x] Kompletan izvještaj sa svim PL/SQL tehnikama
- [x] Test skripte za verifikaciju
- [x] Dokumentacija i primeri

## 🎓 Za Profesora

Sve PL/SQL komponente su implementirane prema specifikaciji:

1. **Trigeri** - Automatsko upravljanje test pozivima i audit
2. **Funkcije** - Složene kalkulacije i metrike
3. **Indeksi** - Optimizacija performansi upita
4. **Izvještaj** - Koristi kursore, WITH, GROUP BY, HAVING, složene JOIN-ove

Sve komponente su testirane i spremne za odbranu! 🚀

