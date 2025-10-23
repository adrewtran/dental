package edu.miu.cs489.dental.repository;

import edu.miu.cs489.dental.model.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DentistRepository extends JpaRepository<Dentist, Long> {
    List<Dentist> findByDentistNameContainingIgnoreCase(String searchString);
}