package pl.netia.troubleticket.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateTroubleTicketRequest(

        @NotBlank
        String externalId,

        @NotNull
        @Min(1)
        Long serviceId,

        @NotBlank
        String description,

        @NotNull
        @Pattern(regexp = "new", message = "status must be 'new'")
        String status,

        @NotBlank
        String note
) {}
