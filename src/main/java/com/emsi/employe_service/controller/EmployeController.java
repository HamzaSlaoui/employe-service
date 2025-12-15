package com.emsi.employe_service.controller;

import com.emsi.employe_service.dto.EmployeRequest;
import com.emsi.employe_service.dto.EmployeResponse;
import com.emsi.employe_service.security.SecurityUtils;
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

    // ADMIN ONLY — Ajouter un employé
    @PostMapping
    public ResponseEntity<?> saveEmploye(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody EmployeRequest toSave) {

        if (!SecurityUtils.isAdmin(role)) {
            return ResponseEntity.status(403).body("Accès interdit : ADMIN uniquement");
        }

        EmployeResponse saved = employeService.saveEmploye(toSave);
        return ResponseEntity.status(201).body(saved);
    }

    // 🔹 ADMIN ONLY — Liste des employés
    @GetMapping
    public ResponseEntity<?> getAllEmployes(
            @RequestHeader("X-User-Role") String role) {
        System.out.println("ROLE = " + role);


        if (!SecurityUtils.isAdmin(role)) {
            return ResponseEntity.status(403).body("Accès interdit : ADMIN uniquement");
        }

        List<EmployeResponse> toReturn = employeService.getAllEmployes();
        return ResponseEntity.ok().body(toReturn);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getEmploye(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Employe-Id") Long employeId,
            @PathVariable Long id) {

        // ADMIN -> accès total
        if (SecurityUtils.isAdmin(role)) {
            return ResponseEntity.ok(employeService.getEmployeById(id));
        }

        // EMPLOYE -> accès uniquement à son propre profil
        if (SecurityUtils.isEmploye(role) && employeId.equals(id)) {
            return ResponseEntity.ok(employeService.getEmployeById(id));
        }

        return ResponseEntity.status(403)
                .body("Accès refusé : vous ne pouvez consulter que votre propre profil");
    }


    // 🔹 ADMIN ONLY — Supprimer
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteEmploye(
            @RequestHeader("X-User-Role") String role,
            @PathVariable(name = "id") Long id) {

        if (!SecurityUtils.isAdmin(role)) {
            return ResponseEntity.status(403).body("Accès interdit : ADMIN uniquement");
        }

        employeService.deleteEmployeById(id);
        return ResponseEntity.noContent().build();
    }

    // 🔹 ADMIN ONLY — Modifier
    @PutMapping(value = "/{id}")
    public ResponseEntity<?> updateEmploye(
            @RequestHeader("X-User-Role") String role,
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody EmployeRequest toUpdate) {

        if (!SecurityUtils.isAdmin(role)) {
            return ResponseEntity.status(403).body("Accès interdit : ADMIN uniquement");
        }

        EmployeResponse updated = employeService.updateEmploye(id, toUpdate);
        return ResponseEntity.ok().body(updated);
    }

    // 🔹 ADMIN ONLY — Décrémenter solde (appelé par conge-service)
    @PostMapping("/solde/{id}/{days}")
    public ResponseEntity<?> decrementSoldeConges(
            @RequestHeader("X-User-Role") String role,
            @PathVariable(name = "id") Long id,
            @PathVariable(name = "days") int days) {

        if (!SecurityUtils.isAdmin(role)) {
            return ResponseEntity.status(403).body("Accès interdit : ADMIN uniquement");
        }

        employeService.decrementSoldeConges(id, days);
        return ResponseEntity.ok().build();
    }

}
