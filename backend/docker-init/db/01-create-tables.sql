CREATE TABLE users (
  user_id UUID PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  email VARCHAR(320) NOT NULL UNIQUE,
  password_hash VARCHAR(255),
  google_id VARCHAR(255) UNIQUE,
  CONSTRAINT password_hash_or_google_id CHECK (
    password_hash IS NOT NULL
    OR google_id IS NOT NULL
  )
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