package pl.netia.troubleticket.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.netia.troubleticket.domain.model.Note;

import java.time.Instant;

public record NoteResponse(
        String id,
        String text,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant date
) {
    public static NoteResponse from(Note note) {
        return new NoteResponse(note.id(), note.text(), note.date());
    }
}
