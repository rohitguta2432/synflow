-- pgvector extension for semantic similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- text-embedding-3-small produces 1536-dim vectors
ALTER TABLE profiles ADD COLUMN embedding vector(1536);
ALTER TABLE deals    ADD COLUMN embedding vector(1536);

-- HNSW indexes use cosine distance (matches the <=> operator).
-- HNSW builds incrementally and needs no training step, so it works on an empty table.
CREATE INDEX idx_profiles_embedding ON profiles USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_deals_embedding    ON deals    USING hnsw (embedding vector_cosine_ops);
