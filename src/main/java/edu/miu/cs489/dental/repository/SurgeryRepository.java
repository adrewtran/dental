package edu.miu.cs489.dental.repository;

import edu.miu.cs489.dental.model.Surgery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurgeryRepository extends JpaRepository<Surgery, Long> {
}