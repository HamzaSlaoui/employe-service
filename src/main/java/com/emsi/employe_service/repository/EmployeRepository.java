package com.emsi.employe_service.repository;

import com.emsi.employe_service.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelephoneAndIdNot(String telephone, Long id);
}
