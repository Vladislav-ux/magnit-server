package ru.magnit.demo.controllers;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.magnit.demo.dto.Response;
import ru.magnit.demo.dto.ResponseStatus;
import ru.magnit.demo.entity.PhoneNumber;
import ru.magnit.demo.entity.Status;
import ru.magnit.demo.entity.User;
import ru.magnit.demo.service.PhoneNumberService;
import ru.magnit.demo.service.StatusService;
import ru.magnit.demo.service.UserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController

public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private PhoneNumberService phoneNumberService;

    @Autowired
    private StatusService statusService;

    @GetMapping("/lk/{email}")
    public Response authorization(@PathVariable String email, HttpServletRequest request, HttpServletResponse response) {
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
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

    @GetMapping("/user")
    public Optional<User> getUserByEmail(@RequestHeader("Authorization") String email) {
        return userService.getUserByEmail(email);
    }

    @PostMapping("/delete_number")
    public Response deletePhone(@RequestHeader("Authorization") String email, @RequestParam(name = "phone") String oldPhone) {
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(oldPhone);
        phoneNumber.setUser(new User());
        phoneNumber.getUser().setEmail(email);
        return phoneNumberService.deletePhone(phoneNumber);
    }

    @PostMapping("/add_number")
    public void addPhone(@RequestHeader("Authorization") String email, @RequestParam(name = "phone") String newPhone) {
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(newPhone);
        phoneNumber.setUser(new User());
        phoneNumber.getUser().setEmail(email);
        phoneNumberService.addPhone(phoneNumber);
    }

    @GetMapping("/get_numbers")
    public List<String> getNumbers(@RequestHeader("Authorization") String email) {
        List<String> numbers = new ArrayList<>();
        for (PhoneNumber p :
                phoneNumberService.getPhonesByEmail(email)) {
            numbers.add(p.getNumber());
        }
        return numbers;
    }


    //get all users for admin and moderator
    @GetMapping("/users")
    public List<User> getAllUsers() {
        Iterator<User> iter = userService.getAllUsers().iterator();
        List<User> users = new ArrayList<>();
        while (iter.hasNext()) {
            users.add(iter.next());
        }

        return users;
    }

    //////////Modifying methods

    //Delete user
    @GetMapping("/delete_user")
    public Response deleteUser(@RequestHeader("Authorization") String email) {
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            return userService.deleteUserByEmail(email);
        }

        return new Response(ResponseStatus.ERROR, "no such user was found");
    }

    //Add new user
    @PostMapping("/add_user")
    //Почему-то не рабоатет с json
    public Response addNewUser(@RequestBody User user) {
        if (userService.getUserByEmail(user.getEmail()).isPresent()) {
            return new Response(ResponseStatus.ERROR, "user is already exist");
        }
        User u = userService.addUser(user);
        if (u != null) {
            return new Response(ResponseStatus.SUCCESS, "user added");
        }
        return new Response(ResponseStatus.ERROR, "user did not add");
    }

    //Modifying first_name
    @PostMapping("/change_first_name")
    public Response changeFirstName(@RequestHeader("Authorization") String email, @RequestParam(name = "first_name") String firstName) {
        return userService.changeFirstName(email, firstName);
    }

    //Modifying last_name
    @PostMapping("/change_last_name")
    public Response changeLastName(@RequestHeader("Authorization") String email, @RequestParam(name = "last_name") String lastName) {
        return userService.changeLastName(email, lastName);
    }

    //Modifying middle_name
    @PostMapping("/change_middle_name")
    public Response changeMiddleName(@RequestHeader("Authorization") String email, @RequestParam(name = "middle_name") String middleName) {
        return userService.changeMiddleName(email, middleName);
    }

    //Modifying birthday

    //Modifying post
    @PostMapping("/change_post")
    public Response changePostName(@RequestHeader("Authorization") String email, @RequestParam(name = "post") String post) {
        return userService.changePost(email, post);
    }

    //Modifying division
    @PostMapping("/change_division")
    public Response changeDivisionName(@RequestHeader("Authorization") String email, @RequestParam(name = "division") String division) {
        return userService.changeDivision(email, division);
    }

    //Modifying number
//    @PostMapping("/change_phone")
//    public Response changePhone(@RequestHeader("Authorization") String email, @RequestParam String currentPhone){
//        return userService.changeFirstName(email, currentPhone);
//    }

    ///////sorting methods

    //sort by first name
    @GetMapping("/sort_first_name")
    public List<User> sortByFirstName() {
        List<User> users = (List<User>) userService.getAllUsers();
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getFirst_name().compareTo(o2.getFirst_name());
            }
        });

        return users;
    }

    //sort by last name
    @GetMapping("/sort_last_name")
    public List<User> sortByLastName() {
        List<User> users = (List<User>) userService.getAllUsers();
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getLast_name().compareTo(o2.getLast_name());
            }
        });

        return users;
    }

    //sort by middle name
    @GetMapping("/sort_middle_name")
    public List<User> sortByMiddleName() {
        List<User> users = (List<User>) userService.getAllUsers();
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getMiddle_name().compareTo(o2.getMiddle_name());
            }
        });

        return users;
    }

    //sort by email
    @GetMapping("/sort_email")
    public List<User> sortByEmail() {
        List<User> users = (List<User>) userService.getAllUsers();
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getEmail().compareTo(o2.getEmail());
            }
        });

        return users;
    }

    //sort by status
    @GetMapping("/sort_status")
    public List<User> sortByStatus() {
        List<User> users = (List<User>) userService.getAllUsers();
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                if (o1.getStatus().getStatus() == o2.getStatus().getStatus()) return 0;
                else if (o1.getStatus().getStatus() > o2.getStatus().getStatus()) return 1;
                else return -1;
            }
        });

        return users;
    }

    //sort by division
    @GetMapping("/sort_division")
    public List<User> sortByDivision() {
        List<User> users = (List<User>) userService.getAllUsers();
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getDivision().compareTo(o2.getDivision());
            }
        });

        return users;
    }

    //sort by post
    @GetMapping("/sort_post")
    public List<User> sortByPost() {
        List<User> users = (List<User>) userService.getAllUsers();
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getPost().compareTo(o2.getPost());
            }
        });

        return users;
    }

    //sort by birthday
    @GetMapping("/sort_birthday")
    public List<User> sortByBirthday() {
        List<User> users = (List<User>) userService.getAllUsers();
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return 0;
            }
        });

        return users;
    }


    ///////search methods

    //search by first name
    @PostMapping("/search_first_name")
    public List<User> searchByFirstName(@RequestParam String firstName) {
        return userService.searchByFirstName(firstName);
    }


    //search by last name
    @PostMapping("/search_last_name")
    public List<User> searchByLastName(@RequestParam String lastName) {
        return userService.searchByLastName(lastName);
    }

    //search by middle name
    @PostMapping("/search_middle_name")
    public List<User> searchByMiddleName(@RequestParam String middleName) {
        return userService.searchByMiddleName(middleName);
    }

    //search by status
    @PostMapping("/search_status")
    public List<User> searchByStatus(@RequestParam String status) {
        return userService.searchByStatus(status);
    }

    //search by post
    @PostMapping("/search_post")
    public List<User> searchByPost(@RequestParam String post) {
        return userService.searchByPost(post);
    }

    //search by division
    @PostMapping("/search_division")
    public List<User> searchByDivision(@RequestParam String division) {
        return userService.searchByDivision(division);
    }

    //search by email
    @PostMapping("/search_email")
    public List<User> searchByEmail(@RequestParam String email) {
        return userService.searchByEmail(email);
    }

    //search by phone number
//    @PostMapping("/search_phone")
//    public List<User> searchByPhoneNumber(@RequestParam String phoneNumber){
//        return userService.searchByPhoneNumber(phoneNumber);
//    }

}
