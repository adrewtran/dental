package edu.miu.cs489.dental.repository;

import edu.miu.cs489.dental.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}