package com.emsi.employe_service.service;

import com.emsi.employe_service.dto.EmployeRequest;
import com.emsi.employe_service.dto.EmployeResponse;
import com.emsi.employe_service.entity.Employe;
import com.emsi.employe_service.enums.Poste;
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

    // =================================================================
    // OPÉRATIONS D'ÉCRITURE - Fallback avec exception
    // =================================================================

    @CircuitBreaker(name = "employeServiceCB", fallbackMethod = "saveEmployeFallback")
    @Retry(name = "employeServiceRetry")
    @Transactional
    public EmployeResponse saveEmploye(EmployeRequest employe){
        log.info("Sauvegarde d'un nouvel employé: {}", employe.getEmail());
        Employe toSave = EmployeMapper.toEntity(employe);
        Employe saved = employeRepository.save(toSave);
        return EmployeMapper.toResponse(saved);
    }

    private EmployeResponse saveEmployeFallback(EmployeRequest employe, Exception ex) {
        log.error("🔴 FALLBACK - Échec sauvegarde employé: {}", ex.getMessage());
        throw new RuntimeException("Service temporairement indisponible. Référence: EMP-CREATE-" + System.currentTimeMillis());
    }

    @CircuitBreaker(name = "employeServiceCB", fallbackMethod = "updateEmployeFallback")
    @Retry(name = "employeServiceRetry")
    @Transactional
    public EmployeResponse updateEmploye(Long id, EmployeRequest toUpdate) {
        log.info("Mise à jour de l'employé ID: {}", id);

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

    private EmployeResponse updateEmployeFallback(Long id, EmployeRequest toUpdate, Exception ex) {
        log.error("🔴 FALLBACK - Échec mise à jour employé ID {}: {}", id, ex.getMessage());
        throw new RuntimeException("Service temporairement indisponible. Référence: EMP-UPDATE-" + id + "-" + System.currentTimeMillis());
    }

    @CircuitBreaker(name = "employeServiceCB", fallbackMethod = "deleteEmployeFallback")
    @Retry(name = "employeServiceRetry")
    @Transactional
    public void deleteEmployeById(Long id) {
        log.info("Suppression de l'employé ID: {}", id);
        Employe toDelete = employeRepository.findById(id)
                .orElseThrow(() -> new EmployeNotFoundException("L'employé demandé n'existe pas dans la base"));
        employeRepository.delete(toDelete);
    }

    private void deleteEmployeFallback(Long id, Exception ex) {
        log.error("🔴 FALLBACK - Échec suppression employé ID {}: {}", id, ex.getMessage());
        throw new RuntimeException("Service temporairement indisponible. Référence: EMP-DELETE-" + id + "-" + System.currentTimeMillis());
    }

    // =================================================================
    // OPÉRATIONS DE LECTURE - Fallback avec données par défaut
    // =================================================================

    @CircuitBreaker(name = "employeServiceCB", fallbackMethod = "getEmployeByIdFallback")
    @Retry(name = "employeServiceRetry")
    public EmployeResponse getEmployeById(Long id){
        log.info("Récupération de l'employé ID: {}", id);
        Employe toReturn = employeRepository.findById(id)
                .orElseThrow(() -> new EmployeNotFoundException("Aucun employé trouvé avec l'id : " + id));
        return EmployeMapper.toResponse(toReturn);
    }

    private EmployeResponse getEmployeByIdFallback(Long id, Exception ex) {
        log.warn("⚠️ FALLBACK - Retour données par défaut pour employé ID {}: {}", id, ex.getMessage());

        // Retourne des données par défaut
        EmployeResponse fallbackResponse = new EmployeResponse();
        fallbackResponse.setId(id);
        fallbackResponse.setNom("Service");
        fallbackResponse.setPrenom("Indisponible");
        fallbackResponse.setEmail("unavailable@system.local");
        fallbackResponse.setTelephone("0000000000");
        fallbackResponse.setAdresse("Données temporairement indisponibles");
        fallbackResponse.setSoldeConge(0);

        return fallbackResponse;
    }

    @CircuitBreaker(name = "employeServiceCB", fallbackMethod = "getAllEmployesFallback")
    @Retry(name = "employeServiceRetry")
    public List<EmployeResponse> getAllEmployes(){
        log.info("Récupération de tous les employés");
        List<Employe> all = employeRepository.findAll();
        return all.stream()
                .map(EmployeMapper::toResponse)
                .toList();
    }

    private List<EmployeResponse> getAllEmployesFallback(Exception ex) {
        log.warn("⚠️ FALLBACK - Retour liste vide: {}", ex.getMessage());
        return Collections.emptyList();
    }

    // =================================================================
    // OPÉRATION CRITIQUE - Décrémentation solde congés
    // =================================================================

    @CircuitBreaker(name = "employeServiceCB", fallbackMethod = "decrementSoldeCongesFallback")
    @Retry(name = "employeServiceRetry")
    @Transactional
    public void decrementSoldeConges(Long id, int days) {
        log.info("Décrémentation solde congés - Employé: {}, Jours: {}", id, days);

        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new EmployeNotFoundException("Aucun employé trouvé avec l'id : " + id));

        int nouveauSolde = employe.getSoldeConge() - days;
        if (nouveauSolde < 0) {
            throw new IllegalArgumentException("Solde de congés insuffisant");
        }

        employe.setSoldeConge(nouveauSolde);
        employeRepository.save(employe);

        log.info("Solde décrémenté avec succès. Nouveau solde: {}", nouveauSolde);
    }

    private void decrementSoldeCongesFallback(Long id, int days, Exception ex) {
        log.error("🔴 ALERTE CRITIQUE - Échec décrémentation solde congés");
        log.error("Employé: {}, Jours: {}, Erreur: {}", id, days, ex.getMessage());

        throw new RuntimeException(
                "ERREUR CRITIQUE: La décrémentation du solde de congés a échoué. " +
                        "Votre demande sera traitée manuellement. " +
                        "Référence: EMP-CONGE-" + id + "-" + System.currentTimeMillis()
        );
    }
}