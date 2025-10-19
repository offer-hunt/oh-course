create table if not exists course.__migration_probe
(
    id         bigserial primary key,
    created_at timestamp not null default now()
);