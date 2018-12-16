package org.moera.naming.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RegisteredNameRepository extends JpaRepository<RegisteredName, NameGeneration> {

    @Query("select nameGeneration from RegisteredName r where r.nameGeneration.name=?1"
            + " order by r.nameGeneration.generation desc")
    List<NameGeneration> getNameGenerations(String name);

}
