package edu.miu.cs489.dental.dto;

public record DentistDto(
        Long id,
        String dentistName,
        AddressSimpleDto address
) {
}

