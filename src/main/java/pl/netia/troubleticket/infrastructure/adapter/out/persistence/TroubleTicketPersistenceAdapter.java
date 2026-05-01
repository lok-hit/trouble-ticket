package pl.netia.troubleticket.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.netia.troubleticket.domain.model.Note;
import pl.netia.troubleticket.domain.model.TroubleTicket;
import pl.netia.troubleticket.domain.model.TroubleTicketStatus;
import pl.netia.troubleticket.domain.port.out.TroubleTicketRepository;
import pl.netia.troubleticket.infrastructure.adapter.out.persistence.entity.NoteEntity;
import pl.netia.troubleticket.infrastructure.adapter.out.persistence.entity.TroubleTicketEntity;
import pl.netia.troubleticket.shared.exception.TroubleTicketNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TroubleTicketPersistenceAdapter implements TroubleTicketRepository {

    private final TroubleTicketJpaRepository ticketRepo;
    private final NoteJpaRepository noteRepo;

    @Override
    public TroubleTicket save(TroubleTicket ticket) {
        Instant now = Instant.now();
        TroubleTicketEntity entity = TroubleTicketEntity.builder()
                .id(ticket.id())
                .externalId(ticket.externalId())
                .tenantId(ticket.tenantId())
                .serviceId(ticket.serviceId())
                .description(ticket.description())
                .status(toDbStatus(ticket.status()))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toDomain(ticketRepo.save(entity));
    }

    @Override
    public Optional<TroubleTicket> findById(String id) {
        return ticketRepo.findByIdWithNotes(id).map(this::toDomain);
    }

    @Override
    public Optional<TroubleTicket> findByTenantIdAndExternalId(String tenantId, String externalId) {
        return ticketRepo.findByTenantIdAndExternalId(tenantId, externalId)
                .map(e -> ticketRepo.findByIdWithNotes(e.getId()).orElse(e))
                .map(this::toDomain);
    }

    @Override
    public List<TroubleTicket> findAllByTenantId(String tenantId) {
        return ticketRepo.findAllByTenantId(tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public TroubleTicket updateStatus(String id, TroubleTicketStatus status) {
        TroubleTicketEntity entity = ticketRepo.findByIdWithNotes(id)
                .orElseThrow(() -> new TroubleTicketNotFoundException(id));
        entity.setStatus(toDbStatus(status));
        entity.setUpdatedAt(Instant.now());
        return toDomain(ticketRepo.save(entity));
    }

    @Override
    public Note addNote(String ticketId, String text) {
        TroubleTicketEntity ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(ticketId));

        NoteEntity note = NoteEntity.builder()
                .id(UUID.randomUUID().toString())
                .ticket(ticket)
                .text(text)
                .createdAt(Instant.now())
                .build();

        NoteEntity saved = noteRepo.save(note);
        return toDomain(saved);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private TroubleTicket toDomain(TroubleTicketEntity e) {
        List<Note> notes = e.getNotes() == null
                ? List.of()
                : e.getNotes().stream().map(this::toDomain).toList();

        return new TroubleTicket(
                e.getId(),
                e.getExternalId(),
                e.getTenantId(),
                e.getServiceId(),
                e.getDescription(),
                fromDbStatus(e.getStatus()),
                notes
        );
    }

    private Note toDomain(NoteEntity e) {
        return new Note(e.getId(), e.getText(), e.getCreatedAt());
    }

    private String toDbStatus(TroubleTicketStatus status) {
        return switch (status) {
            case NEW         -> "new";
            case ACKNOWLEDGED -> "acknowledged";
            case IN_PROGRESS -> "inProgress";
            case RESOLVED    -> "resolved";
            case CLOSED      -> "closed";
            case REJECTED    -> "rejected";
        };
    }

    private TroubleTicketStatus fromDbStatus(String status) {
        return switch (status) {
            case "new"          -> TroubleTicketStatus.NEW;
            case "acknowledged" -> TroubleTicketStatus.ACKNOWLEDGED;
            case "inProgress"   -> TroubleTicketStatus.IN_PROGRESS;
            case "resolved"     -> TroubleTicketStatus.RESOLVED;
            case "closed"       -> TroubleTicketStatus.CLOSED;
            case "rejected"     -> TroubleTicketStatus.REJECTED;
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }
}
