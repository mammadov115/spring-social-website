create table profiles (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null unique references users(id) on delete cascade, 
    bio text, 
    avatar_url varvhar(500),
    birth_date date,
    location varchar(255),
    is_email_public boolean not null default false,
    is_birth_date_public boolean not null default false,
    is_location_public boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),

)