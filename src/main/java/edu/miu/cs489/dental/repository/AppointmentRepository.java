package edu.miu.cs489.dental.repository;

import edu.miu.cs489.dental.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByOrderByAppointmentDateTimeAsc();
}