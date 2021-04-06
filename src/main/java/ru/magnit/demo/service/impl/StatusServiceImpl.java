package ru.magnit.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.magnit.demo.dto.Response;
import ru.magnit.demo.dto.ResponseStatus;
import ru.magnit.demo.entity.Status;
import ru.magnit.demo.repository.StatusRepository;
import ru.magnit.demo.service.StatusService;

@Service
public class StatusServiceImpl implements StatusService {
    @Autowired
    StatusRepository statusRepository;

    @Override
    public Status addStatus(Status status) {
        return statusRepository.save(status);
    }

//    @Override
//    public Status getStatusByEmail(String email) {
//        return statusRepository.;
//    }

    @Override
    public Response deleteStatus(Status status) {
        statusRepository.delete(status);
        return new Response(ResponseStatus.SUCCESS, "status has been deleted");
    }
}
