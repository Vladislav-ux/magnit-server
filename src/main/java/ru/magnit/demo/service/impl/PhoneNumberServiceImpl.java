package ru.magnit.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.magnit.demo.dto.Response;
import ru.magnit.demo.dto.ResponseStatus;
import ru.magnit.demo.entity.PhoneNumber;
import ru.magnit.demo.repository.PhoneNumberRepository;
import ru.magnit.demo.service.PhoneNumberService;

import java.util.List;

@Service
public class PhoneNumberServiceImpl implements PhoneNumberService {

    @Autowired
    PhoneNumberRepository phoneNumberRepository;


    @Override
    public PhoneNumber addPhone(PhoneNumber phoneNumber) {
        return phoneNumberRepository.save(phoneNumber);
    }

    @Override
    public List<PhoneNumber> getPhonesByEmail(String email) {
        return (List<PhoneNumber>) phoneNumberRepository.findPhoneNumbersByEmail(email);
    }

    @Override
    public Response deletePhone(PhoneNumber phoneNumber) {
        phoneNumberRepository.delete(phoneNumber);
        return new Response(ResponseStatus.SUCCESS, "phone has been deleted");
    }
}
