package com.emsi.employe_service.service;

import com.emsi.employe_service.dto.EmployeRequest;
import com.emsi.employe_service.dto.EmployeResponse;
import com.emsi.employe_service.entity.Employe;
import com.emsi.employe_service.exception.EmployeNotFoundException;
import com.emsi.employe_service.mapper.EmployeMapper;
import com.emsi.employe_service.repository.EmployeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class EmployeService {
    private final EmployeRepository employeRepository;

    @Transactional
    public EmployeResponse saveEmploye(EmployeRequest employe){
        Employe toSave = EmployeMapper.toEntity(employe);
        Employe saved = employeRepository.save(toSave);
        return EmployeMapper.toResponse(saved);
    }

    public EmployeResponse getEmployeById(Long id){
        Employe toReturn = employeRepository.findById(id).orElseThrow(() -> new EmployeNotFoundException("Aucun employé trouvé avec l'id : " + id));
        return EmployeMapper.toResponse(toReturn);
    }

    public List<EmployeResponse> getAllEmployes(){
        List<Employe> all = employeRepository.findAll();
        return all.stream()
                .map(EmployeMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteEmployeById(Long id) {
        Employe toDelete = employeRepository.findById(id).orElseThrow(() -> new EmployeNotFoundException("L'employé demandé n'existe pas dans la base"));
        employeRepository.delete(toDelete);
    }

    @Transactional
    public EmployeResponse updateEmploye(Long id, EmployeRequest toUpdate) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new EmployeNotFoundException("Aucun employé trouvé avec l'id : " + id));

        // 2. Vérifier l'unicité de l'email si changé
        if (employeRepository.existsByEmailAndIdNot(toUpdate.getEmail(), id)) {
            throw new IllegalArgumentException("Un autre employé utilise déjà cet email");
        }

        // 3. Vérifier l'unicité du téléphone si changé
        if (employeRepository.existsByTelephoneAndIdNot(toUpdate.getTelephone(), id)) {
            throw new IllegalArgumentException("Un autre employé utilise déjà ce numéro de téléphone");
        }

        employe.setNom(toUpdate.getNom());
        employe.setPrenom(toUpdate.getPrenom());
        employe.setEmail(toUpdate.getEmail());
        employe.setTelephone(toUpdate.getTelephone());
        employe.setPoste(toUpdate.getPoste());
        employe.setAdresse(toUpdate.getAdresse());


        // 5. Sauvegarder et retourner le DTO de réponse
        Employe updated = employeRepository.save(employe);
        return EmployeMapper.toResponse(updated);
    }

    public void decrementSoldeConges(Long id, int days) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new EmployeNotFoundException("Aucun employé trouvé avec l'id : " + id));

        employe.setSoldeConge(employe.getSoldeConge() - days);
        employeRepository.save(employe);
    }
}
