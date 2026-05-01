package pl.netia.troubleticket.domain.port.in;

import pl.netia.troubleticket.domain.model.TroubleTicket;

public interface GetTroubleTicketUseCase {

    TroubleTicket getById(String id, String tenantId);
}
