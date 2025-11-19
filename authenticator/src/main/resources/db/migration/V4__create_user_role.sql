CREATE TABLE users_roles (
    user_id BIGINT REFERENCES public.users(id),
    role_id BIGINT REFERENCES public.roles(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY(user_id, role_id)
)