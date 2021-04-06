package ru.magnit.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.magnit.demo.dto.Response;
import ru.magnit.demo.dto.ResponseStatus;
import ru.magnit.demo.entity.User;
import ru.magnit.demo.repository.UserRepository;
import ru.magnit.demo.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Override
    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findById(email);
    }

    @Override
    public Response deleteUser(User user) {
        userRepository.delete(user);
        return new Response(ResponseStatus.SUCCESS, "user has been deleted");
    }

    @Override
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }


}
