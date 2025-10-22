-- ============================================================
-- PL/SQL Custom Types for Recruitment System
-- ============================================================
-- Autor: Extracted from PostgreSQL Database
-- Datum: 2024
-- Opis: Definicija custom tipova podataka za funkcije i procedure
-- ============================================================

-- Type za audit podatke
CREATE TYPE audit_data_type AS (
    user_id BIGINT,
    action VARCHAR(100),
    entity_type VARCHAR(100),
    entity_id BIGINT,
    before_data_json TEXT,
    after_data_json TEXT,
    time_utc TIMESTAMP WITH TIME ZONE
);

COMMENT ON TYPE audit_data_type IS 'Tip za audit log podatke - prati sve promene u sistemu';

-- Type za sumarne podatke po job posting-u
CREATE TYPE job_posting_summary_type AS (
    job_posting_id BIGINT,
    job_posting_name VARCHAR(255),
    total_applications BIGINT,
    hired_count BIGINT,
    rejection_count BIGINT,
    average_processing_days NUMERIC,
    success_rate NUMERIC
);

COMMENT ON TYPE job_posting_summary_type IS 'Sumarni podaci o performansama po oglasima za posao';

-- Type za osnovne metrike zapošljavanja
CREATE TYPE recruitment_metrics_type AS (
    total_applications BIGINT,
    total_hired BIGINT,
    average_time_to_hire NUMERIC,
    offer_rejection_percentage NUMERIC,
    invitation_rejection_ratio NUMERIC,
    report_start_date DATE,
    report_end_date DATE
);

COMMENT ON TYPE recruitment_metrics_type IS 'Osnovne metrike performansi procesa zapošljavanja';

-- Type za performanse po fazama
CREATE TYPE stage_performance_type AS (
    stage_name VARCHAR(255),
    entered_count BIGINT,
    completed_count BIGINT,
    conversion_rate NUMERIC,
    average_time_in_days NUMERIC,
    total_applications_in_stage BIGINT
);

COMMENT ON TYPE stage_performance_type IS 'Detaljne metrike performansi po fazama workflow-a';
