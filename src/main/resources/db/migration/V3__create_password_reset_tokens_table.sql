create table password_reset_tokens(
    id bigserial primary key,
    token varchar(512) not null unique, 
    user_id uuid not null references users(id) on delete cascade,
    expires_at timestamp not null,
    created_at timestamp default now()
)