package ru.magnit.demo.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.magnit.demo.entity.PhoneNumber;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface PhoneNumberRepository extends CrudRepository<PhoneNumber, String> {

    @Query("select p from PhoneNumber p where p.user.email = :email")
    Iterable<PhoneNumber> findPhoneNumbersByEmail(@Param("email") String email);

    @Transactional
    @Modifying
    @Query("delete from PhoneNumber p where p.user.email = :email")
    void deletePhonesByEmail(@Param("email") String email);
}
