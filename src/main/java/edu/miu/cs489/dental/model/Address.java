package edu.miu.cs489.dental.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;
    private String city;
    private String zipCode;

    @OneToOne(mappedBy = "address")
    private Patient patient;

    @OneToOne(mappedBy = "address")
    private Dentist dentist;

    @OneToOne(mappedBy = "address")
    private Surgery surgery;
}