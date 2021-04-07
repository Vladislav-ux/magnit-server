package ru.magnit.demo.service;

import ru.magnit.demo.dto.Response;
import ru.magnit.demo.entity.PhoneNumber;

import java.util.List;

public interface PhoneNumberService {

    PhoneNumber addPhone(PhoneNumber phoneNumber);
    List<PhoneNumber> getPhonesByEmail(String email);
    Response deletePhone(PhoneNumber phoneNumber);
    void deletePhonesByEmail(String email);

}
