package edu.miu.cs489.dental.repository;

import edu.miu.cs489.dental.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}