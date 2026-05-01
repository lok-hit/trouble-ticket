package pl.netia.troubleticket.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CloseTicketRequest(

        @NotNull
        @Pattern(regexp = "closed", message = "status must be 'closed'")
        String status
) {}
