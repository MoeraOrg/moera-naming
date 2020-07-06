package org.moera.naming.data;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SigningKeyRepository extends JpaRepository<SigningKey, Long> {

    @Query("select s from SigningKey s where s.registeredName.nameGeneration = ?1 order by s.validFrom desc")
    List<SigningKey> findAllKeys(NameGeneration nameGeneration, Pageable page);

    @Query("select s from SigningKey s where s.registeredName.nameGeneration = ?1 and s.validFrom <= ?2"
            + " order by s.validFrom desc")
    List<SigningKey> findKeysValidBefore(NameGeneration nameGeneration, Timestamp before, Pageable page);

}
