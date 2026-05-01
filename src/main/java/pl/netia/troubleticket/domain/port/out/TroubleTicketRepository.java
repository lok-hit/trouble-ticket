package pl.netia.troubleticket.domain.port.out;

import pl.netia.troubleticket.domain.model.Note;
import pl.netia.troubleticket.domain.model.TroubleTicket;
import pl.netia.troubleticket.domain.model.TroubleTicketStatus;

import java.util.List;
import java.util.Optional;

public interface TroubleTicketRepository {

    TroubleTicket save(TroubleTicket ticket);

    Optional<TroubleTicket> findById(String id);

    Optional<TroubleTicket> findByTenantIdAndExternalId(String tenantId, String externalId);

    List<TroubleTicket> findAllByTenantId(String tenantId);

    TroubleTicket updateStatus(String id, TroubleTicketStatus status);

    Note addNote(String ticketId, String text);
}
