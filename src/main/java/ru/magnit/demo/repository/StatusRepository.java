package ru.magnit.demo.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.magnit.demo.entity.Status;

@Repository
public interface StatusRepository extends CrudRepository<Status, String> {
}
