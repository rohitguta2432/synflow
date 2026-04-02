-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'INTERNAL_USER')),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Profiles table
CREATE TABLE profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unique_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('REAL', 'SHADOW')),
    expertise TEXT[],
    services_offered TEXT,
    industry_focus VARCHAR(255),
    geographic_reach TEXT[],
    track_record TEXT,
    contact_status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (contact_status IN ('ACTIVE', 'EXTERNAL', 'NOT_ONBOARDED')),
    summary TEXT,
    ai_generated BOOLEAN DEFAULT FALSE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Profile connections (many-to-many self-referencing)
CREATE TABLE profile_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    connected_profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    connection_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(profile_id, connected_profile_id)
);

-- Deals table
CREATE TABLE deals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    industry VARCHAR(255) NOT NULL,
    deal_type VARCHAR(20) NOT NULL CHECK (deal_type IN ('INVESTMENT', 'PARTNERSHIP', 'ADVISORY', 'BROKERAGE', 'OTHER')),
    ticket_size VARCHAR(100),
    geography TEXT[],
    requirements TEXT,
    status VARCHAR(10) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLOSED', 'DRAFT')),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Matches table
CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deal_id UUID NOT NULL REFERENCES deals(id) ON DELETE CASCADE,
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    relevance_score DECIMAL(5,2),
    match_reason TEXT,
    matched_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(deal_id, profile_id)
);

-- Indexes
CREATE INDEX idx_profiles_industry ON profiles(industry_focus);
CREATE INDEX idx_profiles_type ON profiles(type);
CREATE INDEX idx_profiles_status ON profiles(contact_status);
CREATE INDEX idx_deals_industry ON deals(industry);
CREATE INDEX idx_deals_status ON deals(status);
CREATE INDEX idx_matches_deal ON matches(deal_id);
CREATE INDEX idx_matches_profile ON matches(profile_id);
CREATE INDEX idx_matches_score ON matches(relevance_score DESC);
