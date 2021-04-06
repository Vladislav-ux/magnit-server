package ru.magnit.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.magnit.demo.dto.Response;
import ru.magnit.demo.entity.PhoneNumber;
import ru.magnit.demo.entity.User;
import ru.magnit.demo.service.PhoneNumberService;
import ru.magnit.demo.service.StatusService;
import ru.magnit.demo.service.UserService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private PhoneNumberService phoneNumberService;

    @Autowired
    private StatusService statusService;

    @GetMapping("/users")
    public List<User> getAllUsers(){
        Iterator<User> iter = userService.getAllUsers().iterator();
        List<User> users = new ArrayList<>();
        while(iter.hasNext()){
            users.add(iter.next());
        }

        return users;
    }

    @PostMapping("/user")
    public User getUserByEmail(@RequestHeader("Authorization") String email){
        return userService.getUserByEmail(email).get();
    }

    @PostMapping("/delete-number")
    public Response deletePhone(@RequestHeader("Authorization") String email, @RequestParam(name="phone") String oldPhone){
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(oldPhone);
        phoneNumber.setUser(new User());
        phoneNumber.getUser().setEmail(email);
        return phoneNumberService.deletePhone(phoneNumber);
    }

    @PostMapping("/add-number")
    public void addPhone(@RequestHeader("Authorization") String email, @RequestParam(name="phone") String newPhone){
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(newPhone);
        phoneNumber.setUser(new User());
        phoneNumber.getUser().setEmail(email);
        phoneNumberService.addPhone(phoneNumber);
    }

    @GetMapping("/get-numbers")
    public List<String> getNumbers (@RequestHeader("Authorization") String email){
        List<String> numbers = new ArrayList<>();
        for (PhoneNumber p:
             phoneNumberService.getPhonesByEmail(email)) {
            numbers.add(p.getNumber());
        }
        return numbers;
    }

}
