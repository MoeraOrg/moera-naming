package org.moera.naming.data;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SigningKeyRepository extends JpaRepository<SigningKey, Long> {

    @Query("select s from SigningKey s where s.registeredName.nameGeneration=?1 order by s.created asc")
    List<SigningKey> findAllKeys(NameGeneration nameGeneration, Pageable page);

}
