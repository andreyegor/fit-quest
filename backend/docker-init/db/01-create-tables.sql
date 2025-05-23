CREATE TABLE users (
  user_id UUID PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  email VARCHAR(320) NOT NULL UNIQUE,
  password_hash VARCHAR(255)
);
CREATE TABLE sessions (
  session_id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(user_id),
  token_hash TEXT NOT NULL,
  issued_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  device_info TEXT,
  is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE TABLE exercises (
  exercise_id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(user_id),
  start_time TIMESTAMPTZ NOT NULL,
  end_time TIMESTAMPTZ NOT NULL,
  exercise_type VARCHAR(50) NOT NULL CHECK (
    exercise_type IN ('walking', 'running', 'cycling', 'swimming')
  ),
  metrics JSONB,
  series JSONB
);
CREATE INDEX idx_exercises_user_id ON exercises(user_id);