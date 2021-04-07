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

    @Override
    public Response changeFirstName(String email, String newName) {
        Optional<User> user = userRepository.findById(email);
        if(user.isPresent()){
            user.get().setFirst_name(newName);
            try {
                userRepository.save(user.get());
                return new Response(ResponseStatus.SUCCESS, "first name was updated");
            }catch (Exception e){
                return new Response(ResponseStatus.ERROR, e.getMessage());
            }
        }

        return new Response(ResponseStatus.ERROR, "first name was not updated");
    }

    @Override
    public Response changeLastName(String email, String newName) {
        Optional<User> user = userRepository.findById(email);
        if(user.isPresent()){
            user.get().setLast_name(newName);
            try {
                userRepository.save(user.get());
                return new Response(ResponseStatus.SUCCESS, "last name was updated");
            }catch (Exception e){
                return new Response(ResponseStatus.ERROR, e.getMessage());
            }
        }

        return new Response(ResponseStatus.ERROR, "last name was not updated");
    }

    @Override
    public Response changeMiddleName(String email, String newName) {
        Optional<User> user = userRepository.findById(email);
        if(user.isPresent()){
            user.get().setMiddle_name(newName);
            try {
                userRepository.save(user.get());
                return new Response(ResponseStatus.SUCCESS, "middle name was updated");
            }catch (Exception e){
                return new Response(ResponseStatus.ERROR, e.getMessage());
            }
        }

        return new Response(ResponseStatus.ERROR, "middle name was not updated");
    }

    @Override
    public Response changeDivision(String email, String newDivision) {
        Optional<User> user = userRepository.findById(email);
        if(user.isPresent()){
            user.get().setDivision(newDivision);
            try {
                userRepository.save(user.get());
                return new Response(ResponseStatus.SUCCESS, "division was updated");
            }catch (Exception e){
                return new Response(ResponseStatus.ERROR, e.getMessage());
            }
        }

        return new Response(ResponseStatus.ERROR, "division was not updated");
    }

    @Override
    public Response changePost(String email, String newPost) {
        Optional<User> user = userRepository.findById(email);
        if(user.isPresent()){
            user.get().setPost(newPost);
            try {
                userRepository.save(user.get());
                return new Response(ResponseStatus.SUCCESS, "post was updated");
            }catch (Exception e){
                return new Response(ResponseStatus.ERROR, e.getMessage());
            }
        }

        return new Response(ResponseStatus.ERROR, "post was not updated");
    }

}
