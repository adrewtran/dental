package edu.miu.cs489.dental.repository;

import edu.miu.cs489.dental.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}