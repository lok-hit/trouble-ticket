package pl.netia.troubleticket.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.netia.troubleticket.infrastructure.adapter.out.persistence.entity.NoteEntity;

interface NoteJpaRepository extends JpaRepository<NoteEntity, String> {}
