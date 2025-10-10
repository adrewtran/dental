package edu.miu.cs489.dental.repository;

import edu.miu.cs489.dental.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}