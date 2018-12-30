package org.moera.naming.data;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<Operation, UUID> {
}
