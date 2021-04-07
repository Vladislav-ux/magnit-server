package ru.magnit.demo.controllers;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.magnit.demo.dto.Response;
import ru.magnit.demo.dto.ResponseStatus;
import ru.magnit.demo.entity.PhoneNumber;
import ru.magnit.demo.entity.User;
import ru.magnit.demo.service.PhoneNumberService;
import ru.magnit.demo.service.StatusService;
import ru.magnit.demo.service.UserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

    @GetMapping("/lk/{email}")
    public Response authorization(@PathVariable String email, HttpServletRequest request, HttpServletResponse response){
        Optional<User> user = userService.getUserByEmail(email);
        if(user.isPresent()){
            //Сессия и куки
            HttpSession session = request.getSession();
            session.setAttribute("email", email);
            session.setAttribute("status", user.get().getStatus());
            //неактивность до закрытия браузера
            session.setMaxInactiveInterval(-1);


            Cookie cookie1 = new Cookie("email", email);
            cookie1.setMaxAge(-1);
            Cookie cookie2 = new Cookie("status", user.get().getStatus().getStatus() + "");
            cookie2.setMaxAge(-1);

            response.addCookie(cookie1);
            response.addCookie(cookie2);

            return new Response(ResponseStatus.SUCCESS, "user exists");
        }
        return new Response(ResponseStatus.ERROR, "user does not exist");
    }

    @PostMapping("/user")
    public User getUserByEmail(@RequestHeader("Authorization") String email){
        return userService.getUserByEmail(email).get();
    }

    @PostMapping("/delete_number")
    public Response deletePhone(@RequestHeader("Authorization") String email, @RequestParam(name="phone") String oldPhone){
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(oldPhone);
        phoneNumber.setUser(new User());
        phoneNumber.getUser().setEmail(email);
        return phoneNumberService.deletePhone(phoneNumber);
    }

    @PostMapping("/add_number")
    public void addPhone(@RequestHeader("Authorization") String email, @RequestParam(name="phone") String newPhone){
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(newPhone);
        phoneNumber.setUser(new User());
        phoneNumber.getUser().setEmail(email);
        phoneNumberService.addPhone(phoneNumber);
    }

    @GetMapping("/get_numbers")
    public List<String> getNumbers (@RequestHeader("Authorization") String email){
        List<String> numbers = new ArrayList<>();
        for (PhoneNumber p:
             phoneNumberService.getPhonesByEmail(email)) {
            numbers.add(p.getNumber());
        }
        return numbers;
    }


    //get all users for admin and moderator
    @GetMapping("/users")
    public List<User> getAllUsers(){
        Iterator<User> iter = userService.getAllUsers().iterator();
        List<User> users = new ArrayList<>();
        while(iter.hasNext()){
            users.add(iter.next());
        }

        return users;
    }

    //////////Modifying methods

    //Delete user
//    @GetMapping("/delete_user")
//    public Response deleteUser(@RequestHeader("Authorization") String email){
//        Optional<User> user = userService.getUserByEmail(email);
//        if(user.isPresent()) {
//            return userService.deleteUser(user.get());
//        }
//
//        return new Response(ResponseStatus.ERROR, "no such user was found");
//    }

    //Add new user
//    @PostMapping("/add_user")
//    public Response addNewUser(@RequestBody JSONObject userObject){
//        User user = new User();
//        user.setEmail(userObject.getString("email"));
//        user.setFirst_name(userObject.getString("first_name"));
//        user.setLast_name(userObject.getString("last_name"));
//        user.setMiddle_name(userObject.getString("middle_name"));
//        user.setDivision();
//
//
//    }

    //Modifying first_name
    @PostMapping("/change_first_name")
    public Response changeFirstName(@RequestHeader("Authorization") String email, @RequestParam(name="first_name") String firstName){
         return userService.changeFirstName(email, firstName);
    }

    //Modifying last_name
    @PostMapping("/change_last_name")
    public Response changeLastName(@RequestHeader("Authorization") String email, @RequestParam(name="last_name") String lastName){
        return userService.changeLastName(email, lastName);
    }

    //Modifying middle_name
    @PostMapping("/change_middle_name")
    public Response changeMiddleName(@RequestHeader("Authorization") String email, @RequestParam(name="middle_name") String middleName){
        return userService.changeMiddleName(email, middleName);
    }

    //Modifying birthday

    //Modifying post
    @PostMapping("/change_post")
    public Response changePostName(@RequestHeader("Authorization") String email, @RequestParam(name="post") String post){
        return userService.changePost(email, post);
    }

    //Modifying division
    @PostMapping("/change_division")
    public Response changeDivisionName(@RequestHeader("Authorization") String email, @RequestParam(name="division") String division){
        return userService.changeDivision(email, division);
    }

    //Modifying number
//    @PostMapping("/change_phone")
//    public Response changePhone(@RequestHeader("Authorization") String email, @RequestParam String currentPhone){
//        return userService.changeFirstName(email, currentPhone);
//    }





}
