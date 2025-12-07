package com.stanbic.internMs.intern.controller;

import com.stanbic.internMs.intern.dto.StandardAPIResponse;
import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.models.PerformanceReview;
import com.stanbic.internMs.intern.models.User;
import com.stanbic.internMs.intern.repository.UserRepository;
import com.stanbic.internMs.intern.service.PerformanceReviewService;
import com.stanbic.internMs.intern.utils.PagedResponse;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reviews")
public class PerformanceReviewController {

    private final PerformanceReviewService performanceReviewService;
    private final UserRepository userRepository;

    public PerformanceReviewController(PerformanceReviewService performanceReviewService, UserRepository userRepository){
        this.performanceReviewService=performanceReviewService;
        this.userRepository = userRepository;
    }

    @GetMapping("/list")
    public ResponseEntity<StandardAPIResponse> all(){
        try{
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(performanceReviewService.listPerformanceReviews())
                            .message("Performance Review records retrieved successfully")
                            .successful(true)
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .message("Something went wrong")
                            .successful(false)
                            .build()
            );
        }

//        Pagination + sorting endpoint

    }

    @GetMapping("/listPaginated")
    public ResponseEntity<StandardAPIResponse> getAll(
            @PageableDefault(size=5, sort = "id") Pageable pageable){
        try{
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(performanceReviewService.listPerformanceReviews(pageable))
                            .message("reviews fetched successfully")
                            .successful(true)
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .message("Something went wrong")
                            .successful(false)
                            .build()
            );
        }

    }

    @GetMapping("/listSorted")
    public ResponseEntity<StandardAPIResponse> listApplications(
            @PageableDefault(size=5, sort="id") Pageable pageable){
        try {
            Page<PerformanceReview> page= performanceReviewService.listPerformanceReviews(pageable);
            PagedResponse<PerformanceReview> performanceReviews = new PagedResponse<>(
                    page.getContent(),
                    page.getNumber()+1,
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast()
            );
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(performanceReviews)
                            .message("Reviews retrieved successfully")
                            .successful(true)
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .message("Something went wrong")
                            .successful(false)
                            .build()
            );
        }
    }
    @GetMapping("/view/{id}")
    public ResponseEntity<StandardAPIResponse> getReview(
            @PathVariable("id")@Min(value = 1, message = "Review ID must be positive") Long id) {
        try {
            Optional<PerformanceReview> performanceReview = performanceReviewService.findById(id);
            if (!performanceReview.isPresent()) {
                throw new ValidationException(Map.of("id", "Performance Review not found with id " + id));
            }
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(performanceReview.get())
                            .message("Performance Review record retrieved successfully")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve) {
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getMessage())
                            .message("The performance Review record was not found. Try again with another id")
                            .successful(false)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .message("Something went wrong")
                            .successful(false)
                            .build()
            );
        }
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('INTERN')")
    public ResponseEntity<StandardAPIResponse> myReviews(
            @PageableDefault(size = 5, sort = "id") Pageable pageable){
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth == null ? null : auth.getName();
            if(username == null){
                return ResponseEntity.status(403).body(StandardAPIResponse.builder().successful(false).message("Not authenticated").build());
            }
            User intern = userRepository.findByEmail(username).orElse(null);
            if(intern == null){
                return ResponseEntity.status(404).body(StandardAPIResponse.builder().successful(false).message("Authenticated intern not found").build());
            }

            Page<PerformanceReview> page = performanceReviewService.listReviewsForIntern(intern.getId(), pageable);
            PagedResponse<PerformanceReview> resp = new PagedResponse<>(
                    page.getContent(),
                    page.getNumber() + 1,
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast()
            );

            return ResponseEntity.ok(StandardAPIResponse.builder().data(resp).successful(true).message("My reviews retrieved").build());
        } catch (Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Failed to fetch my reviews").build());
        }
    }

    @GetMapping("/mine/all")
    @PreAuthorize("hasRole('INTERN')")
    public ResponseEntity<StandardAPIResponse> myReviewsAll(){
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth == null ? null : auth.getName();
            if(username == null){
                return ResponseEntity.status(403).body(StandardAPIResponse.builder().successful(false).message("Not authenticated").build());
            }
            User intern = userRepository.findByEmail(username).orElse(null);
            if(intern == null){
                return ResponseEntity.status(404).body(StandardAPIResponse.builder().successful(false).message("Authenticated intern not found").build());
            }

            return ResponseEntity.ok(StandardAPIResponse.builder().data(performanceReviewService.listReviewsForIntern(intern.getId())).successful(true).message("My reviews retrieved").build());
        } catch (Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Failed to fetch my reviews").build());
        }
    }

    // Managers: get reviews for a specific intern
    @GetMapping("/for-intern/{internId}")
    @PreAuthorize("hasRole('LINE_MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<StandardAPIResponse> reviewsForIntern(@PathVariable("internId") Long internId,
                                                                 @PageableDefault(size = 10, sort = "id") Pageable pageable){
        try{
            // Manager must be authenticated
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth == null ? null : auth.getName();
            if(username == null){
                return ResponseEntity.status(403).body(StandardAPIResponse.builder().successful(false).message("Not authenticated").build());
            }

            // Enforce ownership via service: manager identity is resolved inside the service
            boolean allowed = performanceReviewService.managerManagesInternByEmail(username, internId);
            if(!allowed){
                return ResponseEntity.status(403).body(StandardAPIResponse.builder().successful(false).message("You are not authorized to view reviews for this intern").build());
            }

            Page<PerformanceReview> page = performanceReviewService.listReviewsForIntern(internId, pageable);
            PagedResponse<PerformanceReview> resp = new PagedResponse<>(
                    page.getContent(),
                    page.getNumber() + 1,
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast()
            );
            return ResponseEntity.ok(StandardAPIResponse.builder().data(resp).successful(true).message("Reviews for intern retrieved").build());
        } catch (Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Failed to fetch reviews for intern").build());
        }
    }

    // Managers: get all reviews created by the authenticated manager
    @GetMapping("/for-my-interns")
    @PreAuthorize("hasRole('LINE_MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<StandardAPIResponse> reviewsForMyInterns(@PageableDefault(size = 10, sort = "id") Pageable pageable){
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth == null ? null : auth.getName();
            if(username == null){
                return ResponseEntity.status(403).body(StandardAPIResponse.builder().successful(false).message("Not authenticated").build());
            }
            User manager = userRepository.findByEmail(username).orElse(null);
            if(manager == null){
                return ResponseEntity.status(404).body(StandardAPIResponse.builder().successful(false).message("Authenticated manager not found").build());
            }

            Page<PerformanceReview> page = performanceReviewService.listReviewsForManager(manager.getId(), pageable);
            PagedResponse<PerformanceReview> resp = new PagedResponse<>(
                    page.getContent(),
                    page.getNumber() + 1,
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast()
            );
            return ResponseEntity.ok(StandardAPIResponse.builder().data(resp).successful(true).message("Reviews for my interns retrieved").build());
        } catch (Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Failed to fetch reviews for my interns").build());
        }
    }

    @GetMapping("/for-my-interns/all")
    @PreAuthorize("hasRole('LINE_MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<StandardAPIResponse> reviewsForMyInternsAll(){
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth == null ? null : auth.getName();
            if(username == null){
                return ResponseEntity.status(403).body(StandardAPIResponse.builder().successful(false).message("Not authenticated").build());
            }
            User manager = userRepository.findByEmail(username).orElse(null);
            if(manager == null){
                return ResponseEntity.status(404).body(StandardAPIResponse.builder().successful(false).message("Authenticated manager not found").build());
            }
            return ResponseEntity.ok(StandardAPIResponse.builder().data(performanceReviewService.listReviewsForManager(manager.getId())).successful(true).message("Reviews for my interns retrieved").build());
        } catch (Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Failed to fetch reviews for my interns").build());
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('LINE_MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<StandardAPIResponse> createReview(@RequestBody PerformanceReview review){
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth == null ? null : auth.getName();
            if(username == null){
                return ResponseEntity.status(403).body(StandardAPIResponse.builder().successful(false).message("Not authenticated").build());
            }

            // Delegate manager resolution and intern validation to the service
            PerformanceReview created = performanceReviewService.createPerformanceReview(review, username);
            return ResponseEntity.status(201).body(
                    StandardAPIResponse.builder().data(created).successful(true).message("Performance review created").build()
            );
        } catch(Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Failed to create review").build()
            );
        }
    }

    @PostMapping("/self")
    @PreAuthorize("hasRole('INTERN')")
    public ResponseEntity<StandardAPIResponse> createSelfReview(@RequestBody PerformanceReview review){
        try{
            // Interns may submit self-feedback; set intern from authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth == null ? null : auth.getName();
            User intern = null;
            if(username != null){
                intern = userRepository.findByEmail(username).orElse(null);
            }
            if(intern == null){
                return ResponseEntity.status(403).body(StandardAPIResponse.builder().successful(false).message("Authenticated intern not found").build());
            }
            review.setIntern(intern);
            // Do not allow setting lineManager from request for self reviews
            review.setLineManager(review.getLineManager());
            PerformanceReview created = performanceReviewService.createPerformanceReview(review);
            return ResponseEntity.status(201).body(
                    StandardAPIResponse.builder().data(created).successful(true).message("Self review submitted").build()
            );
        } catch(Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Failed to submit self review").build()
            );
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LINE_MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<StandardAPIResponse> updateReview(@PathVariable("id") Long id, @RequestBody PerformanceReview review){
        try{
            PerformanceReview updated = performanceReviewService.update(id, review);
            return ResponseEntity.ok(StandardAPIResponse.builder().data(updated).successful(true).message("Review updated").build());
        } catch(Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Failed to update review").build());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardAPIResponse> deleteReview(@PathVariable("id") Long id){
        try{
            performanceReviewService.delete(id);
            return ResponseEntity.ok(StandardAPIResponse.builder().successful(true).message("Review deleted").build());
        } catch(Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Failed to delete review").build());
        }
    }



}
