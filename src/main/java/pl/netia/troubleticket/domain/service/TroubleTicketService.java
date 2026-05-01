package pl.netia.troubleticket.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.netia.troubleticket.domain.model.Note;
import pl.netia.troubleticket.domain.model.TroubleTicket;
import pl.netia.troubleticket.domain.model.TroubleTicketStatus;
import pl.netia.troubleticket.domain.port.in.*;
import pl.netia.troubleticket.domain.port.out.TroubleTicketRepository;
import pl.netia.troubleticket.shared.exception.TroubleTicketNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TroubleTicketService implements
        CreateTroubleTicketUseCase,
        ListTroubleTicketsUseCase,
        GetTroubleTicketUseCase,
        CloseTroubleTicketUseCase,
        AddNoteUseCase {

    private final TroubleTicketRepository repository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 2, backoff = @Backoff(delay = 50))
    @Transactional
    public CreateTroubleTicketUseCase.Result create(CreateTroubleTicketUseCase.Command command) {
        Optional<TroubleTicket> existing =
                repository.findByTenantIdAndExternalId(command.tenantId(), command.externalId());

        if (existing.isPresent()) {
            return new CreateTroubleTicketUseCase.Result(existing.get(), false);
        }

        TroubleTicket ticket = new TroubleTicket(
                UUID.randomUUID().toString(),
                command.externalId(),
                command.tenantId(),
                command.serviceId(),
                command.description(),
                TroubleTicketStatus.ACKNOWLEDGED,
                List.of()
        );

        TroubleTicket saved = repository.save(ticket);
        repository.addNote(saved.id(), command.note());

        TroubleTicket withNote = repository.findById(saved.id())
                .orElseThrow(() -> new TroubleTicketNotFoundException(saved.id()));

        return new CreateTroubleTicketUseCase.Result(withNote, true);
    }

    // ── List ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TroubleTicket> listByTenant(String tenantId) {
        return repository.findAllByTenantId(tenantId);
    }

    // ── Get ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TroubleTicket getById(String id, String tenantId) {
        return repository.findById(id)
                .filter(ticket -> ticket.belongsTo(tenantId))
                .orElseThrow(() -> new TroubleTicketNotFoundException(id));
    }

    // ── Close ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TroubleTicket close(String id, String tenantId) {
        TroubleTicket ticket = repository.findById(id)
                .filter(t -> t.belongsTo(tenantId))
                .orElseThrow(() -> new TroubleTicketNotFoundException(id));

        if (ticket.isClosed()) {
            return ticket;
        }

        return repository.updateStatus(id, TroubleTicketStatus.CLOSED);
    }

    // ── Add note ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Note addNote(AddNoteUseCase.Command command) {
        repository.findById(command.ticketId())
                .filter(ticket -> ticket.belongsTo(command.tenantId()))
                .orElseThrow(() -> new TroubleTicketNotFoundException(command.ticketId()));

        return repository.addNote(command.ticketId(), command.text());
    }
}
