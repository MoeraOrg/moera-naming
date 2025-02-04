package org.moera.naming.data;

import java.util.List;
import java.util.UUID;

import org.moera.lib.naming.types.OperationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

    List<Operation> findAllByStatusOrderByAdded(OperationStatus status, Pageable pageable);

}
