package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.models.PerformanceReview;
import com.stanbic.internMs.intern.repository.PerformanceReviewRepository;
import com.stanbic.internMs.intern.repository.RotationRepository;
import com.stanbic.internMs.intern.repository.UserRepository;
import com.stanbic.internMs.intern.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PerformanceReviewService {
    private final PerformanceReviewRepository performanceReviewRepository;
    private final RotationRepository rotationRepository;
    private final UserRepository userRepository;

    public PerformanceReviewService(PerformanceReviewRepository performanceReviewRepository, RotationRepository rotationRepository, UserRepository userRepository){
        this.performanceReviewRepository=performanceReviewRepository;
        this.rotationRepository = rotationRepository;
        this.userRepository = userRepository;
    }

    public List<PerformanceReview> listPerformanceReviews(){return performanceReviewRepository.findAll();}

    public Page<PerformanceReview> listPerformanceReviews(Pageable pageable){return performanceReviewRepository.findAll(pageable);}

//    public Page<PerformanceReview> listPerformanceReviews(Pageable pageable, String search){
//        if(search==null || search.isBlank()){
//            return performanceReviewRepository.findAll(pageable);
//        }
//        return performanceReviewRepository.findByNameContainingIgnoreCase(search, pageable);
//    }

    public Optional<PerformanceReview> findById(Long id){return performanceReviewRepository.findById(id);}

    public PerformanceReview update(Long id, PerformanceReview performanceReview){
        //                    existing.setStartDate();
        return performanceReviewRepository.findById(id)
                .map(performanceReviewRepository::save)
                .orElseThrow(()-> new RuntimeException("Performance Review not found with id: " +id));
    }

    public PerformanceReview createPerformanceReview(PerformanceReview performanceReview){
//        performanceReview.setCreatedAt();
        return performanceReviewRepository.save(performanceReview);

    }

    /**
     * Create a performance review on behalf of a manager identified by email.
     * This resolves the manager user, validates/loads the intern entity if provided,
     * sets the lineManager on the review and persists it.
     */
    public PerformanceReview createPerformanceReview(PerformanceReview performanceReview, String managerEmail){
        if(managerEmail == null) throw new RuntimeException("Manager email is required");
        User manager = userRepository.findByEmail(managerEmail).orElseThrow(() -> new RuntimeException("Manager not found"));

        // If an intern was passed with an id, resolve the managed entity
        if(performanceReview.getIntern() != null && performanceReview.getIntern().getId() != null){
            Long internId = performanceReview.getIntern().getId();
            // rely on repository to throw if not found
            User intern = userRepository.findById(internId).orElseThrow(() -> new RuntimeException("Intern not found"));
            performanceReview.setIntern(intern);
        }

        performanceReview.setLineManager(manager);
        return performanceReviewRepository.save(performanceReview);
    }

    public PerformanceReview save(PerformanceReview performanceReview){
        return performanceReviewRepository.save(performanceReview);
    }

    public void delete(Long id){performanceReviewRepository.deleteById(id);}

    // List reviews for a specific intern
    public List<PerformanceReview> listReviewsForIntern(Long internId){
        return performanceReviewRepository.findByInternId(internId);
    }

    public Page<PerformanceReview> listReviewsForIntern(Long internId, Pageable pageable){
        return performanceReviewRepository.findByInternId(internId, pageable);
    }

    // List reviews created by a specific line manager
    public List<PerformanceReview> listReviewsForManager(Long managerId){
        return performanceReviewRepository.findByLineManagerId(managerId);
    }

    public Page<PerformanceReview> listReviewsForManager(Long managerId, Pageable pageable){
        return performanceReviewRepository.findByLineManagerId(managerId, pageable);
    }

    /**
     * Check whether a manager manages the given intern.
     * Returns true if the manager is recorded as the lineManager on any review for the intern
     * OR if the manager is the assignedSupervisor on any rotation that includes the intern.
     */
    public boolean managerManagesIntern(Long managerId, Long internId){
        // Fast repository-level checks:
        try{
            // 1) Manager authored a review for the intern?
            boolean authored = performanceReviewRepository.existsByLineManagerIdAndIntern_Id(managerId, internId);
            if(authored) return true;

            // 2) Manager is assigned supervisor on a rotation that includes the intern
            return rotationRepository.existsByAssignedSupervisorIdAndInterns_Id(managerId, internId);
        } catch(Exception e){
            return false;
        }
    }

    /**
     * Resolve manager by email and check whether they manage the intern.
     * Returns false if manager not found or not managing the intern.
     */
    public boolean managerManagesInternByEmail(String managerEmail, Long internId){
        if(managerEmail == null) return false;
        User manager = userRepository.findByEmail(managerEmail).orElse(null);
        if(manager == null) return false;
        return managerManagesIntern(manager.getId(), internId);
    }
}
