package pl.netia.troubleticket.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.netia.troubleticket.infrastructure.adapter.out.persistence.entity.TroubleTicketEntity;

import java.util.List;
import java.util.Optional;

interface TroubleTicketJpaRepository extends JpaRepository<TroubleTicketEntity, String> {

    Optional<TroubleTicketEntity> findByTenantIdAndExternalId(String tenantId, String externalId);

    List<TroubleTicketEntity> findAllByTenantId(String tenantId);

    @Query("SELECT t FROM TroubleTicketEntity t LEFT JOIN FETCH t.notes WHERE t.id = :id")
    Optional<TroubleTicketEntity> findByIdWithNotes(@Param("id") String id);
}
