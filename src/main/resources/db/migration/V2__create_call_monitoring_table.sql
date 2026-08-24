CREATE TABLE call_monitoring (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    call_id VARCHAR(50) NOT NULL,
    call_timestamp TIMESTAMPTZ NOT NULL,
    cs_name VARCHAR(100) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    sentiment_score SMALLINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_call_monitoring_call_id UNIQUE (call_id),
    CONSTRAINT ck_call_monitoring_sentiment_score CHECK (sentiment_score BETWEEN 0 AND 100)
);

CREATE INDEX idx_call_monitoring_call_timestamp ON call_monitoring (call_timestamp);
CREATE INDEX idx_call_monitoring_sentiment_score ON call_monitoring (sentiment_score);
