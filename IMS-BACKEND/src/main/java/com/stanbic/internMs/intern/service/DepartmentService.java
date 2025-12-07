package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.models.Department;
import com.stanbic.internMs.intern.repository.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository){
        this.departmentRepository=departmentRepository;
    }

    public List<Department> listDepartments(){return departmentRepository.findAll();}

    public Page<Department> listDepartments(Pageable pageable){ return departmentRepository.findAll(pageable);}

    public Page<Department> listDepartments(Pageable pageable,String search){
        if(search==null || search.isBlank()){
            return departmentRepository.findAll(pageable);
        }
        return departmentRepository.findByNameContainingIgnoreCase(search, pageable);
    }

    public Department update(Long id, Department department){
        return departmentRepository.findById(id)
                .map(existing->{
                    existing.setName(department.getName());
//                    existing.setUpdatedAt(department.getUpdatedAt());
                    return departmentRepository.save(existing);
                })
                .orElseThrow(()-> new RuntimeException("Department not found with id: " +id));
    }

    public void delete(Long id){departmentRepository.deleteById(id);}

    public Department createDepartment(Department department){
//        department.setCreatedAt(new )
//        department.setUpdatedAt();
        return departmentRepository.save(department);
    }
    public Department save(Department department){return departmentRepository.save(department);}

    public Optional<Department> findById(Long id){return departmentRepository.findById(id);}

//    public Page<Department> searchDepartments(Map<String, String>filters, Pageable pageable){
//        AdvancedSpecificationBuilder<Department> builder= new AdvancedSpecificationBuilder<>();
//        Specification<Department> spec=builder.buildFromParams(filters);
//        return departmentRepository.findAll(spec, pageable);
//    }
}
