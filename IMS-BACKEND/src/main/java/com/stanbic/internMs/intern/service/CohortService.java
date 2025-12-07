package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.models.Cohort;
import com.stanbic.internMs.intern.models.User;
import com.stanbic.internMs.intern.repository.CohortRepository;
import com.stanbic.internMs.intern.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CohortService {
    private final CohortRepository cohortRepository;
    private final UserRepository userRepository;

    public CohortService(CohortRepository cohortRepository, UserRepository userRepository){
        this.cohortRepository=cohortRepository;
        this.userRepository=userRepository;
    }

    public List<Cohort> listCohorts(){return cohortRepository.findAll();}

    public Page<Cohort> listCohorts(Pageable pageable){return cohortRepository.findAll(pageable);}

    public Page<Cohort> listCohorts(Pageable pageable, String search){
        if(search==null || search.isBlank()){
            return cohortRepository.findAll(pageable);
        }
        return cohortRepository.findByNameContainingIgnoreCase(search, pageable);
    }

    public Optional<Cohort> findById(Long id){return cohortRepository.findById(id);}

    // Update minimal fields like name and active flag
    public Cohort update(Long id, Cohort cohort){
        return cohortRepository.findById(id)
                .map(existing->{
                    existing.setName(cohort.getName());
                    existing.setIsActive(cohort.getIsActive());
                    return cohortRepository.save(existing);
                })
                .orElse(null);
    }

    public Cohort save(Cohort cohort){return cohortRepository.save(cohort);}

    public void delete(Long id){cohortRepository.deleteById(id);}

    public Cohort createCohort(Cohort cohort){
        return cohortRepository.save(cohort);
    }

    /**
     * Archive a cohort: mark cohort inactive and archive its interns.
     * If deleteInterns is true, remove intern users entirely.
     */
    public void archiveCohort(Long cohortId, boolean deleteInterns){
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new RuntimeException("Cohort not found with id: " + cohortId));

        List<User> interns = cohort.getInterns();
        if(interns != null && !interns.isEmpty()){
            if(deleteInterns){
                // delete intern users
                userRepository.deleteAll(interns);
            } else {
                // mark interns archived and detach cohort
                for(User u : interns){
                    u.setArchived(true);
                    u.setCohort(null);
                }
                userRepository.saveAll(interns);
            }
        }

        cohort.setIsActive(false);
        cohortRepository.save(cohort);
    }

}

