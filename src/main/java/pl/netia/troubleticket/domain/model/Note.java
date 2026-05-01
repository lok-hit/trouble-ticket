package pl.netia.troubleticket.domain.model;

import java.time.Instant;

public record Note(
        String id,
        String text,
        Instant date
) {}
