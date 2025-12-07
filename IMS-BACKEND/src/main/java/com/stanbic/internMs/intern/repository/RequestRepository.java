package com.stanbic.internMs.intern.repository;

import com.stanbic.internMs.intern.models.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
}
