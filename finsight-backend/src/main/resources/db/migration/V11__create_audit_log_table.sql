CREATE TABLE audit_log
(
    id               UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    action           NVARCHAR(50)     NOT NULL,
    actor_user_id    UNIQUEIDENTIFIER NOT NULL,
    actor_full_name  NVARCHAR(255)    NOT NULL,
    target_user_id   UNIQUEIDENTIFIER NOT NULL,
    target_full_name NVARCHAR(255)    NOT NULL,
    request_id       NVARCHAR(36),
    archived         BIT              NOT NULL DEFAULT 0,
    archived_at      DATETIMEOFFSET(7),
    created_at       DATETIMEOFFSET(7)     NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_at       DATETIMEOFFSET(7)     NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    created_by       NVARCHAR(255)    DEFAULT N'SYSTEM',
    updated_by       NVARCHAR(255)    DEFAULT N'SYSTEM',

    CONSTRAINT pk_audit_log PRIMARY KEY (id)
);

CREATE INDEX ix_audit_log_created_at ON audit_log (created_at DESC);
CREATE INDEX ix_audit_log_actor      ON audit_log (actor_user_id);
CREATE INDEX ix_audit_log_target     ON audit_log (target_user_id);
CREATE INDEX ix_audit_log_action     ON audit_log (action);
