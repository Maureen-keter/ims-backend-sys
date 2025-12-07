package com.stanbic.internMs.intern.controller;

import com.stanbic.internMs.intern.dto.StandardAPIResponse;
import com.stanbic.internMs.intern.models.Department;
import com.stanbic.internMs.intern.service.DepartmentService;
import com.stanbic.internMs.intern.utils.PagedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/v1/department")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService){
        this.departmentService=departmentService;
    }

    @PostMapping("/add")
    public ResponseEntity<StandardAPIResponse> createDepartment(@RequestBody Department department){
        try{
            Department created=departmentService.createDepartment(department);
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(created)
                            .message("Department created successfully")
                            .successful(true)
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

    @PatchMapping("/update/{id}")
    public ResponseEntity<StandardAPIResponse> updateDepartment(@PathVariable Long id, @RequestBody Department department){
        try{
            Department updated = departmentService.update(id, department);
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(updated)
                            .message("Department updated successfully")
                            .successful(true)
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<StandardAPIResponse> deleteDepartment(@PathVariable Long id){
        try{
            departmentService.delete(id);
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .message("Department deleted successfully")
                            .successful(true)
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

    @GetMapping("list")
    public ResponseEntity<StandardAPIResponse> all(){
        try{
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(departmentService.listDepartments())
                            .message("Department records fetched successfully")
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

//    Pagination + sorting endpoint
    @GetMapping("listPaginated")
    public ResponseEntity<StandardAPIResponse> getAll(
            @PageableDefault(size=5, sort="id")Pageable pageable){
        try{
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(departmentService.listDepartments())
                            .message("Department records fetched successfully")
                            .successful(true)
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

    @GetMapping("listAllSorted")
    public ResponseEntity<StandardAPIResponse> listAllDepartments(
            @RequestParam(defaultValue = "1") @PathVariable int pageNo,
            @RequestParam(defaultValue = "5") @PathVariable int size,
            @RequestParam(required = false) @PathVariable String search,
            @RequestParam(defaultValue = "id") @PathVariable String sort){
        try{
            log.info("pageNo::"+ pageNo);
            if(pageNo<1){
                pageNo=1;
            }
//          Covert to 0-based
            Pageable pageable= PageRequest.of(pageNo-1, size, Sort.by(sort.split(",")));
            Page<Department> page=departmentService.listDepartments(pageable, search);
            PagedResponse<Department> departments= new PagedResponse<>(
                page.getContent(),
                pageNo,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
            );
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(departments)
                            .message("Successfully fetched departments")
                            .successful(true)
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

//    @GetMapping("search")
//    public ResponseEntity<StandardAPIResponse> searchDepartments(
//            @PageableDefault(size=5, sort="id") Pageable pageable,
//            @RequestParam Map<String, String> filters){
//        try{
//            filters.remove("page");
//            filters.remove("size");
//            filters.remove("sort");
//
//            Page<Department> page=departmentService.searchDepartments(filters, pageable);
//
//            PagedResponse<Department> departments=new PagedResponse<>(
//                    page.getContent(),
//                    page.getNumber()+1,
//                    page.getSize(),
//                    page.getTotalElements(),
//                    page.getTotalPages(),
//                    page.isLast()
//            );
//            return ResponseEntity.status(200).body(
//                    StandardAPIResponse.builder()
//                            .data(departments)
//                            .message("Department records fetched successfully")
//                            .successful(true)
//                            .build()
//            );
//
//            } catch (Exception e){
//            return ResponseEntity.status(500).body(
//                    StandardAPIResponse.builder()
//                            .errors(e.getMessage())
//                            .successful(false)
//                            .message("Something went wrong")
//                            .build()
//            );
//        }
//    }
}
