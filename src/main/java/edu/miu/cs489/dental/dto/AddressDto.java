package edu.miu.cs489.dental.dto;

public record AddressDto(Long id, String street, String city, String zipCode, PatientDto patient) {
}

