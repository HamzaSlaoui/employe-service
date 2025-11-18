package com.emsi.employe_service.entity;

import com.emsi.employe_service.enums.Poste;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false,  length = 50)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true,  length = 10)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Poste poste;


    @Column(nullable = true,  length = 100)
    private String adresse;

    @Column(nullable = false)
    private int soldeConge = 28;

}
