package edu.miu.cs489.dental.repository;

import edu.miu.cs489.dental.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("select p from Patient p left join p.address a where " +
            "lower(p.name) like lower(concat('%', :s, '%')) or " +
            "lower(p.patNo) like lower(concat('%', :s, '%')) or " +
            "lower(a.street) like lower(concat('%', :s, '%')) or " +
            "lower(a.city) like lower(concat('%', :s, '%')) or " +
            "lower(a.zipCode) like lower(concat('%', :s, '%'))")
    List<Patient> search(@Param("s") String s);
}