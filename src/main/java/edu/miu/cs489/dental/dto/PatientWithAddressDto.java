package edu.miu.cs489.dental.dto;

public record PatientWithAddressDto(Long id, String patNo, String name, AddressSimpleDto address) {
}

