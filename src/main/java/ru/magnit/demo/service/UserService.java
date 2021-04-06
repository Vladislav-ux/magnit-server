package ru.magnit.demo.service;


import ru.magnit.demo.dto.Response;
import ru.magnit.demo.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User addUser(User user);
    Optional<User> getUserByEmail(String email);
    Response deleteUser(User user);
    Iterable<User> getAllUsers();
    //update all fields

}
