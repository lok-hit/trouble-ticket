package pl.netia.troubleticket.infrastructure.adapter.in.web.dto;

import pl.netia.troubleticket.domain.model.TroubleTicket;

public record TroubleTicketSummaryResponse(
        String externalId,
        long serviceId,
        String description,
        String status
) {
    public static TroubleTicketSummaryResponse from(TroubleTicket ticket) {
        return new TroubleTicketSummaryResponse(
                ticket.externalId(),
                ticket.serviceId(),
                ticket.description(),
                TroubleTicketResponse.toApiStatus(ticket.status())
        );
    }
}
