package ru.magnit.demo.service;


import ru.magnit.demo.dto.Response;
import ru.magnit.demo.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User addUser(User user);
    Optional<User> getUserByEmail(String email);
    Response deleteUserByEmail(String email);
    Iterable<User> getAllUsers();

    //update all fields
    Response changeFirstName(String email, String newName);
    Response changeLastName(String email, String newName);
    Response changeMiddleName(String email, String newName);
    Response changePost(String email, String post);
    Response changeDivision(String email, String division);

    //search all fields
    List<User> searchByFirstName(String firstName);
    List<User> searchByLastName(String lastName);
    List<User> searchByMiddleName(String middleName);
    List<User> searchByEmail(String email);
    List<User> searchByPost(String post);
    List<User> searchByDivision(String division);
    List<User> searchByStatus(String status);
//    List<User> searchByPhoneNumber(String phoneNumber);

}
