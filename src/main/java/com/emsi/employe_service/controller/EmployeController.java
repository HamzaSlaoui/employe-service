package com.emsi.employe_service.controller;

import com.emsi.employe_service.dto.EmployeRequest;
import com.emsi.employe_service.dto.EmployeResponse;
import com.emsi.employe_service.exception.EmployeNotFoundException;
import com.emsi.employe_service.service.EmployeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/employes")
@AllArgsConstructor
public class EmployeController {
    private final EmployeService employeService;

    @PostMapping
    public ResponseEntity<EmployeResponse> saveEmploye(@Valid @RequestBody EmployeRequest toSave){
        EmployeResponse saved = employeService.saveEmploye(toSave);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<EmployeResponse>> getAllEmployes(){
        List<EmployeResponse> toReturn = employeService.getAllEmployes();
        return ResponseEntity.ok().body(toReturn);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<EmployeResponse> getEmploye(@PathVariable(name = "id") Long id) {
        EmployeResponse toReturn = employeService.getEmployeById(id);
        return ResponseEntity.status(200).body(toReturn);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteEmploye(@PathVariable(name = "id") Long id) {
        employeService.deleteEmployeById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<EmployeResponse> updateEmploye(@PathVariable(name = "id") Long id, @Valid @RequestBody EmployeRequest toUpdate) {
        EmployeResponse updated = employeService.updateEmploye(id,  toUpdate);
        return ResponseEntity.ok().body(updated);
    }

    @PostMapping("/solde/{id}/{days}")
    public void decrementSoldeConges(@PathVariable(name = "id") Long id, @PathVariable(name = "days") int days) {
        employeService.decrementSoldeConges(id, days);
    }

}
