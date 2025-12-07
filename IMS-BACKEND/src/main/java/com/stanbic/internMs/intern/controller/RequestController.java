package com.stanbic.internMs.intern.controller;

import com.stanbic.internMs.intern.dto.GenericDTO;
import com.stanbic.internMs.intern.dto.StandardAPIResponse;
import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.models.Request;
import com.stanbic.internMs.intern.service.RequestService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService){
        this.requestService=requestService;
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('INTERN')")
    public ResponseEntity<StandardAPIResponse> createRequest(@RequestBody GenericDTO dto){
        try{
            Request created = requestService.createRequest(dto);
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(created)
                            .message("Request created successfully")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve){
            return ResponseEntity.status(400).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors())
                            .successful(false)
                            .message("Validation failed")
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

    @GetMapping
    public ResponseEntity<StandardAPIResponse> listRequests(
            @RequestParam(required = false) Long internId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Request.Status status){
        try{
            List<Request> list = requestService.listRequests(Optional.ofNullable(internId), Optional.ofNullable(departmentId), Optional.ofNullable(status));
            return ResponseEntity.ok(StandardAPIResponse.builder().data(list).successful(true).message("Requests fetched").build());
        } catch(Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Something went wrong").build());
        }
    }

    @PutMapping("/{id}/respond")
    @PreAuthorize("hasRole('LINE_MANAGER')")
    public ResponseEntity<StandardAPIResponse> respondToRequest(@PathVariable("id") @Min(1) Long id, @RequestBody GenericDTO dto){
        try{
            Long responderId = dto.get("responderId") == null ? null : Long.parseLong(dto.get("responderId").toString());
            Boolean approved = dto.get("approved") == null ? Boolean.FALSE : Boolean.parseBoolean(dto.get("approved").toString());
            String responseReason = dto.getString("responseReason");

            if(responderId==null){
                throw new ValidationException(Map.of("responderId","Responder id is required"));
            }

            Request updated = requestService.respondToRequest(id, approved, responderId, responseReason);
            return ResponseEntity.ok(StandardAPIResponse.builder().data(updated).successful(true).message("Request updated").build());
        } catch (ValidationException ve){
            return ResponseEntity.status(400).body(StandardAPIResponse.builder().errors(ve.getErrors()).successful(false).message("Validation failed").build());
        } catch (Exception e){
            return ResponseEntity.status(500).body(StandardAPIResponse.builder().errors(e.getMessage()).successful(false).message("Something went wrong").build());
        }
    }

}
