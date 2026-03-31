CREATE TABLE course
(
    course_id       BIGINT AUTO_INCREMENT NOT NULL,
    code            VARCHAR(255) NULL,
    title           VARCHAR(255) NULL,
    status          VARCHAR(255) NULL,
    created_by      VARCHAR(255) NULL,
    modified_by     VARCHAR(255) NULL,
    active          BIT(1) NULL,
    organization_id BIGINT NULL,
    `visible`       VARCHAR(255) NOT NULL,
    created_at      datetime NULL,
    modified_at     datetime NULL,
    CONSTRAINT pk_course PRIMARY KEY (course_id)
);

CREATE TABLE `organization`
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    name        VARCHAR(255) NULL,
    code        VARCHAR(255) NULL,
    created_at  datetime NULL,
    modified_at datetime NULL,
    CONSTRAINT pk_organization PRIMARY KEY (id)
);

CREATE TABLE users
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    name            VARCHAR(255) NOT NULL,
    user_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    `role`          VARCHAR(255) NOT NULL,
    organization_id BIGINT       NOT NULL,
    created_at      datetime NULL,
    modified_at     datetime NULL,
    created_by      BIGINT NULL,
    modified_by     BIGINT NULL,
    status          VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE `organization`
    ADD CONSTRAINT uc_organization_code UNIQUE (code);

ALTER TABLE `organization`
    ADD CONSTRAINT uc_organization_name UNIQUE (name);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_user_name UNIQUE (user_name);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES `organization` (id);