package com.emsi.employe_service.service;

import com.emsi.employe_service.dto.EmployeRequest;
import com.emsi.employe_service.dto.EmployeResponse;
import com.emsi.employe_service.entity.Employe;
import com.emsi.employe_service.exception.EmployeNotFoundException;
import com.emsi.employe_service.mapper.EmployeMapper;
import com.emsi.employe_service.repository.EmployeRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class EmployeService {
    private final EmployeRepository employeRepository;

    @Transactional
    public EmployeResponse saveEmploye(EmployeRequest employe){
        Employe toSave = EmployeMapper.toEntity(employe);
        Employe saved = employeRepository.save(toSave);
        return EmployeMapper.toResponse(saved);
    }


    public EmployeResponse getEmployeById(Long id){
        log.info("Récupération de l'employé avec l'id: {}", id);
        Employe toReturn = employeRepository.findById(id)
                .orElseThrow(() -> new EmployeNotFoundException("Aucun employé trouvé avec l'id : " + id));
        return EmployeMapper.toResponse(toReturn);
    }

    public List<EmployeResponse> getAllEmployes(){
        log.info("Récupération de tous les employés");
        List<Employe> all = employeRepository.findAll();
        return all.stream()
                .map(EmployeMapper::toResponse)
                .toList();
    }



    @Transactional
    public void deleteEmployeById(Long id) {
        Employe toDelete = employeRepository.findById(id)
                .orElseThrow(() -> new EmployeNotFoundException("L'employé demandé n'existe pas dans la base"));
        employeRepository.delete(toDelete);
    }

    @Transactional
    public EmployeResponse updateEmploye(Long id, EmployeRequest toUpdate) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new EmployeNotFoundException("Aucun employé trouvé avec l'id : " + id));

        if (employeRepository.existsByEmailAndIdNot(toUpdate.getEmail(), id)) {
            throw new IllegalArgumentException("Un autre employé utilise déjà cet email");
        }

        if (employeRepository.existsByTelephoneAndIdNot(toUpdate.getTelephone(), id)) {
            throw new IllegalArgumentException("Un autre employé utilise déjà ce numéro de téléphone");
        }

        employe.setNom(toUpdate.getNom());
        employe.setPrenom(toUpdate.getPrenom());
        employe.setEmail(toUpdate.getEmail());
        employe.setTelephone(toUpdate.getTelephone());
        employe.setPoste(toUpdate.getPoste());
        employe.setAdresse(toUpdate.getAdresse());

        Employe updated = employeRepository.save(employe);
        return EmployeMapper.toResponse(updated);
    }

    // DECREMENT SOLDE CONGES - Avec fallback critique
    @CircuitBreaker(name = "employeServiceCircuitBreaker", fallbackMethod = "decrementSoldeCongesFallback")
    @Retry(name = "employeServiceRetry")
    @Transactional
    public void decrementSoldeConges(Long id, int days) {
        log.info("Décrémentation du solde de congés pour l'employé {} de {} jours", id, days);

        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new EmployeNotFoundException("Aucun employé trouvé avec l'id : " + id));

        employe.setSoldeConge(employe.getSoldeConge() - days);
        employeRepository.save(employe);

        log.info("Solde décrémenté avec succès pour l'employé {}", id);
    }

    private void decrementSoldeCongesFallback(Long id, int days, Exception ex) {
        log.error("ALERTE CRITIQUE: Échec de la décrémentation du solde de congés. " +
                "Employé ID: {}, Jours: {}, Erreur: {}", id, days, ex.getMessage());

        // Logger pour traitement manuel ultérieur
        throw new RuntimeException(
                "Service temporairement indisponible. Votre demande de congé sera traitée ultérieurement. " +
                        "Référence: EMP-" + id + "-" + System.currentTimeMillis()
        );
    }
}