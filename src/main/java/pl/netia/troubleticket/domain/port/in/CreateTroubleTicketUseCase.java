package pl.netia.troubleticket.domain.port.in;

import pl.netia.troubleticket.domain.model.TroubleTicket;

public interface CreateTroubleTicketUseCase {

    record Command(
            String tenantId,
            String externalId,
            long serviceId,
            String description,
            String note
    ) {}

    record Result(TroubleTicket ticket, boolean created) {}

    /**
     * Creates a new trouble ticket or returns an existing one
     * if (tenantId, externalId) pair already exists (idempotency).
     */
    Result create(Command command);
}
