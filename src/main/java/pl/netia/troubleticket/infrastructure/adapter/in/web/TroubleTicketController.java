package pl.netia.troubleticket.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.netia.troubleticket.domain.model.Note;
import pl.netia.troubleticket.domain.model.TroubleTicket;
import pl.netia.troubleticket.domain.port.in.*;
import pl.netia.troubleticket.infrastructure.adapter.in.web.dto.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/troubleTicket")
@RequiredArgsConstructor
public class TroubleTicketController {

    private final CreateTroubleTicketUseCase createUseCase;
    private final ListTroubleTicketsUseCase listUseCase;
    private final GetTroubleTicketUseCase getUseCase;
    private final CloseTroubleTicketUseCase closeUseCase;
    private final AddNoteUseCase addNoteUseCase;

    @PostMapping
    public ResponseEntity<TroubleTicketResponse> create(
            @Valid @RequestBody CreateTroubleTicketRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String tenantId = jwt.getSubject();

        CreateTroubleTicketUseCase.Command command = new CreateTroubleTicketUseCase.Command(
                tenantId,
                request.externalId(),
                request.serviceId(),
                request.description(),
                request.note()
        );

        CreateTroubleTicketUseCase.Result result = createUseCase.create(command);
        TroubleTicketResponse body = TroubleTicketResponse.from(result.ticket());
        URI location = locationOf(result.ticket().id());

        return result.created()
                ? ResponseEntity.created(location).body(body)
                : ResponseEntity.ok().location(location).body(body);
    }

    @GetMapping
    public ResponseEntity<List<TroubleTicketSummaryResponse>> list(
            @AuthenticationPrincipal Jwt jwt) {

        List<TroubleTicketSummaryResponse> body = listUseCase.listByTenant(jwt.getSubject())
                .stream()
                .map(TroubleTicketSummaryResponse::from)
                .toList();

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TroubleTicketResponse> getById(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        TroubleTicket ticket = getUseCase.getById(id, jwt.getSubject());
        return ResponseEntity.ok(TroubleTicketResponse.from(ticket));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TroubleTicketResponse> close(
            @PathVariable String id,
            @Valid @RequestBody CloseTicketRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        TroubleTicket ticket = closeUseCase.close(id, jwt.getSubject());
        return ResponseEntity.ok(TroubleTicketResponse.from(ticket));
    }

    @PostMapping("/{id}/note")
    public ResponseEntity<NoteResponse> addNote(
            @PathVariable String id,
            @Valid @RequestBody AddNoteRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        AddNoteUseCase.Command command = new AddNoteUseCase.Command(id, jwt.getSubject(), request.text());
        Note note = addNoteUseCase.addNote(command);
        return ResponseEntity.status(201).body(NoteResponse.from(note));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private URI locationOf(String id) {
        return ServletUriComponentsBuilder.fromCurrentRequestUri()
                .replacePath("/api/v1/troubleTicket/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
