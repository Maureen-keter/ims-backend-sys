package com.stanbic.internMs.intern.controller;

import com.stanbic.internMs.intern.dto.GenericDTO;
import com.stanbic.internMs.intern.dto.StandardAPIResponse;
import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.models.Rotation;
import com.stanbic.internMs.intern.service.RotationService;
import com.stanbic.internMs.intern.utils.PagedResponse;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/rotations")
public class RotationController {
    private final RotationService rotationService;

    public RotationController(RotationService rotationService){
        this.rotationService=rotationService;
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<StandardAPIResponse> createRotation(@RequestBody GenericDTO dto){
        try{
            Long internId = dto.get("internId") == null ? null : Long.parseLong(dto.get("internId").toString());
            Long departmentId = dto.get("departmentId") == null ? null : Long.parseLong(dto.get("departmentId").toString());
            Long supervisorId = dto.get("supervisorId") == null ? null : Long.parseLong(dto.get("supervisorId").toString());

            Rotation rdata = new Rotation();
            rdata.setName(dto.getString("name"));
            try{ rdata.setStartDate(java.time.LocalDate.parse(dto.getString("startDate"))); } catch(Exception ignored){}
            try{ rdata.setEndDate(java.time.LocalDate.parse(dto.getString("endDate"))); } catch(Exception ignored){}

            Rotation created = rotationService.proposeRotation(internId, departmentId, supervisorId, rdata);
            return ResponseEntity.ok(StandardAPIResponse.builder().data(created).successful(true).message("Rotation proposed").build());
        } catch(Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Something went wrong").build());
        }
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<StandardAPIResponse> approveRotation(@PathVariable("id") Long id, @RequestBody GenericDTO dto){
        try{
            Long approverId = dto.get("approverId") == null ? null : Long.parseLong(dto.get("approverId").toString());
            boolean approved = dto.get("approved") == null ? false : Boolean.parseBoolean(dto.get("approved").toString());

            Rotation updated = rotationService.approveRotation(id, approverId, approved);
            return ResponseEntity.ok(StandardAPIResponse.builder().data(updated).successful(true).message("Rotation updated").build());
        } catch(Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Something went wrong").build());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<StandardAPIResponse> all(){
        try{
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(rotationService.listRotations())
                            .message("Rotation records retrieved successfully")
                            .successful(true)
                            .build()
            );
        }catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }

    }
    @GetMapping("/listPaginated")
    public ResponseEntity<StandardAPIResponse> getAll(
            @PageableDefault(size = 5, sort = "id")Pageable pageable){
        try{
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(rotationService.listRotations(pageable))
                            .message("Rotation records retrieved successfully")
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
    public ResponseEntity<StandardAPIResponse> listRotations(
            @PageableDefault(size = 5, sort = "id") Pageable pageable){
        try{
            Page<Rotation> page=rotationService.listRotations(pageable);
            PagedResponse<Rotation> rotations= new PagedResponse<>(
                    page.getContent(),
                    page.getNumber()+1,
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast()
            );
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(rotations)
                            .message("Rotation records retrieved successfully")
                            .successful(true)
                            .build()
            );
        }catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }
    @GetMapping("/view/{id}")
    public ResponseEntity<StandardAPIResponse> getRotation(
            @PathVariable("id") @Min(value = 1, message = "Rotation id must be positive") Long id){
        try{
            Optional<Rotation> rotation=rotationService.findById(id);
            if(!rotation.isPresent()){
                throw new ValidationException(Map.of("id", "Rotation not found with id " + id));
            }
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(rotation.get())
                            .message("Rotation record retrieved successfully")
                            .successful(true)
                            .build()
            );
        }  catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .message("Something went wrong")
                            .successful(false)
                            .build()
            );
        }
        }
//    @PostMapping("add")
//    public ResponseEntity<StandardAPIResponse> createRotation(
//
}
