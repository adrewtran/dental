package edu.miu.cs489.dental.repository;

import edu.miu.cs489.dental.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("select distinct a from Address a " +
            "left join fetch a.patient p " +
            "left join fetch a.dentist d " +
            "left join fetch a.surgery s " +
            "order by lower(a.city)")
    List<Address> findAllWithRelationsOrderByCity();
}