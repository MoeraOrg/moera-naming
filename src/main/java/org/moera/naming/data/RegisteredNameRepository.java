package org.moera.naming.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisteredNameRepository extends JpaRepository<RegisteredName, NameGeneration> {
}
