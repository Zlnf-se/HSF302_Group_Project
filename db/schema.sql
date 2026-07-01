IF OBJECT_ID(N'dbo.application_documents', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.application_documents (
        id BIGINT IDENTITY(1,1) NOT NULL,
        application_id BIGINT NOT NULL,
        document_type NVARCHAR(30) NOT NULL,
        original_file_name NVARCHAR(255) NOT NULL,
        content_type NVARCHAR(120) NOT NULL,
        file_size_bytes BIGINT NOT NULL,
        storage_path NVARCHAR(500) NOT NULL,
        uploaded_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT pk_application_documents PRIMARY KEY (id)
    );
END;

IF OBJECT_ID(N'dbo.application_notes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.application_notes (
        id BIGINT IDENTITY(1,1) NOT NULL,
        application_id BIGINT NOT NULL,
        author_id BIGINT NOT NULL,
        content NVARCHAR(3000) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT pk_application_notes PRIMARY KEY (id)
    );
END;

IF OBJECT_ID(N'dbo.application_status_history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.application_status_history (
        id BIGINT IDENTITY(1,1) NOT NULL,
        application_id BIGINT NOT NULL,
        from_status NVARCHAR(40),
        to_status NVARCHAR(40) NOT NULL,
        changed_by_id BIGINT,
        changed_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        note NVARCHAR(1000),
        CONSTRAINT pk_application_status_history PRIMARY KEY (id)
    );
END;

IF OBJECT_ID(N'dbo.applications', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.applications (
        id BIGINT IDENTITY(1,1) NOT NULL,
        candidate_id BIGINT NOT NULL,
        job_posting_id BIGINT NOT NULL,
        applied_at DATETIME2 DEFAULT SYSDATETIME(),
        status NVARCHAR(30),
        cover_letter NVARCHAR(3000),
        CONSTRAINT pk_applications PRIMARY KEY (id),
        CONSTRAINT uq_applications_candidate_job UNIQUE (candidate_id, job_posting_id)
    );
END;

IF OBJECT_ID(N'dbo.activity_logs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.activity_logs (
        id BIGINT IDENTITY(1,1) NOT NULL,
        event_type NVARCHAR(50) NOT NULL,
        actor_id BIGINT,
        actor_username NVARCHAR(100),
        description NVARCHAR(2000),
        ip_address NVARCHAR(45),
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT pk_activity_logs PRIMARY KEY (id)
    );
END;

IF OBJECT_ID(N'dbo.candidate_profiles', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.candidate_profiles (
        id BIGINT IDENTITY(1,1) NOT NULL,
        years_of_experience INT,
        education_level NVARCHAR(100),
        current_title NVARCHAR(150),
        linkedin_url NVARCHAR(300),
        resume_summary NVARCHAR(2000),
        CONSTRAINT pk_candidate_profiles PRIMARY KEY (id)
    );
END;

IF OBJECT_ID(N'dbo.candidate_skills', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.candidate_skills (
        candidate_id BIGINT NOT NULL,
        skill_id BIGINT NOT NULL,
        CONSTRAINT pk_candidate_skills PRIMARY KEY (candidate_id, skill_id)
    );
END;

IF OBJECT_ID(N'dbo.candidates', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.candidates (
        id BIGINT IDENTITY(1,1) NOT NULL,
        name NVARCHAR(100) NOT NULL,
        email NVARCHAR(150) NOT NULL,
        phone NVARCHAR(20),
        profile_id BIGINT,
        user_id BIGINT,
        CONSTRAINT pk_candidates PRIMARY KEY (id),
        CONSTRAINT uq_candidates_email UNIQUE (email),
        CONSTRAINT uq_candidates_profile UNIQUE (profile_id),
        CONSTRAINT uq_candidates_user UNIQUE (user_id)
    );
END;

IF OBJECT_ID(N'dbo.companies', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.companies (
        id BIGINT IDENTITY(1,1) NOT NULL,
        name NVARCHAR(200) NOT NULL,
        industry NVARCHAR(100),
        website NVARCHAR(200),
        profile_id BIGINT,
        CONSTRAINT pk_companies PRIMARY KEY (id),
        CONSTRAINT uq_companies_name UNIQUE (name),
        CONSTRAINT uq_companies_profile UNIQUE (profile_id)
    );
END;

IF OBJECT_ID(N'dbo.company_profiles', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.company_profiles (
        id BIGINT IDENTITY(1,1) NOT NULL,
        description NVARCHAR(2000),
        headquarters NVARCHAR(200),
        employee_count INT,
        founded_year INT,
        CONSTRAINT pk_company_profiles PRIMARY KEY (id)
    );
END;

IF OBJECT_ID(N'dbo.evaluations', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.evaluations (
        id BIGINT IDENTITY(1,1) NOT NULL,
        interview_id BIGINT NOT NULL,
        interviewer_id BIGINT NOT NULL,
        rating INT NOT NULL,
        feedback NVARCHAR(4000) NOT NULL,
        submitted_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT pk_evaluations PRIMARY KEY (id),
        CONSTRAINT uq_evaluations_interview UNIQUE (interview_id)
    );
END;

IF OBJECT_ID(N'dbo.interviews', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.interviews (
        id BIGINT IDENTITY(1,1) NOT NULL,
        application_id BIGINT NOT NULL,
        scheduled_at DATETIME2 NOT NULL,
        interview_type NVARCHAR(30),
        interviewer_name NVARCHAR(100),
        notes NVARCHAR(1000),
        result NVARCHAR(30),
        CONSTRAINT pk_interviews PRIMARY KEY (id)
    );
END;

IF OBJECT_ID(N'dbo.job_postings', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.job_postings (
        id BIGINT IDENTITY(1,1) NOT NULL,
        title NVARCHAR(200) NOT NULL,
        description NVARCHAR(2000),
        location NVARCHAR(100),
        job_type NVARCHAR(30),
        salary_min DECIMAL(12,2),
        salary_max DECIMAL(12,2),
        posted_date DATE,
        deadline DATE,
        status NVARCHAR(30),
        company_id BIGINT NOT NULL,
        CONSTRAINT pk_job_postings PRIMARY KEY (id)
    );
END;

IF OBJECT_ID(N'dbo.job_required_skills', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.job_required_skills (
        job_posting_id BIGINT NOT NULL,
        skill_id BIGINT NOT NULL,
        CONSTRAINT pk_job_required_skills PRIMARY KEY (job_posting_id, skill_id)
    );
END;

IF OBJECT_ID(N'dbo.password_reset_tokens', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.password_reset_tokens (
        id BIGINT IDENTITY(1,1) NOT NULL,
        token NVARCHAR(100) NOT NULL,
        user_id BIGINT NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        expires_at DATETIME2 NOT NULL,
        used_at DATETIME2,
        CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
        CONSTRAINT uq_password_reset_tokens_token UNIQUE (token)
    );
END;

IF OBJECT_ID(N'dbo.roles', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.roles (
        id BIGINT IDENTITY(1,1) NOT NULL,
        name NVARCHAR(30) NOT NULL,
        CONSTRAINT pk_roles PRIMARY KEY (id),
        CONSTRAINT uq_roles_name UNIQUE (name)
    );
END;

IF OBJECT_ID(N'dbo.skills', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.skills (
        id BIGINT IDENTITY(1,1) NOT NULL,
        name NVARCHAR(100) NOT NULL,
        category NVARCHAR(50),
        CONSTRAINT pk_skills PRIMARY KEY (id),
        CONSTRAINT uq_skills_name UNIQUE (name)
    );
END;

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) NOT NULL,
        username NVARCHAR(100) NOT NULL,
        password NVARCHAR(255) NOT NULL,
        email NVARCHAR(150) NOT NULL,
        full_name NVARCHAR(150) NOT NULL,
        enabled BIT NOT NULL DEFAULT 1,
        account_status NVARCHAR(20) NOT NULL CONSTRAINT df_users_account_status DEFAULT N'ACTIVE',
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        role_id BIGINT NOT NULL,
        CONSTRAINT pk_users PRIMARY KEY (id),
        CONSTRAINT uq_users_username UNIQUE (username),
        CONSTRAINT uq_users_email UNIQUE (email)
    );
END;

IF COL_LENGTH(N'dbo.users', N'account_status') IS NULL
BEGIN
    ALTER TABLE dbo.users
    ADD account_status NVARCHAR(20) NOT NULL
        CONSTRAINT df_users_account_status DEFAULT N'ACTIVE';

    EXEC(N'
        UPDATE dbo.users
        SET account_status = CASE
            WHEN enabled = 1 THEN N''ACTIVE''
            ELSE N''INACTIVE''
        END
    ');
END;

IF OBJECT_ID(N'dbo.fk_users_roles', N'F') IS NULL
    ALTER TABLE dbo.users ADD CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES dbo.roles(id);

IF OBJECT_ID(N'dbo.fk_candidates_profiles', N'F') IS NULL
    ALTER TABLE dbo.candidates ADD CONSTRAINT fk_candidates_profiles FOREIGN KEY (profile_id) REFERENCES dbo.candidate_profiles(id);

IF OBJECT_ID(N'dbo.fk_candidates_users', N'F') IS NULL
    ALTER TABLE dbo.candidates ADD CONSTRAINT fk_candidates_users FOREIGN KEY (user_id) REFERENCES dbo.users(id);

IF OBJECT_ID(N'dbo.fk_companies_profiles', N'F') IS NULL
    ALTER TABLE dbo.companies ADD CONSTRAINT fk_companies_profiles FOREIGN KEY (profile_id) REFERENCES dbo.company_profiles(id);

IF OBJECT_ID(N'dbo.fk_job_postings_companies', N'F') IS NULL
    ALTER TABLE dbo.job_postings ADD CONSTRAINT fk_job_postings_companies FOREIGN KEY (company_id) REFERENCES dbo.companies(id);

IF OBJECT_ID(N'dbo.fk_candidate_skills_candidates', N'F') IS NULL
    ALTER TABLE dbo.candidate_skills ADD CONSTRAINT fk_candidate_skills_candidates FOREIGN KEY (candidate_id) REFERENCES dbo.candidates(id);

IF OBJECT_ID(N'dbo.fk_candidate_skills_skills', N'F') IS NULL
    ALTER TABLE dbo.candidate_skills ADD CONSTRAINT fk_candidate_skills_skills FOREIGN KEY (skill_id) REFERENCES dbo.skills(id);

IF OBJECT_ID(N'dbo.fk_job_required_skills_job_postings', N'F') IS NULL
    ALTER TABLE dbo.job_required_skills ADD CONSTRAINT fk_job_required_skills_job_postings FOREIGN KEY (job_posting_id) REFERENCES dbo.job_postings(id);

IF OBJECT_ID(N'dbo.fk_job_required_skills_skills', N'F') IS NULL
    ALTER TABLE dbo.job_required_skills ADD CONSTRAINT fk_job_required_skills_skills FOREIGN KEY (skill_id) REFERENCES dbo.skills(id);

IF OBJECT_ID(N'dbo.fk_applications_candidates', N'F') IS NULL
    ALTER TABLE dbo.applications ADD CONSTRAINT fk_applications_candidates FOREIGN KEY (candidate_id) REFERENCES dbo.candidates(id);

IF OBJECT_ID(N'dbo.fk_applications_job_postings', N'F') IS NULL
    ALTER TABLE dbo.applications ADD CONSTRAINT fk_applications_job_postings FOREIGN KEY (job_posting_id) REFERENCES dbo.job_postings(id);

IF OBJECT_ID(N'dbo.fk_interviews_applications', N'F') IS NULL
    ALTER TABLE dbo.interviews ADD CONSTRAINT fk_interviews_applications FOREIGN KEY (application_id) REFERENCES dbo.applications(id);

IF OBJECT_ID(N'dbo.fk_application_documents_applications', N'F') IS NULL
    ALTER TABLE dbo.application_documents ADD CONSTRAINT fk_application_documents_applications FOREIGN KEY (application_id) REFERENCES dbo.applications(id);

IF OBJECT_ID(N'dbo.fk_application_notes_applications', N'F') IS NULL
    ALTER TABLE dbo.application_notes ADD CONSTRAINT fk_application_notes_applications FOREIGN KEY (application_id) REFERENCES dbo.applications(id);

IF OBJECT_ID(N'dbo.fk_application_notes_users', N'F') IS NULL
    ALTER TABLE dbo.application_notes ADD CONSTRAINT fk_application_notes_users FOREIGN KEY (author_id) REFERENCES dbo.users(id);

IF OBJECT_ID(N'dbo.fk_application_status_history_applications', N'F') IS NULL
    ALTER TABLE dbo.application_status_history ADD CONSTRAINT fk_application_status_history_applications FOREIGN KEY (application_id) REFERENCES dbo.applications(id);

IF OBJECT_ID(N'dbo.fk_application_status_history_users', N'F') IS NULL
    ALTER TABLE dbo.application_status_history ADD CONSTRAINT fk_application_status_history_users FOREIGN KEY (changed_by_id) REFERENCES dbo.users(id);

IF OBJECT_ID(N'dbo.fk_evaluations_interviews', N'F') IS NULL
    ALTER TABLE dbo.evaluations ADD CONSTRAINT fk_evaluations_interviews FOREIGN KEY (interview_id) REFERENCES dbo.interviews(id);

IF OBJECT_ID(N'dbo.fk_evaluations_users', N'F') IS NULL
    ALTER TABLE dbo.evaluations ADD CONSTRAINT fk_evaluations_users FOREIGN KEY (interviewer_id) REFERENCES dbo.users(id);

IF OBJECT_ID(N'dbo.fk_activity_logs_users', N'F') IS NULL
    ALTER TABLE dbo.activity_logs ADD CONSTRAINT fk_activity_logs_users FOREIGN KEY (actor_id) REFERENCES dbo.users(id);

IF OBJECT_ID(N'dbo.fk_password_reset_tokens_users', N'F') IS NULL
    ALTER TABLE dbo.password_reset_tokens ADD CONSTRAINT fk_password_reset_tokens_users FOREIGN KEY (user_id) REFERENCES dbo.users(id);
