CREATE TABLE IF NOT EXISTS id_generator (
  seq_name VARCHAR(255) NOT NULL PRIMARY KEY,
  next_val BIGINT
);

INSERT INTO id_generator (seq_name, next_val)
VALUES ('fc_participation_seq', 1)
ON DUPLICATE KEY UPDATE next_val = next_val;
