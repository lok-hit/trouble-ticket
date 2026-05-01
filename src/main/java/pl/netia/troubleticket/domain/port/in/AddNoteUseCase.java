package pl.netia.troubleticket.domain.port.in;

import pl.netia.troubleticket.domain.model.Note;

public interface AddNoteUseCase {

    record Command(String ticketId, String tenantId, String text) {}

    Note addNote(Command command);
}
