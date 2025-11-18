package com.emsi.employe_service.dto;

import com.emsi.employe_service.enums.Poste;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne doit pas dépasser 50 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne doit pas dépasser 50 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 10, message = "Le téléphone doit contenir 10 caractères maximum")
    private String telephone;

    @NotNull(message = "Le poste est obligatoire")
    private Poste poste;

    @Size(max = 100, message = "L'adresse ne doit pas dépasser 100 caractères")
    private String adresse;
}
