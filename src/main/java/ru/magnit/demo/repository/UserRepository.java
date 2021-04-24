package ru.magnit.demo.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.magnit.demo.entity.User;

import java.util.Date;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.email = :email")
    void deleteByEmail(@Param("email") String email);

    @Query("select u from User u where u.first_name = :first_name")
    Iterable<User> findByFirstName(@Param("first_name") String first_name);

    @Query("select u from User u where u.last_name = :last_name")
    Iterable<User> findByLastName(@Param("last_name") String last_name);

    @Query("select u from User u where u.middle_name = :middle_name")
    Iterable<User> findByMiddleName(@Param("middle_name") String middle_name);

    @Query("select u from User u where u.email = :email")
    Iterable<User> findByEmail(@Param("email") String email);

    @Query("select u from User u where u.post = :post")
    Iterable<User> findByPost(@Param("post") String post);

    @Query("select u from User u where u.division = :division")
    Iterable<User> findByDivision(@Param("division") String division);

    @Query("select u from User u where u.status.status_name = :status")
    Iterable<User> findByStatus(@Param("status") String status);

    @Query("select u from User u where u.birthday = :birthday")
    Iterable<User> findByBirthday(@Param("birthday") Date birthday);

}
