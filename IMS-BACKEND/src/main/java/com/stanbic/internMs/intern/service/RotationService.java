package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.models.PerformanceReview;
import com.stanbic.internMs.intern.models.Rotation;
import com.stanbic.internMs.intern.models.User;
import com.stanbic.internMs.intern.models.Department;
import com.stanbic.internMs.intern.repository.RotationRepository;
import com.stanbic.internMs.intern.repository.UserRepository;
import com.stanbic.internMs.intern.repository.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RotationService {
    private final RotationRepository rotationRepository;

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public RotationService(RotationRepository rotationRepository, UserRepository userRepository, DepartmentRepository departmentRepository){
        this.rotationRepository = rotationRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<Rotation> listRotations(){return rotationRepository.findAll();}

    public Page<Rotation> listRotations(Pageable pageable){return rotationRepository.findAll(pageable);}

    public Page<Rotation> listRotations(Pageable pageable, String search){
        if(search==null || search.isBlank()){
            return rotationRepository.findAll(pageable);
        }
        return rotationRepository.findByNameContainingIgnoreCase(search, pageable);
    }

    public Optional<Rotation>findById(Long id){return rotationRepository.findById(id);}



    //name, startDate,endDate,
    public Rotation update(Long id, Rotation rotation){
        return rotationRepository.findById(id)
                .map(existing->{
                    existing.setName(rotation.getName());
                    existing.setStartDate(rotation.getStartDate());
                    existing.setEndDate(rotation.getEndDate());
                    return rotationRepository.save(existing);
                })
                .orElse(null);
    }

    public void delete(Long id){rotationRepository.deleteById(id);}

    public Rotation createRotation(Rotation rotation){
//        rotation.setStartDate();
        return rotationRepository.save(rotation);
    }

    public Rotation proposeRotation(Long internId, Long departmentId, Long supervisorId, Rotation rotationData){
        User intern = userRepository.findById(internId).orElseThrow(()-> new RuntimeException("Intern not found"));
        Department dept = departmentRepository.findById(departmentId).orElseThrow(()-> new RuntimeException("Department not found"));
        User supervisor = userRepository.findById(supervisorId).orElseThrow(()-> new RuntimeException("Supervisor not found"));

        Rotation rotation = new Rotation();
        rotation.setName(rotationData.getName());
        rotation.setStartDate(rotationData.getStartDate());
        rotation.setEndDate(rotationData.getEndDate());
        rotation.setDepartment(dept);
        rotation.setAssignedSupervisor(supervisor);
        rotation.setStatus(Rotation.Status.PROPOSED);
        rotation.setInterns(java.util.List.of(intern));

        return rotationRepository.save(rotation);
    }

    public Rotation approveRotation(Long rotationId, Long approverId, boolean approved){
        Rotation rotation = rotationRepository.findById(rotationId).orElseThrow(()-> new RuntimeException("Rotation not found"));
        User approver = userRepository.findById(approverId).orElseThrow(()-> new RuntimeException("Approver not found"));
        rotation.setApprover(approver);
        rotation.setStatus(approved? Rotation.Status.APPROVED: Rotation.Status.REJECTED);
        rotation.setUpdatedAt(java.time.Instant.now());
        return rotationRepository.save(rotation);
    }

    public Rotation save(Rotation rotation){return rotationRepository.save(rotation);}

}


