-- =====================================================
-- SKRIPT ZA GENERISANJE TEST PODATAKA ZA PERFORMANSE - ISPRAVNA VERZIJA
-- =====================================================
-- Ovaj skript generiše veliki broj podataka za testiranje performansi
-- funkcije calculate_recruitment_metrics

-- =====================================================
-- 1. PROVERA POSTOJEĆIH PODATAKA
-- =====================================================
SELECT 'Postojeći requestion ID-ovi:' as info;
SELECT id FROM requestions ORDER BY id;

SELECT 'Postojeći workflow ID-ovi:' as info;
SELECT id FROM workflow_defs ORDER BY id;

SELECT 'Postojeći workflow stage ID-ovi:' as info;
SELECT id FROM workflow_stages ORDER BY id;

SELECT 'Postojeći workflow transition ID-ovi:' as info;
SELECT id FROM workflow_transitions ORDER BY id;

SELECT 'Postojeći user ID-ovi:' as info;
SELECT id FROM users ORDER BY id;

-- =====================================================
-- 2. KREIRANJE DODATNIH JOB POSTING-OVA
-- =====================================================
-- Prvo postavljamo sequence na 100
SELECT setval('job_postings_id_seq', 100);

-- Kreiramo job postings koristeći postojeće requestion i workflow ID-ove
INSERT INTO job_postings (created_at, posting_status, valid_from, valid_to, requestion_id, pipeline_workflow_id)
SELECT 
    '2024-01-01'::timestamp + (random() * interval '365 days') as created_at,
    'PUBLISHED' as posting_status,
    '2024-01-01'::date + (random() * 365)::int as valid_from,
    '2025-12-31'::date as valid_to,
    -- Koristimo postojeće requestion ID-ove
    (SELECT id FROM requestions ORDER BY random() LIMIT 1) as requestion_id,
    -- Koristimo postojeće workflow ID-ove
    (SELECT id FROM workflow_defs ORDER BY random() LIMIT 1) as pipeline_workflow_id
FROM generate_series(101, 150) as s;

-- =====================================================
-- 3. KREIRANJE DODATNIH CANDIDATE PROFILA
-- =====================================================
-- Prvo postavljamo sequence na 100
SELECT setval('candidate_profiles_id_seq', 100);

INSERT INTO candidate_profiles (first_name, last_name, email, phone, cv_path, created_at)
SELECT 
    'Candidate' || s as first_name,
    'LastName' || s as last_name,
    'candidate' || s || '@example.com' as email,
    '+3816' || (random() * 10000000)::int as phone,
    '/uploads/cv/candidate_' || s || '.pdf' as cv_path,
    '2024-01-01'::timestamp + (random() * interval '365 days') as created_at
FROM generate_series(101, 1100) as s;

-- =====================================================
-- 4. KREIRANJE DODATNIH USERS
-- =====================================================
-- Prvo postavljamo sequence na 100
SELECT setval('users_id_seq', 100);

INSERT INTO users (first_name, last_name, email, password, role, is_active, created_at)
SELECT 
    'User' || s as first_name,
    'LastName' || s as last_name,
    'user' || s || '@company.com' as email,
    '$2a$10$dummy.hash.for.testing' as password,
    'HR_MANAGER' as role,
    true as is_active,
    '2024-01-01'::timestamp + (random() * interval '365 days') as created_at
FROM generate_series(101, 150) as s;

-- =====================================================
-- 5. GENERISANJE 100,000+ APLIKACIJA
-- =====================================================
-- Prvo postavljamo sequence na 100
SELECT setval('applications_id_seq', 100);

-- Kreiramo jedinstvene kombinacije kandidat-job_posting
WITH candidate_job_combinations AS (
    SELECT 
        c.id as candidate_id,
        j.id as job_posting_id,
        row_number() OVER (ORDER BY random()) as rn
    FROM candidate_profiles c
    CROSS JOIN job_postings j
    WHERE c.id >= 100 OR j.id >= 100  -- Samo test podaci
),
selected_combinations AS (
    SELECT candidate_id, job_posting_id
    FROM candidate_job_combinations
    WHERE rn <= 100000  -- Limit na 100,000 aplikacija
)
INSERT INTO applications (applied_at, note, application_status, job_posting_id, candidate_id, workflow_id, current_stage_id)
SELECT 
    -- Datum aplikacije u opsegu 2024-2025
    '2024-01-01'::timestamp + (random() * interval '730 days') as applied_at,
    
    -- Random note (može biti NULL)
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
    
    -- Status aplikacije sa određenom distribucijom
    CASE 
        WHEN (random() * 100)::int BETWEEN 0 AND 60 THEN 'ACTIVE'
        WHEN (random() * 100)::int BETWEEN 61 AND 75 THEN 'REJECTED'
        WHEN (random() * 100)::int BETWEEN 76 AND 85 THEN 'HIRED'
        WHEN (random() * 100)::int BETWEEN 86 AND 95 THEN 'WITHDRAWN'
        ELSE 'REFUSED_OFFER'
    END as application_status,
    
    sc.job_posting_id,
    sc.candidate_id,
    
    -- Random workflow (koristimo postojeće ID-ove)
    (SELECT id FROM workflow_defs ORDER BY random() LIMIT 1) as workflow_id,
    
    -- Random current stage (koristimo postojeće ID-ove)
    (SELECT id FROM workflow_stages ORDER BY random() LIMIT 1) as current_stage_id

FROM selected_combinations sc;

-- =====================================================
-- 6. GENERISANJE APPLICATION STATUS HISTORY
-- =====================================================
-- Za svaku aplikaciju kreiramo 2-5 status history zapisa
INSERT INTO application_status_history (application_id, stage_id, transition_id, entered_at, exited_at, comment, triggered_by_user_id)
SELECT 
    a.id as application_id,
    
    -- Random stage (koristimo postojeće ID-ove)
    (SELECT id FROM workflow_stages ORDER BY random() LIMIT 1) as stage_id,
    
    -- Random transition (može biti NULL)
    CASE 
        WHEN random() < 0.7 THEN (SELECT id FROM workflow_transitions ORDER BY random() LIMIT 1)
        ELSE NULL
    END as transition_id,
    
    -- entered_at - uvek pre applied_at aplikacije
    a.applied_at - (random() * interval '7 days') as entered_at,
    
    -- exited_at - za neke zapise (70% slučajeva)
    CASE 
        WHEN random() < 0.7 THEN 
            a.applied_at + (random() * interval '30 days')
        ELSE NULL
    END as exited_at,
    
    -- Random comment
    CASE (random() * 5)::int
        WHEN 0 THEN 'Test sent to candidate'
        WHEN 1 THEN 'Interview scheduled'
        WHEN 2 THEN 'Offer made to candidate'
        WHEN 3 THEN 'Application reviewed'
        ELSE 'Status updated'
    END as comment,
    
    -- Random user (koristimo postojeće ID-ove)
    (SELECT id FROM users ORDER BY random() LIMIT 1) as triggered_by_user_id

FROM applications a
CROSS JOIN generate_series(1, (2 + random() * 4)::int) as history_count
WHERE a.id >= 100; -- Samo za test aplikacije

-- =====================================================
-- 7. GENERISANJE OFFERS
-- =====================================================
-- Kreiramo ponude za HIRED aplikacije i neke ACTIVE
INSERT INTO offers (application_id, start_date, offer_status, created_at)
SELECT 
    a.id as application_id,
    
    -- start_date - u budućnosti
    CURRENT_DATE + (random() * 90)::int as start_date,
    
    -- offer_status sa distribucijom
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
    
    -- created_at - u opsegu 2024-2025
    '2024-01-01'::timestamp + (random() * interval '730 days') as created_at

FROM applications a
WHERE a.application_status IN ('HIRED', 'ACTIVE', 'REFUSED_OFFER')
  AND a.id >= 100 -- Samo za test aplikacije
  AND random() < 0.3; -- Samo 30% aplikacija ima ponude

-- =====================================================
-- 8. DODATNO POBOLJŠANJE ZA HIRED APLIKACIJE
-- =====================================================
-- Osiguravamo da HIRED aplikacije imaju ispravnu status history
UPDATE application_status_history ash
SET exited_at = ash.entered_at + (random() * interval '30 days')
FROM applications a
WHERE ash.application_id = a.id 
  AND a.application_status = 'HIRED'
  AND a.id >= 100 -- Samo za test aplikacije
  AND ash.exited_at IS NULL
  AND ash.stage_id = 104; -- Ponuda stage

-- =====================================================
-- 9. STATISTIKE NAKON GENERISANJA
-- =====================================================
SELECT 
    'Applications' as table_name,
    COUNT(*) as total_records
FROM applications
UNION ALL
SELECT 
    'Application Status History' as table_name,
    COUNT(*) as total_records
FROM application_status_history
UNION ALL
SELECT 
    'Offers' as table_name,
    COUNT(*) as total_records
FROM offers
UNION ALL
SELECT 
    'Job Postings' as table_name,
    COUNT(*) as total_records
FROM job_postings
UNION ALL
SELECT 
    'Candidates' as table_name,
    COUNT(*) as total_records
FROM candidate_profiles
UNION ALL
SELECT 
    'Users' as table_name,
    COUNT(*) as total_records
FROM users;

-- =====================================================
-- 10. ANALIZA DISTRIBUCIJE STATUSOVA
-- =====================================================
SELECT 
    'Application Status Distribution' as metric,
    application_status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM applications
GROUP BY application_status
ORDER BY count DESC;

SELECT 
    'Offer Status Distribution' as metric,
    offer_status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM offers
GROUP BY offer_status
ORDER BY count DESC;

-- =====================================================
-- 11. TEST FUNKCIJE
-- =====================================================
SELECT 'Test funkcije calculate_recruitment_metrics:' as test;
SELECT * FROM calculate_recruitment_metrics('2024-01-01'::date, '2025-12-31'::date);
