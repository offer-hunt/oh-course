create table if not exists course.course_courses
(
    id                      uuid            primary key,
    author_id               uuid            not null,
    title                   varchar(255)    not null,
    slug                    varchar(255)    unique not null,
    description              text            null,
    cover_url               varchar(512)    null,
    language                varchar(16)     null,
    level                   varchar(32)     null,
    estimated_duration_min  int             null,
    status                  varchar         not null check (status in ('DRAFT','PUBLISHED','ARCHIVED')),
    access_type             varchar         not null check (access_type in ('PUBLIC','PRIVATE_LINK')),
    invite_code             varchar(64)     null,
    requires_entitlement     boolean         not null default false,
    max_free_enrollments    int             null,
    version                 int             not null default 1,
    published_at            timestamptz     null,
    archived_at             timestamptz     null,
    created_at              timestamptz     not null default now(),
    updated_at              timestamptz     null
);

create table if not exists course.course_members
(
    course_id uuid not null references course.course_courses (id),
    user_id   uuid not null,
    role      varchar not null check (role in ('OWNER','ADMIN')),
    added_at  timestamptz not null default now(),
    added_by  uuid,
    constraint course_members_pk primary key (course_id, user_id)
);

create table if not exists course.course_lessons
(
    id              uuid primary key,
    course_id       uuid not null references course.course_courses (id),
    title           varchar(255) not null,
    description     text null,
    order_index     int not null,
    duration_min    int null,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz null
);

create table if not exists course.course_lesson_pages
(
    id              uuid primary key,
    lesson_id       uuid not null references course.course_lessons (id),
    title           varchar(255) not null,
    page_type       varchar not null check (page_type in ('THEORY','TEST','CODE_TASK')),
    sort_order      int not null,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz null
);

create table if not exists course.course_methodical_page_content
(
    page_id             uuid primary key references course.course_lesson_pages (id),
    markdown            text not null,
    external_video_url  text null,
    updated_at          timestamptz not null default now()
);

create table if not exists course.course_questions
(
    id              uuid primary key,
    page_id         uuid not null references course.course_lesson_pages (id),
    type            varchar not null check (type in ('SINGLE_CHOICE','MULTIPLE_CHOICE','TEXT_INPUT','CODE')),
    text            text not null,
    correct_answer  text null,
    use_ai_check    boolean not null default false,
    points          int null,
    sort_order      int not null,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz null
);

create table if not exists course.course_question_options
(
    id          uuid primary key,
    question_id uuid not null references course.course_questions (id),
    label       text not null,
    is_correct  boolean not null default false,
    sort_order  int not null
);

create table if not exists course.course_question_test_cases
(
    id               uuid primary key,
    question_id      uuid not null references course.course_questions (id),
    input_data       text not null,
    expected_output  text not null,
    timeout_ms       int null,
    memory_limit_mb  int null
);

CREATE EXTENSION IF NOT EXISTS citext;
create table if not exists course.course_tags_ref
(
    id          uuid primary key,
    name        citext unique not null,
    created_at  timestamptz not null default now()
);

create table if not exists course.course_tags
(
    course_id   uuid not null references course.course_courses (id),
    tag_id      uuid not null references course.course_tags_ref (id),
    constraint course_tags_pk primary key (course_id, tag_id)
);

create table if not exists course.course_ratings
(
    user_id     uuid not null,
    course_id   uuid not null references course.course_courses (id),
    value       int not null check (value between 1 and 5),
    comment     text null,
    created_at  timestamptz not null default now(),
    updated_at  timestamptz null,
    constraint course_ratings_pk primary key (user_id, course_id)
);

create table if not exists course.course_stats
(
    course_id       uuid primary key references course.course_courses (id),
    enrollments     int not null default 0,
    avg_completion  numeric(5,2) not null default 0,
    avg_rating      numeric(3,2) not null default 0,
    updated_at      timestamptz not null default now()
);
