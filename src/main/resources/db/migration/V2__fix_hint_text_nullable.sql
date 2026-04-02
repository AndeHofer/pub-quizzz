-- Phase 17 changed Hint.hintText to nullable=true in the entity, but ddl-auto=update
-- does not alter existing NOT NULL constraints. This migration fixes the live schema.
-- The IF EXISTS guard makes this safe to run on fresh databases where Flyway runs
-- before Hibernate has created the tables.
ALTER TABLE IF EXISTS question_hints
    ALTER COLUMN hint_text VARCHAR NULL;
