CREATE TABLE users (
  user_id UUID PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  email VARCHAR(320) NOT NULL UNIQUE,
  password_hash VARCHAR(255)
);
CREATE TABLE sessions (
  session_id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  token_hash TEXT NOT NULL,
  issued_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  device_info TEXT,
  is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE exercises (
    exercise_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    workout_type VARCHAR(50) NOT NULL,
    metrics JSON,
    series JSON
);
