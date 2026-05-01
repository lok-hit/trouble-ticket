package pl.netia.troubleticket.domain.port.in;

import pl.netia.troubleticket.domain.model.TroubleTicket;

import java.util.List;

public interface ListTroubleTicketsUseCase {

    List<TroubleTicket> listByTenant(String tenantId);
}
