CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    uid CHAR(36) NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL UNIQUE,
    passhash VARCHAR(255),
    google_id VARCHAR(255),
    CONSTRAINT passhash_or_google_id CHECK (
        passhash IS NOT NULL OR google_id IS NOT NULL
    )
);

CREATE TABLE sessions (
  id UUID PRIMARY KEY,
  uid UUID NOT NULL,
  tokenhash TEXT NOT NULL,
  issued_at TIMESTAMP NOT NULL, -- когда выпущен
  expires_at TIMESTAMP NOT NULL,-- когда истекает
  device_info TEXT,             -- инфа о девайсе (опционально)
  revoked BOOLEAN NOT NULL DEFAULT FALSE, -- признак отзыва
  CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(uuid) ON DELETE CASCADE
);