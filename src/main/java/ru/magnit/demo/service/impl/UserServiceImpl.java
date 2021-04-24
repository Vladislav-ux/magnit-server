package ru.magnit.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.magnit.demo.dto.Response;
import ru.magnit.demo.dto.ResponseStatus;
import ru.magnit.demo.entity.PhoneNumber;
import ru.magnit.demo.entity.User;
import ru.magnit.demo.repository.UserRepository;
import ru.magnit.demo.service.PhoneNumberService;
import ru.magnit.demo.service.UserService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    PhoneNumberService phoneNumberService;

    @Override
    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findById(email);
    }

    @Override
    public Response deleteUserByEmail(String email) {
        phoneNumberService.deletePhonesByEmail(email);
        userRepository.deleteByEmail(email);
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
                return new Response(ResponseStatus.ERROR, "first name was not updated");
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
    public Response changePhoneNumber(String email, String oldNumber, String newNumber) {
        try {
            PhoneNumber phoneNumber = phoneNumberService.getPhoneNumberByEmailAndPhone(email, oldNumber);
            phoneNumberService.deletePhone(phoneNumber);

            phoneNumber.setNumber(newNumber);
            phoneNumberService.addPhone(phoneNumber);

            return new Response(ResponseStatus.SUCCESS, "phone number was updated");
        }catch (Exception e) {
            return new Response(ResponseStatus.ERROR, "phone number was updated");
        }
    }

    @Override
    public Response changeBirthday(String email, Date birthday) {
        Optional<User> user = userRepository.findById(email);
        if(user.isPresent()){
            user.get().setBirthday(birthday);
            try {
                userRepository.save(user.get());
                return new Response(ResponseStatus.SUCCESS, "birthday was updated");
            }catch (Exception e){
                return new Response(ResponseStatus.ERROR, e.getMessage());
            }
        }

        return new Response(ResponseStatus.ERROR, "birthday was not updated");
    }

    @Override
    public List<User> searchByFirstName(String firstName) {
        return (List<User>)userRepository.findByFirstName(firstName);
    }

    @Override
    public List<User> searchByLastName(String lastName) {
        return (List<User>)userRepository.findByLastName(lastName);
    }

    @Override
    public List<User> searchByMiddleName(String middleName) {
        return (List<User>)userRepository.findByMiddleName(middleName);
    }

    @Override
    public List<User> searchByEmail(String email) {
        return (List<User>)userRepository.findByEmail(email);
    }

    @Override
    public List<User> searchByPost(String post) {
        return (List<User>)userRepository.findByPost(post);

    }

    @Override
    public List<User> searchByDivision(String division) {
        return (List<User>)userRepository.findByDivision(division);
    }

    @Override
    public List<User> searchByStatus(String status) {
        return (List<User>)userRepository.findByStatus(status);
    }

    @Override
    public List<User> searchByBirthday(Date birthday) {
        return (List<User>) userRepository.findByBirthday(birthday);
    }

//    @Override
//    public List<User> searchByPhoneNumber(String phoneNumber) {
//        return null;
//    }

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
