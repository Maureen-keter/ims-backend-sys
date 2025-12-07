package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.dto.GenericDTO;
import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.models.Request;
import com.stanbic.internMs.intern.models.User;
import com.stanbic.internMs.intern.repository.DepartmentRepository;
import com.stanbic.internMs.intern.repository.RequestRepository;
import com.stanbic.internMs.intern.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public RequestService(RequestRepository requestRepository, UserRepository userRepository, DepartmentRepository departmentRepository){
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public Request createRequest(GenericDTO dto){
        Long internId = dto.get("internId") == null ? null : Long.parseLong(dto.get("internId").toString());
        Long departmentId = dto.get("departmentId") == null ? null : Long.parseLong(dto.get("departmentId").toString());
        String reason = dto.getString("reason");

        if(internId == null) throw new ValidationException(java.util.Map.of("internId", "internId is required"));
        if(departmentId == null) throw new ValidationException(java.util.Map.of("departmentId", "departmentId is required"));

        User intern = userRepository.findById(internId).orElseThrow(()-> new ValidationException(java.util.Map.of("internId", "Intern not found")));
        var dept = departmentRepository.findById(departmentId).orElseThrow(()-> new ValidationException(java.util.Map.of("departmentId", "Department not found")));

        Request r = new Request();
        r.setIntern(intern);
        r.setDepartment(dept);
        r.setReason(reason);
        r.setStatus(Request.Status.PENDING);
        r.setCreatedAt(LocalDateTime.now());

        return requestRepository.save(r);
    }

    public List<Request> listRequests(Optional<Long> internId, Optional<Long> departmentId, Optional<Request.Status> status){
        List<Request> all = requestRepository.findAll();
        return all.stream()
                .filter(r -> internId.map(id -> r.getIntern()!=null && r.getIntern().getId().equals(id)).orElse(true))
                .filter(r -> departmentId.map(id -> r.getDepartment()!=null && r.getDepartment().getId().equals(id)).orElse(true))
                .filter(r -> status.map(s -> r.getStatus()==s).orElse(true))
                .collect(Collectors.toList());
    }

    public Request respondToRequest(Long requestId, boolean approved, Long responderId, String responseReason){
        Request r = requestRepository.findById(requestId).orElseThrow(()-> new ValidationException(java.util.Map.of("id","Request not found")));

        User responder = userRepository.findById(responderId).orElseThrow(()-> new ValidationException(java.util.Map.of("responderId","Responder not found")));
        r.setResponder(responder);
        r.setResponseReason(responseReason);
        r.setUpdatedAt(LocalDateTime.now());
        r.setStatus(approved ? Request.Status.APPROVED : Request.Status.REJECTED);

        // If approved, assign intern to requested department
        if(approved){
            User intern = r.getIntern();
            if(intern != null){
                intern.setDepartment(r.getDepartment());
                userRepository.save(intern);
            }
        }

        return requestRepository.save(r);
    }
}
