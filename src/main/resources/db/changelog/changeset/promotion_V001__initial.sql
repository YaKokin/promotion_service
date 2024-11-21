CREATE TABLE PriorityLevel
(
    id    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name  VARCHAR(64) NOT NULL,
    value INT         NOT NULL
);

CREATE TABLE Tariff
(
    id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name              VARCHAR(64)   NOT NULL,
    description       VARCHAR(4096),
    price             DECIMAL(10, 2) NOT NULL,
    total_impressions INT            NOT NULL,
    priority_level    BIGINT NOT NULL,
    FOREIGN KEY (priority_level) REFERENCES PriorityLevel (id)
);

CREATE TABLE Resource
(
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    source_id     BIGINT      NOT NULL,
    resource_type SMALLINT NOT NULL DEFAULT 0,
    owner_id      BIGINT      NOT NULL
);

CREATE TABLE Promotion
(
    id                    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    resource_id           BIGINT   NOT NULL,
    tariff_id             BIGINT   NOT NULL,
    remaining_impressions INT,
    status                SMALLINT NOT NULL DEFAULT 0,
    FOREIGN KEY (resource_id) REFERENCES Resource (id),
    FOREIGN KEY (tariff_id) REFERENCES Tariff (id)
);

CREATE TABLE Impression
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    promotion_id   BIGINT    NOT NULL,
    timestamp      TIMESTAMPTZ DEFAULT current_timestamp,
    viewer_user_id BIGINT NOT NULL ,
    FOREIGN KEY (promotion_id) REFERENCES Promotion (id)
);
