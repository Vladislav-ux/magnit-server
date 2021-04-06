package ru.magnit.demo.service;

import ru.magnit.demo.dto.Response;
import ru.magnit.demo.entity.Status;

public interface StatusService {

    Status addStatus(Status status);
//    Status getStatusByEmail(String email);
    Response deleteStatus(Status status);
}
