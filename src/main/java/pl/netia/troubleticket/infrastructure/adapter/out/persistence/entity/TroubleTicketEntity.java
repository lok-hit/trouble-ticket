package pl.netia.troubleticket.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trouble_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TroubleTicketEntity {

    @Id
    private String id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "service_id", nullable = false)
    private long serviceId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<NoteEntity> notes = new ArrayList<>();
}
