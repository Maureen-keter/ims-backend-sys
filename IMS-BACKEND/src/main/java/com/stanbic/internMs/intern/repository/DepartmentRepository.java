package com.stanbic.internMs.intern.repository;

import com.stanbic.internMs.intern.models.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Page<Department> findByNameContainingIgnoreCase(String search, Pageable pageable);
}
