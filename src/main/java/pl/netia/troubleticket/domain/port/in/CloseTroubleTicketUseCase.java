package pl.netia.troubleticket.domain.port.in;

import pl.netia.troubleticket.domain.model.TroubleTicket;

public interface CloseTroubleTicketUseCase {

    TroubleTicket close(String id, String tenantId);
}
