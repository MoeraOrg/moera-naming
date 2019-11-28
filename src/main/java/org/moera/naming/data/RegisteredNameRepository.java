package org.moera.naming.data;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RegisteredNameRepository extends JpaRepository<RegisteredName, NameGeneration> {

    @Query("select r from RegisteredName r where r.nameGeneration.name = ?1 order by r.nameGeneration.generation desc")
    List<RegisteredName> findAllGenerations(String name, Pageable page);

    @Query("select max(r.nameGeneration.generation) from RegisteredName r where r.nameGeneration.name = ?1")
    Integer findLatestGenerationNumber(String name);

}
