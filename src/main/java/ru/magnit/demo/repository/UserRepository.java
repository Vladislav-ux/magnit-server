package ru.magnit.demo.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.magnit.demo.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

//    @Query("select c from Category c where c.title = :name")
//    Category findByName(@Param("name") String name);
}
