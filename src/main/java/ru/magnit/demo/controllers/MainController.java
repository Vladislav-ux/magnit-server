package ru.magnit.demo.controllers;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.magnit.demo.ServerApplication;
import ru.magnit.demo.dto.CodeStorage;
import ru.magnit.demo.dto.Response;
import ru.magnit.demo.dto.ResponseStatus;
import ru.magnit.demo.dto.SMSCSender;
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
            if(user.getBirthday() != null) {
                aRow.createCell(5).setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(user.getBirthday()));
            }
            aRow.createCell(6).setCellValue(user.getDivision());
            aRow.createCell(7).setCellValue(user.getPost());
            if(user.getStatus() != null) {
                aRow.createCell(8).setCellValue(user.getStatus().getStatus_name());
            }

            List<PhoneNumber> phoneNumbers = phoneNumberService.getPhonesByEmail(user.getEmail());
            for (int i = 0; i < phoneNumbers.size(); i++) {
                aRow.createCell(9 + i).setCellValue(phoneNumbers.get(i).getNumber());
            }

        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment; filename=myfile.xlsx");
        workbook.write(response.getOutputStream());

    }

    @PostMapping("/import")
    public Response importData(@RequestParam("excel_file") MultipartFile excelfile) {
        System.out.println(excelfile.getContentType() + " excel = " + excelfile);
        try {
            int i = 1;
            XSSFWorkbook workbook = new XSSFWorkbook(excelfile.getInputStream());
            XSSFSheet worksheet = workbook.getSheetAt(0);

            while (i <= worksheet.getLastRowNum()) {
                User user = new User();
                XSSFRow row = worksheet.getRow(i++);

                //TODO посмотреть, какое из полей можно оставить пустым
                //TODO если любое поле будет пустым, то будет ошибка

                user.setEmail(row.getCell(0).getStringCellValue());
                user.setFirst_name(row.getCell(1).getStringCellValue());
                user.setLast_name(row.getCell(2).getStringCellValue());
                user.setMiddle_name(row.getCell(3).getStringCellValue());
                user.setAvatar(row.getCell(4).getStringCellValue());
                user.setBirthday(row.getCell(5).getDateCellValue());
                user.setDivision(row.getCell(6).getStringCellValue());
                user.setPost(row.getCell(7).getStringCellValue());

                String statusName = row.getCell(8).getStringCellValue();
                Status status = new Status();
                switch (statusName) {
                    case "user":
                        status.setStatus(1);
                        status.setStatus_name("user");
                        user.setStatus(new Status());
                        break;
                    case "moderator":
                        status.setStatus(3);
                        status.setStatus_name("moderator");
                        user.setStatus(new Status());
                        break;
                    case "admin":
                        status.setStatus(2);
                        status.setStatus_name("admin");
                        user.setStatus(new Status());
                        break;
                    default:
                        status.setStatus(1);
                        status.setStatus_name("user");
                        user.setStatus(new Status());
                        break;
                }

                //добавление пользователя
                Response response = addNewUser(user);
                //TODO добавляем номера только тогда, когда пользователь был успешно добавлен
                if (response.getStatus() == ResponseStatus.SUCCESS) {
                    for (int j = 1; j <= 5; j++) {
                        try {
                            String phone = row.getCell(8 + i).getStringCellValue();
                            addNumberByAdmin(user.getEmail(), phone);
                        } catch (Exception ex) {
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            return new Response(ResponseStatus.ERROR, "can not import data");
        }

        return new Response(ResponseStatus.SUCCESS, "all data downloaded");
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
    public Response deletePhone(@RequestHeader("Authorization") String email,
                                @RequestParam(name = "phone") String oldPhone) {
        //TODO исправить new User()
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(oldPhone);
        phoneNumber.setUser(new User());
        phoneNumber.getUser().setEmail(email);
        return phoneNumberService.deletePhone(phoneNumber);
    }

    //Добавление номера любому пользователю без кода подтверждения
    @PostMapping("/add_number_admin")
    public Response addNumberByAdmin(@RequestHeader("Authorization") String email,
                                     @RequestParam(name = "phone") String newPhone) {
        try {
            //TODO исправить new User()
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setNumber(newPhone);
            phoneNumber.setUser(new User());
            phoneNumber.getUser().setEmail(email);
            phoneNumberService.addPhone(phoneNumber);
            return new Response(ResponseStatus.SUCCESS, "phone number was added");
        } catch (Exception e) {
            return new Response(ResponseStatus.ERROR, "phone was not added");
        }
    }

    @Autowired
    private CodeStorage codeStorage;

    @PostMapping("/add_number")
    public void addPhone(@RequestParam(name = "phone") String newPhone) {
        SMSCSender sd = new SMSCSender();
        codeStorage.generateCode();
//        String[] ret = sd.send_sms(newPhone, "Your password : " + codeStorage.getCode(), 0, "", "", 0, "Magnit", "");
    }

    @PostMapping("/send_phone_code")
    public Response sendPhoneCode(@RequestHeader("Authorization") String email,
                                  @RequestParam(name = "phone") String newPhone,
                                  @RequestParam(name = "code") int code) {
        //TODO исправить new User()
//        if (code == codeStorage.getCode()) {
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setNumber(newPhone);
            phoneNumber.setUser(new User());
            phoneNumber.getUser().setEmail(email);
            phoneNumberService.addPhone(phoneNumber);
            return new Response(ResponseStatus.SUCCESS, "phone number was added");
//        }

//        return new Response(ResponseStatus.ERROR, "code is invalid");
    }

    //TODO убрать этот метод и возвращать все параметры для каждого User
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
    public List<User> getAllUsers(@RequestParam(name = "start_index") int startIndex,
                                  @RequestParam(name = "last_index") int lastIndex) {
        Iterator<User> iter = userService.getAllUsers().iterator();
        List<User> users = new ArrayList<>();
        while (iter.hasNext()) {
            users.add(iter.next());
        }

        return getLimitList(users, startIndex, lastIndex);
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
            return new Response(ResponseStatus.SUCCESS, "user was added");
        }
        return new Response(ResponseStatus.ERROR, "user was not added");
    }

    //Modifying first_name
    @PostMapping("/change_first_name")
    public Response changeFirstName(@RequestHeader("Authorization") String email,
                                    @RequestParam(name = "first_name") String firstName) {
        return userService.changeFirstName(email, firstName);
    }

    //Modifying last_name
    @PostMapping("/change_last_name")
    public Response changeLastName(@RequestHeader("Authorization") String email,
                                   @RequestParam(name = "last_name") String lastName) {
        return userService.changeLastName(email, lastName);
    }

    //Modifying middle_name
    @PostMapping("/change_middle_name")
    public Response changeMiddleName(@RequestHeader("Authorization") String email,
                                     @RequestParam(name = "middle_name") String middleName) {
        return userService.changeMiddleName(email, middleName);
    }

    //TODO Modifying birthday
    //Modifying birthday
    @PostMapping("/change_birthday")
    public Response changeBirthday(@RequestHeader("Authorization") String email,
                                     @RequestParam(name = "birthday") Date birthday) {

        return userService.changeBirthday(email, birthday);
    }

    //Modifying post
    @PostMapping("/change_post")
    public Response changePostName(@RequestHeader("Authorization") String email,
                                   @RequestParam(name = "post") String post) {
        return userService.changePost(email, post);
    }

    //Modifying division
    @PostMapping("/change_division")
    public Response changeDivisionName(@RequestHeader("Authorization") String email,
                                       @RequestParam(name = "division") String division) {
        return userService.changeDivision(email, division);
    }

    //Modifying number
    @PostMapping("/change_phone")
    public Response changePhone(@RequestHeader("Authorization") String email,
                                @RequestParam(name = "old_number") String oldPhone,
                                @RequestParam(name = "new_number") String newPhone) {
        return userService.changePhoneNumber(email, oldPhone, newPhone);
    }

    ///////sorting methods

    //sort by first name

    @PostMapping("/sort_fio")
    public List<User> sortByFIO(@RequestParam(required = false) List<User> list,
                                @RequestParam(name = "start_index") int startIndex,
                                @RequestParam(name = "last_index") int lastIndex) {
        List<User> users = null;

        if (list == null) {
            users = (List<User>) userService.getAllUsers();
        } else {
            users = list;
        }
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                //Заказчик попросил сортировать по ФИО именно так
                String fio1 = o1.getLast_name() + o1.getFirst_name() + o1.getMiddle_name();
                String fio2 = o2.getLast_name() + o2.getFirst_name() + o2.getMiddle_name();

                return fio1.compareTo(fio2);
//                return o1.getFirst_name().compareTo(o2.getFirst_name());
            }
        });

        return getLimitList(users, startIndex, lastIndex);
    }

    //sort by email
    @GetMapping("/sort_email")
    public List<User> sortByEmail(@RequestParam(required = false) List<User> list,
                                  @RequestParam(name = "start_index") int startIndex,
                                  @RequestParam(name = "last_index") int lastIndex) {
        List<User> users = null;

        if (list == null) {
            users = (List<User>) userService.getAllUsers();
        } else {
            users = list;
        }

        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getEmail().compareTo(o2.getEmail());
            }
        });

        return getLimitList(users, startIndex, lastIndex);
    }

    //sort by status
    @GetMapping("/sort_status")
    public List<User> sortByStatus(@RequestParam(required = false) List<User> list,
                                   @RequestParam(name = "start_index") int startIndex,
                                   @RequestParam(name = "last_index") int lastIndex) {
        List<User> users = null;

        if (list == null) {
            users = (List<User>) userService.getAllUsers();
        } else {
            users = list;
        }
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                if (o1.getStatus().getStatus() == o2.getStatus().getStatus()) return 0;
                else if (o1.getStatus().getStatus() > o2.getStatus().getStatus()) return 1;
                else return -1;
            }
        });

        return getLimitList(users, startIndex, lastIndex);
    }

    //sort by division
    @GetMapping("/sort_division")
    public List<User> sortByDivision(@RequestParam(required = false) List<User> list,
                                     @RequestParam(name = "start_index") int startIndex,
                                     @RequestParam(name = "last_index") int lastIndex) {
        List<User> users = null;

        if (list == null) {
            users = (List<User>) userService.getAllUsers();
        } else {
            users = list;
        }
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getDivision().compareTo(o2.getDivision());
            }
        });

        return getLimitList(users, startIndex, lastIndex);
    }

    //sort by post
    @GetMapping("/sort_post")
    public List<User> sortByPost(@RequestParam(required = false) List<User> list,
                                 @RequestParam(name = "start_index") int startIndex,
                                 @RequestParam(name = "last_index") int lastIndex) {
        List<User> users = null;

        if (list == null) {
            users = (List<User>) userService.getAllUsers();
        } else {
            users = list;
        }

        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getPost().compareTo(o2.getPost());
            }
        });

        return getLimitList(users, startIndex, lastIndex);
    }

    //sort by birthday
    //TODO сделать сортировку
    @GetMapping("/sort_birthday")
    public List<User> sortByBirthday(@RequestParam(required = false) List<User> list,
                                     @RequestParam(name = "start_index") int startIndex,
                                     @RequestParam(name = "last_index") int lastIndex) {
        List<User> users = null;

        if (list == null) {
            users = (List<User>) userService.getAllUsers();
        } else {
            users = list;
        }
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                if (o1.getBirthday().getTime() == o2.getBirthday().getTime()) return 0;
                else if (o1.getBirthday().getTime() > o2.getBirthday().getTime()) return 1;
                else return -1;
            }
        });

        return getLimitList(users, startIndex, lastIndex);
    }


    ///////search methods

    //search by first name
    @PostMapping("/search_first_name")
    public List<User> searchByFirstName(@RequestParam("first_name") String firstName,
                                        @RequestParam(name = "start_index") int startIndex,
                                        @RequestParam(name = "last_index") int lastIndex) {
        return getLimitList(userService.searchByFirstName(firstName), startIndex, lastIndex);
    }


    //search by last name
    @PostMapping("/search_last_name")
    public List<User> searchByLastName(@RequestParam("last_name") String lastName,
                                       @RequestParam(name = "start_index") int startIndex,
                                       @RequestParam(name = "last_index") int lastIndex) {
        return  getLimitList(userService.searchByLastName(lastName), startIndex, lastIndex);
    }

    //search by middle name
    @PostMapping("/search_middle_name")
    public List<User> searchByMiddleName(@RequestParam("middle_name") String middleName,
                                         @RequestParam(name = "start_index") int startIndex,
                                         @RequestParam(name = "last_index") int lastIndex) {
        return getLimitList(userService.searchByMiddleName(middleName), startIndex,lastIndex);
    }

    //search by status
    @PostMapping("/search_status")
    public List<User> searchByStatus(@RequestParam("status") String status,
                                     @RequestParam(name = "start_index") int startIndex,
                                     @RequestParam(name = "last_index") int lastIndex) {
        return getLimitList(userService.searchByStatus(status), startIndex, lastIndex);
    }

    //search by post
    @PostMapping("/search_post")
    public List<User> searchByPost(@RequestParam("post") String post,
                                   @RequestParam(name = "start_index") int startIndex,
                                   @RequestParam(name = "last_index") int lastIndex) {
        return getLimitList(userService.searchByPost(post), startIndex, lastIndex);
    }

    //search by division
    @PostMapping("/search_division")
    public List<User> searchByDivision(@RequestParam("division") String division,
                                       @RequestParam(name = "start_index") int startIndex,
                                       @RequestParam(name = "last_index") int lastIndex) {
        return getLimitList(userService.searchByDivision(division), startIndex, lastIndex);
    }

    //search by email
    @PostMapping("/search_email")
    public List<User> searchByEmail(@RequestParam("email") String email,
                                    @RequestParam(name = "start_index") int startIndex,
                                    @RequestParam(name = "last_index") int lastIndex) {
        return getLimitList(userService.searchByEmail(email), startIndex, lastIndex);
    }

    //TODO search by birthday
    //search by birthday
    @PostMapping("/search_birthday")
    public List<User> searchByBirthday(@RequestParam("birthday") Date birthday,
                                    @RequestParam(name = "start_index") int startIndex,
                                    @RequestParam(name = "last_index") int lastIndex) {
        System.out.println("birthday = " + birthday.toString());
        return getLimitList(userService.searchByBirthday(birthday), startIndex, lastIndex);
    }

    //TODO search by phone number
    //search by phone number
    @PostMapping("/search_number")
    public List<User> searchByNumber(@RequestParam("number") String number,
                                    @RequestParam(name = "start_index") int startIndex,
                                    @RequestParam(name = "last_index") int lastIndex) {
        List<PhoneNumber> list = phoneNumberService.searchByPhoneNumber(number);
        List<User> users = new ArrayList<>();

        for (PhoneNumber pn:
             list) {
            users.add(userService.getUserByEmail(pn.getUser().getEmail()).get());
        }

        return getLimitList(users, startIndex, lastIndex);
    }

    @PostMapping("/search_and_sort")
    public List<User> sortAndSearch(@RequestBody Map<String, Object> map,
                                    @RequestParam(name = "start_index") int startIndex,
                                    @RequestParam(name = "last_index") int lastIndex) {
        int sortValue = (int) map.get("sort");
        int searchValue = (int) map.get("search");

        List<User> list = null;

        switch (searchValue) {
            case 1:
                //search email
                list = searchByEmail((String) map.get("email"), startIndex, lastIndex);
                break;
            case 2:
                //search division
                list = searchByDivision((String) map.get("division"), startIndex, lastIndex);
                break;

            case 3:
                //search post
                list = searchByPost((String) map.get("post"), startIndex, lastIndex);
                break;

            case 4:
                //search status
                list = searchByStatus((String) map.get("status_name"), startIndex, lastIndex);
                break;

            case 5:
                // search first name
                list = searchByFirstName((String) map.get("first_name"), startIndex, lastIndex);
                break;

            case 6:
//                search last name
                list = searchByLastName((String) map.get("last_name"), startIndex, lastIndex);
                break;

            case 7:
                //search middle name
                list = searchByMiddleName((String) map.get("middle_name"), startIndex, lastIndex);
                break;

            case 8:
                //TODO search birthday
                break;

            case 9:
                //TODO search phone number
                break;

        }

        switch (sortValue){
            case 1:
                //sort email
                return sortByEmail(list, startIndex, lastIndex);
            case 2:
                //sort division
                return sortByDivision(list, startIndex, lastIndex);
            case 3:
                //sort post
                return sortByPost(list, startIndex, lastIndex);

            case 4:
                //sort status
                return sortByStatus(list, startIndex, lastIndex);

            case 5:
                // sort first name
                return sortByFIO(list, startIndex, lastIndex);

            case 6:
                //sort birthday
                return sortByBirthday(list, startIndex, lastIndex);
        }

        return null;
    }

    private List<User> getLimitList(List<User> list, int start_index, int last_index) {
        if (list == null) {
            return null;
        }

        if (last_index < list.size()) {
            return list.subList(start_index, last_index + 1);
        } else if (start_index < list.size()) {
            return list.subList(start_index, list.size());
        } else {
            return null;
        }
    }
}
