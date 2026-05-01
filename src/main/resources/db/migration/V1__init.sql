CREATE TABLE trouble_ticket (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL,
    tenant_id   VARCHAR(255) NOT NULL,
    service_id  BIGINT       NOT NULL,
    description TEXT         NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_tenant_external UNIQUE (tenant_id, external_id)
);

CREATE TABLE trouble_ticket_note (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    ticket_id  VARCHAR(36)  NOT NULL REFERENCES trouble_ticket(id) ON DELETE CASCADE,
    text       TEXT         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_ticket_tenant ON trouble_ticket (tenant_id);
CREATE INDEX idx_note_ticket   ON trouble_ticket_note (ticket_id);
