package edu.miu.cs489.dental.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Data
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patNo;
    private String name;

    @OneToOne
    @JsonIgnore // prevent serializing the address when a Patient is serialized to avoid cycles
    private Address address;

    @OneToMany(mappedBy = "patient")
    private List<Appointment> appointments;
}