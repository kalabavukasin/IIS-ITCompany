# PL/SQL Integracija sa Postojećim Kodom

Ovaj dokument objašnjava kako su PL/SQL komponente integrisane sa vašim postojećim JPA modelima i servisima.

## 🔗 Povezanost sa Postojećim Kodom

### 1. **JPA Modeli ↔ PL/SQL Tabele**

PL/SQL komponente rade direktno sa vašim postojećim tabelama:

| JPA Entitet | Tabela | PL/SQL Upotreba |
|-------------|--------|-----------------|
| `Application` | `applications` | ✅ Triggeri, funkcije, indeksi |
| `Offer` | `offers` | ✅ Triggeri, funkcije, indeksi |
| `ApplicationStatusHistory` | `application_status_history` | ✅ Triggeri, funkcije, indeksi |
| `JobPosting` | `job_postings` | ✅ Funkcije, indeksi |
| `Requestion` | `requestions` | ✅ Funkcije, indeksi |
| `User` | `users` | ✅ Indeksi |
| `AuditLog` | `audit_logs` | ✅ Triggeri snimaju podatke |
| `WorkflowStage` | `workflow_stages` | ✅ Funkcije, indeksi |
| `WorkflowDef` | `workflow_defs` | ✅ Funkcije, indeksi |

### 2. **Servisi Integracija**

#### `ReportService` - Proširen sa PL/SQL metodama:

```java
// Originalna metoda (JPA)
public ReportDTO generateReport(LocalDate startDate, LocalDate endDate)

// Nova PL/SQL metoda
public ReportDTO generateReportWithPlSql(LocalDate startDate, LocalDate endDate)

// Kompleksan PL/SQL izveštaj
public List<Map<String, Object>> generateComprehensivePlSqlReport(LocalDate startDate, LocalDate endDate)
```

#### `ReportController` - Novi endpointi:

```http
# Originalni endpointi (JPA)
GET /api/reports/pdf?startDate=2024-01-01&endDate=2024-12-31

# Novi PL/SQL endpointi
GET /api/reports/plsql?startDate=2024-01-01&endDate=2024-12-31
GET /api/reports/plsql/comprehensive?startDate=2024-01-01&endDate=2024-12-31
GET /api/reports/plsql/performance-test
POST /api/reports/plsql/set-current-user/1
```

### 3. **Automatska Inicijalizacija**

`PlSqlInitializationService` automatski pokreće sve PL/SQL komponente prilikom pokretanja aplikacije.

## 🚀 Kako Koristiti

### 1. **Pokretanje Aplikacije**

```bash
# Aplikacija automatski pokreće PL/SQL komponente
mvn spring-boot:run
```

### 2. **Testiranje Integracije**

```http
# Test osnovnog izveštaja (JPA)
GET /api/reports/pdf?startDate=2024-01-01&endDate=2024-12-31

# Test PL/SQL izveštaja
GET /api/reports/plsql?startDate=2024-01-01&endDate=2024-12-31

# Test kompleksnog PL/SQL izveštaja
GET /api/reports/plsql/comprehensive?startDate=2024-01-01&endDate=2024-12-31
```

### 3. **Poređenje Rezultata**

Možete porediti rezultate između JPA i PL/SQL implementacije:

```java
// JPA implementacija
ReportDTO jpaReport = reportService.generateReport(startDate, endDate);

// PL/SQL implementacija  
ReportDTO plsqlReport = reportService.generateReportWithPlSql(startDate, endDate);

// Rezultati treba da budu identični!
```

## 🔧 Tehnička Integracija

### 1. **Triggeri**

Triggeri se automatski aktiviraju kada se menjaju vaši JPA entiteti:

```java
// Kada se ažurira Application kroz JPA
applicationRepository.save(application);
// → Automatski se pokreće applications_audit_trigger
// → Snima se u audit_logs tabelu
```

### 2. **Funkcije**

PL/SQL funkcije rade sa istim podacima kao vaši JPA upiti:

```java
// JPA upit
List<Application> applications = applicationRepository.findApplicationsByDateRange(startDate, endDate);

// PL/SQL funkcija (isti podaci)
Map<String, Object> metrics = jdbcTemplate.queryForMap(
    "SELECT * FROM calculate_recruitment_metrics(?, ?, ?)", 
    startDate, endDate, null
);
```

### 3. **Indeksi**

Indeksi ubrzavaju vaše postojeće JPA upite:

```java
// Ovaj JPA upit će biti brži zbog indeksa
List<Application> applications = applicationRepository.findApplicationsByStatusAndDateRange(
    ApplicationStatus.HIRED, startDate, endDate
);
```

## 📊 Audit Log Integracija

### Automatsko Snimanje

Sve promene u vašim entitetima se automatski snimaju:

```java
// Ažuriranje aplikacije
Application app = applicationRepository.findById(1L).orElse(null);
app.setStatus(ApplicationStatus.HIRED);
applicationRepository.save(app);

// Automatski se kreira audit log:
// {
//   "entityType": "APPLICATION",
//   "action": "UPDATE", 
//   "entityId": 1,
//   "beforeDataJson": "{...}",
//   "afterDataJson": "{...}",
//   "timeUtc": "2024-01-01T10:00:00Z"
// }
```

### Pregled Audit Logova

```java
// Pregled kroz JPA
List<AuditLog> logs = auditLogRepository.findByEntityType("APPLICATION");

// Ili direktno SQL
List<Map<String, Object>> logs = jdbcTemplate.queryForList(
    "SELECT * FROM audit_logs WHERE entity_type = 'APPLICATION'"
);
```

## 🎯 Prednosti Integracije

### 1. **Performanse**
- Indeksi ubrzavaju vaše postojeće upite
- PL/SQL funkcije su optimizovane za složene kalkulacije
- Manje mrežnog saobraćaja (kalkulacije u bazi)

### 2. **Audit Trail**
- Automatsko snimanje svih promena
- Detaljni JSON podaci o prethodnim i novim stanjima
- Praćenje ko je napravio promene

### 3. **Kompatibilnost**
- Postojeći kod radi bez promena
- PL/SQL komponente su dodatak, ne zamena
- Fallback na JPA ako PL/SQL ne radi

### 4. **Fleksibilnost**
- Možete koristiti JPA ili PL/SQL prema potrebi
- Lako dodavanje novih PL/SQL funkcija
- Mogućnost poređenja rezultata

## 🐛 Troubleshooting

### Problem: PL/SQL funkcije ne rade
**Rešenje:** Proverite da li su skriptovi pokrenuti:
```sql
-- Proverite da li postoje funkcije
\df calculate_recruitment_metrics
```

### Problem: Triggeri ne snimaju podatke
**Rešenje:** Postavite user_id u session:
```http
POST /api/reports/plsql/set-current-user/1
```

### Problem: Spori upiti
**Rešenje:** Proverite da li su indeksi kreirani:
```sql
-- Proverite indekse
\d applications
```

## 📈 Monitoring

### Pregled Performansi

```http
GET /api/reports/plsql/performance-test
```

### Pregled Audit Logova

```sql
-- Poslednje promene
SELECT * FROM audit_logs 
WHERE time_utc > NOW() - INTERVAL '1 hour'
ORDER BY time_utc DESC;
```

### Pregled Korišćenja Indeksa

```sql
-- Korišćenje indeksa
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read
FROM pg_stat_user_indexes 
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;
```

## 🎉 Zaključak

PL/SQL komponente su potpuno integrisane sa vašim postojećim kodom:

- ✅ Koriste iste tabele kao JPA entiteti
- ✅ Automatski se pokreću prilikom startovanja
- ✅ Dodaju nove funkcionalnosti bez menjanja postojećeg koda
- ✅ Pružaju audit trail i bolje performanse
- ✅ Mogu se koristiti paralelno sa JPA implementacijom

Sve je spremno za korišćenje! 🚀
