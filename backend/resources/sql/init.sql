CREATE EXTENSION "uuid-ossp";
CREATE EXTENSION intarray;

-- Users
CREATE TABLE users (
    id UUID DEFAULT uuid_generate_v4() primary key,
    flake_id bigint not null,
    screen_name text NOT NULL unique,
    name text NOT NULL,
    email text NOT NULL unique,
    avatar text default null,
    oauth_type text default null,
    oauth_id text default null,
    block boolean DEFAULT false,
    created_at timestamp with time zone NOT NULL default (current_timestamp AT TIME ZONE 'UTC'));
  ALTER TABLE users ADD CONSTRAINT users_oauth_id_key UNIQUE (oauth_id);
  ALTER TABLE users ADD CONSTRAINT created_at_chk CHECK (EXTRACT(TIMEZONE from created_at) = '0');
CREATE INDEX users_created_at_index ON users(created_at DESC);

CREATE TABLE files (
    id UUID DEFAULT uuid_generate_v4() primary key,
    flake_id bigint not null,
    user_id UUID not null,
    name text not null default 'Untitled',
    data json not null,
    likes int not null default 0,
    views int not null default 0,
    private boolean default true,
    del boolean default false,
    created_at timestamp with time zone NOT NULL default (current_timestamp AT TIME ZONE 'UTC'));
  ALTER TABLE files ADD CONSTRAINT created_at_chk CHECK (EXTRACT(TIMEZONE from created_at) = '0');

-- TODO: histories

CREATE TABLE comments (
    id UUID DEFAULT uuid_generate_v4() primary key,
    flake_id bigint not null,
    user_id UUID NOT NULL,
    file_id UUID NOT NULL,
    body text not null,
    del boolean default false,
    created_at timestamp with time zone NOT NULL default (current_timestamp AT TIME ZONE 'UTC'),
    updated_at timestamp with time zone NOT NULL default (current_timestamp AT TIME ZONE 'UTC')
    );
  ALTER TABLE comments ADD CONSTRAINT created_at_chk CHECK (EXTRACT(TIMEZONE from created_at) = '0');
  ALTER TABLE comments ADD CONSTRAINT updated_at_chk CHECK (EXTRACT(TIMEZONE from updated_at) = '0');

CREATE TABLE refresh_tokens (
    user_id UUID NOT NULL unique,
    token   UUID NOT NULL unique);

CREATE TABLE reports (
    id UUID DEFAULT uuid_generate_v4() primary key,
    flake_id bigint not null,
    user_id UUID NOT NULL,
    file_id UUID NOT NULL,
    status text default 'pending',
    data json not null,
    created_at timestamp with time zone NOT NULL default (current_timestamp AT TIME ZONE 'UTC'));
  ALTER TABLE reports ADD CONSTRAINT created_at_chk CHECK (EXTRACT(TIMEZONE from created_at) = '0');
