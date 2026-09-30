-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    id_card VARCHAR(18),
    gender SMALLINT DEFAULT 0,
    birth_date DATE,
    role SMALLINT NOT NULL DEFAULT 0,
    avatar VARCHAR(255),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 2. 医生表
CREATE TABLE IF NOT EXISTS doctors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    name VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    title VARCHAR(50),
    specialty TEXT,
    introduction TEXT,
    schedule JSONB,
    consultation_fee DECIMAL(10,2) DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 预约表
CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES users(id),
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    appointment_date DATE NOT NULL,
    appointment_time VARCHAR(20) NOT NULL,
    symptom TEXT,
    status SMALLINT NOT NULL DEFAULT 0,
    cancel_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 药品表
CREATE TABLE IF NOT EXISTS drugs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    generic_name VARCHAR(200),
    category VARCHAR(100),
    specification VARCHAR(100),
    unit VARCHAR(20),
    manufacturer VARCHAR(200),
    price DECIMAL(10,2),
    stock INTEGER DEFAULT 0,
    instruction TEXT,
    side_effects TEXT,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. 对话会话表
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255),
    context JSONB,
    message_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. 对话消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES chat_sessions(id),
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    token_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. 知识库文档表
CREATE TABLE IF NOT EXISTS knowledge_docs (
    id BIGSERIAL PRIMARY KEY,
    doc_id VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    category VARCHAR(50),
    source VARCHAR(255),
    embedding_id VARCHAR(64),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. 管理员表
CREATE TABLE IF NOT EXISTS admins (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始管理员账号：admin / 123456
INSERT INTO admins (username, password, real_name, status)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 1)
ON CONFLICT (username) DO NOTHING;

-- 创建索引提升查询性能
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_doctors_department ON doctors(department);
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_appointments_doctor ON appointments(doctor_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_drugs_name ON drugs(name);
CREATE INDEX idx_chat_sessions_user ON chat_sessions(user_id);
CREATE INDEX idx_chat_sessions_session ON chat_sessions(session_id);
CREATE INDEX idx_chat_messages_session ON chat_messages(session_id);
CREATE INDEX idx_knowledge_docs_category ON knowledge_docs(category);