package pl.netia.troubleticket.domain.model;

import java.util.List;

public record TroubleTicket(
        String id,
        String externalId,
        String tenantId,
        long serviceId,
        String description,
        TroubleTicketStatus status,
        List<Note> notes
) {
    public boolean belongsTo(String tenant) {
        return this.tenantId.equals(tenant);
    }

    public boolean isClosed() {
        return this.status == TroubleTicketStatus.CLOSED;
    }
}
