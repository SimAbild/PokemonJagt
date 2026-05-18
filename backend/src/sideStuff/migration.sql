-- =============================================================
-- DATABASE MIGRATION — PokemonJagt
-- Kør denne i: Supabase Dashboard → SQL Editor
-- =============================================================
--
-- 3NF design:
--   gym_members    — alle brugere i gymnasystemet
--   gym_roster     — hvilke trainers er aktivt i gymmet (max 6)
--   trainer_pokemon — pokemon knyttet til en gym_member
--   pokedex        — uændret
--
-- Ingen transitive afhængigheder:
--   gym_members:  alle kolonner afhænger direkte af id (PK)
--   gym_roster:   member_id + added_at afhænger af id (PK)
--                 roster er en separat relation — ikke en egenskab på gym_member
-- =============================================================


-- 1. Omdøb trainers → gym_members
--    FK i trainer_pokemon opdateres automatisk af Postgres
ALTER TABLE trainers RENAME TO gym_members;


-- 2. Tilføj nye kolonner til gym_members
ALTER TABLE gym_members
    ADD COLUMN IF NOT EXISTS email        VARCHAR(255),
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20),
    ADD COLUMN IF NOT EXISTS address      VARCHAR(255),
    ADD COLUMN IF NOT EXISTS role         VARCHAR(20) NOT NULL DEFAULT 'TRAINER';


-- 3. Opret gym_roster — holder styr på hvilke trainers der er i gymmet (max 6)
CREATE TABLE IF NOT EXISTS gym_roster (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL UNIQUE REFERENCES gym_members(id) ON DELETE CASCADE,
    added_at  TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);
