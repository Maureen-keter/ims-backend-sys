package com.stanbic.internMs.intern.controller;

import com.stanbic.internMs.intern.dto.DtoMapper;
import com.stanbic.internMs.intern.dto.GenericDTO;
import com.stanbic.internMs.intern.dto.StandardAPIResponse;
import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.models.Cohort;
import com.stanbic.internMs.intern.service.CohortService;
import com.stanbic.internMs.intern.utils.ValidationUtil;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cohort")
public class CohortController {
    private final CohortService cohortService;

    public CohortController(CohortService cohortService){
        this.cohortService=cohortService;
    }

    @GetMapping("/list")
    public ResponseEntity<StandardAPIResponse> all(){
        try{
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(cohortService.listCohorts())
                            .message("Cohort records retrieved successfully")
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
    public ResponseEntity<StandardAPIResponse>getCohort(@PathVariable(value = "id", required = true) @Min(value = 1, message = "Cohort id must be positive") Long id){
        try{
            Optional<Cohort> cohort=cohortService.findById(id);
            if(!cohort.isPresent()){
                throw new ValidationException(Map.of("id", "Company not found with id " + id));
            }
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(cohort.get())
                            .message("Successfully fetched cohort")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve){
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors().get("id"))
                            .message("The cohort was not found. Try again with a different ID")
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }

    @PostMapping("/add")
    public ResponseEntity<StandardAPIResponse> createCohort(@RequestBody GenericDTO dto){
        try{
            ValidationUtil.validateRequiredFields(dto, Cohort.class);
            Cohort cohort= DtoMapper.mapToEntity(dto, Cohort.class);
            Cohort createCohort=cohortService.createCohort(cohort);

            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(createCohort)
                            .message("Successfully created Cohort")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve){
            return ResponseEntity.status(400).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors())
                            .successful(false)
                            .message("The input is incorrect.Try again")
                            .build()
            );

        } catch(Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<StandardAPIResponse> update(@PathVariable Long id, @RequestBody GenericDTO dto){
        try{
            Optional<Cohort> cohort=cohortService.findById(id);
            if(!cohort.isPresent()){
                throw new ValidationException(Map.of("id", "Cohort not found with id "+id));
            }
            ValidationUtil.validateRequiredFields(dto, Cohort.class);
            DtoMapper.mapNonNullFields(dto, cohort.get()); //only overwrite present fields

            Cohort updatedCohort=cohortService.save(cohort.get());

            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(updatedCohort)
                            .message("Cohort record updated successfully")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve){
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors().get("id"))
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .build()
            );
        }
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<StandardAPIResponse> archiveCohort(
            @PathVariable("id") Long id,
            @RequestParam(defaultValue = "false") boolean delete){
        try{
            Optional<Cohort> cohortOptional = cohortService.findById(id);
            if(!cohortOptional.isPresent()){
                throw new ValidationException(Map.of("id", "Cohort not found with id " + id));
            }

            cohortService.archiveCohort(id, delete);

            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .message("Cohort archived successfully")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve){
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors())
                            .successful(false)
                            .message("Cohort not found")
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }
}
