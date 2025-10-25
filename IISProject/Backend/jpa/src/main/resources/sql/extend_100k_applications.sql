-- =====================================================
-- SKRIPT ZA DODAVANJE 100,000 NOVIH APLIKACIJA
-- =====================================================
-- Ovaj skript se nadovezuje na postojeće podatke i dodaje 100,000 novih aplikacija

-- =====================================================
-- 1. PROVERA TRENUTNOG STANJA
-- =====================================================
SELECT 'Provera trenutnog stanja...' as status;

-- Broj zapisa po tabeli pre dodavanja
SELECT 
    'Applications' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('applications')) as table_size
FROM applications
UNION ALL
SELECT 
    'Application Status History' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('application_status_history')) as table_size
FROM application_status_history
UNION ALL
SELECT 
    'Offers' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('offers')) as table_size
FROM offers;

-- =====================================================
-- 2. DODAVANJE DODATNIH CANDIDATE PROFILA
-- =====================================================
SELECT 'Dodavanje dodatnih candidate profila...' as status;

-- Postavljamo sequence na trenutnu vrednost
SELECT setval('candidate_profiles_id_seq', (SELECT MAX(id) FROM candidate_profiles));

-- Dodajemo 20,000 novih kandidata
INSERT INTO candidate_profiles (first_name, last_name, email, phone, cv_path, created_at)
SELECT 
    'Candidate' || s as first_name,
    'LastName' || s as last_name,
    'candidate' || s || '@example.com' as email,
    '+3816' || (random() * 10000000)::int as phone,
    '/uploads/cv/candidate_' || s || '.pdf' as cv_path,
    '2024-01-01'::timestamp + (random() * interval '365 days') as created_at
FROM generate_series(
    (SELECT MAX(id) + 1 FROM candidate_profiles), 
    (SELECT MAX(id) + 20000 FROM candidate_profiles)
) as s;

-- =====================================================
-- 3. DODAVANJE DODATNIH JOB POSTING-OVA
-- =====================================================
SELECT 'Dodavanje dodatnih job posting-ova...' as status;

-- Postavljamo sequence na trenutnu vrednost
SELECT setval('job_postings_id_seq', (SELECT MAX(id) FROM job_postings));

-- Dodajemo 500 novih job posting-ova (koristimo samo PUBLISHED status kao u originalnom skriptu)
INSERT INTO job_postings (created_at, posting_status, valid_from, valid_to, requestion_id, pipeline_workflow_id)
SELECT 
    '2024-01-01'::timestamp + (random() * interval '365 days') as created_at,
    'PUBLISHED' as posting_status,
    '2024-01-01'::date + (random() * 365)::int as valid_from,
    '2025-12-31'::date as valid_to,
    (SELECT id FROM requestions ORDER BY random() LIMIT 1) as requestion_id,
    (SELECT id FROM workflow_defs ORDER BY random() LIMIT 1) as pipeline_workflow_id
FROM generate_series(
    (SELECT MAX(id) + 1 FROM job_postings), 
    (SELECT MAX(id) + 500 FROM job_postings)
) as s;

-- =====================================================
-- 4. DODAVANJE DODATNIH USERS
-- =====================================================
SELECT 'Dodavanje dodatnih users...' as status;

-- Postavljamo sequence na trenutnu vrednost
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- Dodajemo 200 novih korisnika (koristimo samo HR_MANAGER kao u originalnom skriptu)
INSERT INTO users (first_name, last_name, email, password, role, is_active, created_at)
SELECT 
    'User' || s as first_name,
    'LastName' || s as last_name,
    'user' || s || '@company.com' as email,
    '$2a$10$dummy.hash.for.testing' as password,
    'HR_MANAGER' as role,
    true as is_active,
    '2024-01-01'::timestamp + (random() * interval '365 days') as created_at
FROM generate_series(
    (SELECT MAX(id) + 1 FROM users), 
    (SELECT MAX(id) + 200 FROM users)
) as s;

-- =====================================================
-- 5. GENERISANJE 100,000 NOVIH APLIKACIJA
-- =====================================================
SELECT 'Generisanje 100,000 novih aplikacija...' as status;

-- Postavljamo sequence na trenutnu vrednost
SELECT setval('applications_id_seq', (SELECT MAX(id) FROM applications));

-- Kreiramo 100,000 novih aplikacija
WITH candidate_job_combinations AS (
    SELECT 
        c.id as candidate_id,
        j.id as job_posting_id,
        row_number() OVER (ORDER BY random()) as rn
    FROM candidate_profiles c
    CROSS JOIN job_postings j
    WHERE c.id > (SELECT MAX(id) - 20000 FROM candidate_profiles) -- Samo novi kandidati
       OR j.id > (SELECT MAX(id) - 500 FROM job_postings) -- Ili novi job postings
),
selected_combinations AS (
    SELECT candidate_id, job_posting_id
    FROM candidate_job_combinations
    WHERE rn <= 100000  -- 100,000 aplikacija
)
INSERT INTO applications (applied_at, note, application_status, job_posting_id, candidate_id, workflow_id, current_stage_id)
SELECT 
    '2024-01-01'::timestamp + (random() * interval '730 days') as applied_at,
    CASE 
        WHEN random() < 0.3 THEN 
            CASE (random() * 4)::int
                WHEN 0 THEN 'Kandidat nije zadovoljio uslove'
                WHEN 1 THEN 'Nedostaju potrebni dokumenti'
                WHEN 2 THEN 'Kandidat se povukao'
                WHEN 3 THEN 'Odličan kandidat'
                ELSE NULL
            END
        ELSE NULL
    END as note,
    CASE 
        WHEN (random() * 100)::int BETWEEN 0 AND 60 THEN 'ACTIVE'
        WHEN (random() * 100)::int BETWEEN 61 AND 75 THEN 'REJECTED'
        WHEN (random() * 100)::int BETWEEN 76 AND 85 THEN 'HIRED'
        WHEN (random() * 100)::int BETWEEN 86 AND 95 THEN 'WITHDRAWN'
        ELSE 'REFUSED_OFFER'
    END as application_status,
    sc.job_posting_id,
    sc.candidate_id,
    (SELECT id FROM workflow_defs ORDER BY random() LIMIT 1) as workflow_id,
    (SELECT id FROM workflow_stages ORDER BY random() LIMIT 1) as current_stage_id
FROM selected_combinations sc;

-- =====================================================
-- 6. GENERISANJE APPLICATION STATUS HISTORY ZA NOVE APLIKACIJE
-- =====================================================
SELECT 'Generisanje status history za nove aplikacije...' as status;

-- Za svaku novu aplikaciju kreiramo 2-6 status history zapisa
INSERT INTO application_status_history (application_id, stage_id, transition_id, entered_at, exited_at, comment, triggered_by_user_id)
SELECT 
    a.id as application_id,
    (SELECT id FROM workflow_stages ORDER BY random() LIMIT 1) as stage_id,
    CASE 
        WHEN random() < 0.7 THEN (SELECT id FROM workflow_transitions ORDER BY random() LIMIT 1)
        ELSE NULL
    END as transition_id,
    a.applied_at - (random() * interval '7 days') as entered_at,
    CASE 
        WHEN random() < 0.7 THEN 
            a.applied_at + (random() * interval '30 days')
        ELSE NULL
    END as exited_at,
    CASE (random() * 5)::int
        WHEN 0 THEN 'Test sent to candidate'
        WHEN 1 THEN 'Interview scheduled'
        WHEN 2 THEN 'Offer made to candidate'
        WHEN 3 THEN 'Application reviewed'
        ELSE 'Status updated'
    END as comment,
    (SELECT id FROM users ORDER BY random() LIMIT 1) as triggered_by_user_id
FROM applications a
CROSS JOIN generate_series(1, (2 + random() * 5)::int) as history_count
WHERE a.id > (SELECT MAX(id) - 100000 FROM applications); -- Samo za nove aplikacije

-- =====================================================
-- 7. GENERISANJE OFFERS ZA NOVE APLIKACIJE
-- =====================================================
SELECT 'Generisanje offers za nove aplikacije...' as status;

-- Kreiramo ponude za nove aplikacije
INSERT INTO offers (application_id, start_date, offer_status, created_at)
SELECT 
    a.id as application_id,
    CURRENT_DATE + (random() * 90)::int as start_date,
    CASE 
        WHEN (random() * 100)::int BETWEEN 0 AND 10 THEN 'DRAFT'
        WHEN (random() * 100)::int BETWEEN 11 AND 20 THEN 'APPROVAL'
        WHEN (random() * 100)::int BETWEEN 21 AND 30 THEN 'APPROVED'
        WHEN (random() * 100)::int BETWEEN 31 AND 50 THEN 'SENT'
        WHEN (random() * 100)::int BETWEEN 51 AND 80 THEN 'ACCEPTED'
        WHEN (random() * 100)::int BETWEEN 81 AND 95 THEN 'DECLINED'
        WHEN (random() * 100)::int BETWEEN 96 AND 98 THEN 'WITHDRAWN'
        ELSE 'EXPIRED'
    END as offer_status,
    '2024-01-01'::timestamp + (random() * interval '730 days') as created_at
FROM applications a
WHERE a.application_status IN ('HIRED', 'ACTIVE', 'REFUSED_OFFER')
  AND a.id > (SELECT MAX(id) - 100000 FROM applications) -- Samo za nove aplikacije
  AND random() < 0.3; -- Samo 30% aplikacija ima ponude

-- =====================================================
-- 8. FINALNE STATISTIKE
-- =====================================================
SELECT 'Finalne statistike nakon dodavanja 100,000 aplikacija...' as status;

-- Broj zapisa po tabeli nakon dodavanja
SELECT 
    'Applications' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('applications')) as table_size
FROM applications
UNION ALL
SELECT 
    'Application Status History' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('application_status_history')) as table_size
FROM application_status_history
UNION ALL
SELECT 
    'Offers' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('offers')) as table_size
FROM offers
UNION ALL
SELECT 
    'Job Postings' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('job_postings')) as table_size
FROM job_postings
UNION ALL
SELECT 
    'Candidates' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('candidate_profiles')) as table_size
FROM candidate_profiles
UNION ALL
SELECT 
    'Users' as table_name,
    COUNT(*) as total_records,
    pg_size_pretty(pg_total_relation_size('users')) as table_size
FROM users;

-- Distribucija statusova aplikacija
SELECT 
    'Application Status Distribution' as metric,
    application_status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM applications
GROUP BY application_status
ORDER BY count DESC;

-- Distribucija statusova ponuda
SELECT 
    'Offer Status Distribution' as metric,
    offer_status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM offers
GROUP BY offer_status
ORDER BY count DESC;

-- =====================================================
-- 9. ZAVRŠETAK
-- =====================================================
SELECT 'Dodavanje 100,000 aplikacija završeno!' as status;
SELECT 'Sada imate ukupno oko 157,000+ aplikacija za testiranje performansi.' as info;
SELECT 'Pokrenite vaš upit da vidite da li su sada performanse sporije.' as sledeći_korak;
