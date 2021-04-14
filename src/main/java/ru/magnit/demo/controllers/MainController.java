package ru.magnit.demo.controllers;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.magnit.demo.dto.CodeStorage;
import ru.magnit.demo.dto.Response;
import ru.magnit.demo.dto.ResponseStatus;
import ru.magnit.demo.dto.SMSCSender;
import ru.magnit.demo.entity.PhoneNumber;
import ru.magnit.demo.entity.User;
import ru.magnit.demo.service.PhoneNumberService;
import ru.magnit.demo.service.StatusService;
import ru.magnit.demo.service.UserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController

public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private PhoneNumberService phoneNumberService;

    @Autowired
    private StatusService statusService;

    @PostMapping("/test_phone_code")
    public void addPhoneWithCode(@RequestParam String number){
        SMSCSender sd= new SMSCSender();

        String[] ret = sd.send_sms(number, "Ваш пароль: 123", 1, "", "", 0, "", "");
    }

//    @PostMapping
//    public void importData(){
//
//    }

    @GetMapping(value = "/export")
    public void exportSata(HttpServletResponse response) throws Exception {
        Iterator<User> iter = userService.getAllUsers().iterator();
        List<User> users = new ArrayList<>();
        while (iter.hasNext()) {
            users.add(iter.next());
        }

        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("All Users List");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Email");
        header.createCell(1).setCellValue("First name");
        header.createCell(2).setCellValue("Last name");
        header.createCell(3).setCellValue("Middle Name");
        header.createCell(4).setCellValue("Avatar");
        header.createCell(5).setCellValue("Birthday");
        header.createCell(6).setCellValue("Division");
        header.createCell(7).setCellValue("Post");
        header.createCell(8).setCellValue("Status");
        header.createCell(9).setCellValue("Phone number1");
        header.createCell(10).setCellValue("Phone number2");
        header.createCell(11).setCellValue("Phone number3");
        header.createCell(12).setCellValue("Phone number4");
        header.createCell(13).setCellValue("Phone number5");

        int rowNum = 1;

        for (User user : users) {
            Row aRow = sheet.createRow(rowNum++);
            aRow.createCell(0).setCellValue(user.getEmail());
            aRow.createCell(1).setCellValue(user.getFirst_name());
            aRow.createCell(2).setCellValue(user.getLast_name());
            aRow.createCell(3).setCellValue(user.getMiddle_name());
            aRow.createCell(4).setCellValue(user.getAvatar());
            aRow.createCell(5).setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(user.getBirthday()));
            aRow.createCell(6).setCellValue(user.getDivision());
            aRow.createCell(7).setCellValue(user.getPost());
            aRow.createCell(8).setCellValue(user.getStatus().getStatus_name());

            List<PhoneNumber> phoneNumbers = phoneNumberService.getPhonesByEmail(user.getEmail());
            for (int i = 0; i < phoneNumbers.size(); i++) {
                aRow.createCell(9 + i).setCellValue(phoneNumbers.get(i).getNumber());
            }

        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment; filename=myfile.xlsx");
        workbook.write(response.getOutputStream());

    }

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
        //TODO исправить new User()
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(oldPhone);
        phoneNumber.setUser(new User());
        phoneNumber.getUser().setEmail(email);
        return phoneNumberService.deletePhone(phoneNumber);
    }

    @Autowired
    private CodeStorage codeStorage;

    @PostMapping("/add_number")
    public void addPhone(@RequestParam(name = "phone") String newPhone) {
        SMSCSender sd= new SMSCSender();
        codeStorage.generateCode();
        String[] ret = sd.send_sms(newPhone, "Your password : " + codeStorage.getCode(), 1, "", "", 0, "", "");
    }

    @PostMapping("/send_phone_code")
    public Response sendPhoneCode(@RequestHeader("Authorization") String email, @RequestParam(name = "phone") String newPhone, @RequestParam(name = "code") int code){
        //TODO исправить new User()
        if(code == codeStorage.getCode()) {
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setNumber(newPhone);
            phoneNumber.setUser(new User());
            phoneNumber.getUser().setEmail(email);
            phoneNumberService.addPhone(phoneNumber);
            return new Response(ResponseStatus.SUCCESS, "phone number was added");
        }

        return new Response(ResponseStatus.ERROR, "code is invalid");
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

    //TODO Modifying birthday


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
    @PostMapping("/change_phone")
    public Response changePhone(@RequestHeader("Authorization") String email, @RequestBody Map<String, String> map){
        String oldPhone = map.get("old_number");
        String newPhone = map.get("new_number");

        return userService.changePhoneNumber(email, oldPhone, newPhone);
    }

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

    //TODO search by phone number
//    @PostMapping("/search_phone")
//    public List<User> searchByPhoneNumber(@RequestParam String phoneNumber){
//        return userService.searchByPhoneNumber(phoneNumber);
//    }

}
