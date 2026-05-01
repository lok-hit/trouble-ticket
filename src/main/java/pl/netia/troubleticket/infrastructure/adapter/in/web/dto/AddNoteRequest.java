package pl.netia.troubleticket.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddNoteRequest(

        @NotBlank
        String text
) {}
