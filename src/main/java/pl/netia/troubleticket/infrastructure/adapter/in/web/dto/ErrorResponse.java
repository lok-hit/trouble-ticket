package pl.netia.troubleticket.infrastructure.adapter.in.web.dto;

public record ErrorResponse(
        String code,
        String message,
        String requestId
) {}
