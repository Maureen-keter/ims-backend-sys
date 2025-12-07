package com.stanbic.internMs.intern.repository;

import com.stanbic.internMs.intern.models.Cohort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public @Repository
interface CohortRepository extends JpaRepository<Cohort, Long> {
    Page<Cohort> findByNameContainingIgnoreCase(String search, Pageable pageable);
}
