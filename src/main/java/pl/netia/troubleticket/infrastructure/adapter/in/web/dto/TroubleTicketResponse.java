package pl.netia.troubleticket.infrastructure.adapter.in.web.dto;

import pl.netia.troubleticket.domain.model.TroubleTicket;
import pl.netia.troubleticket.domain.model.TroubleTicketStatus;

import java.util.List;

public record TroubleTicketResponse(
        String id,
        String externalId,
        long serviceId,
        String description,
        String status,
        List<NoteResponse> notes
) {
    public static TroubleTicketResponse from(TroubleTicket ticket) {
        return new TroubleTicketResponse(
                ticket.id(),
                ticket.externalId(),
                ticket.serviceId(),
                ticket.description(),
                toApiStatus(ticket.status()),
                ticket.notes().stream().map(NoteResponse::from).toList()
        );
    }

    static String toApiStatus(TroubleTicketStatus status) {
        return switch (status) {
            case NEW          -> "new";
            case ACKNOWLEDGED -> "acknowledged";
            case IN_PROGRESS  -> "inProgress";
            case RESOLVED     -> "resolved";
            case CLOSED       -> "closed";
            case REJECTED     -> "rejected";
        };
    }
}
