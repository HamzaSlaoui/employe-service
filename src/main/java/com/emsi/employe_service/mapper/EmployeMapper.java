package com.emsi.employe_service.mapper;

import com.emsi.employe_service.dto.EmployeRequest;
import com.emsi.employe_service.dto.EmployeResponse;
import com.emsi.employe_service.entity.Employe;

public class EmployeMapper {

    private EmployeMapper() {
        // constructeur privé pour empêcher l'instanciation
    }

    public static Employe toEntity(EmployeRequest request) {
        if (request == null) {
            return null;
        }

        Employe employe = new Employe();
        employe.setNom(request.getNom());
        employe.setPrenom(request.getPrenom());
        employe.setEmail(request.getEmail());
        employe.setTelephone(request.getTelephone());
        employe.setPoste(request.getPoste());
        employe.setAdresse(request.getAdresse());
        // soldeConge : on laisse la valeur par défaut de l'entity (28)
        return employe;
    }

    public static EmployeResponse toResponse(Employe employe) {
        if (employe == null) {
            return null;
        }

        EmployeResponse response = new EmployeResponse();
        response.setId(employe.getId());
        response.setNom(employe.getNom());
        response.setPrenom(employe.getPrenom());
        response.setEmail(employe.getEmail());
        response.setTelephone(employe.getTelephone());
        response.setPoste(employe.getPoste());
        response.setAdresse(employe.getAdresse());
        response.setSoldeConge(employe.getSoldeConge());
        return response;
    }
}
