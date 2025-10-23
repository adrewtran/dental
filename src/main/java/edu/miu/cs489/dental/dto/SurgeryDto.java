package edu.miu.cs489.dental.dto;

public record SurgeryDto(
        Long id,
        String surgeryNo,
        AddressSimpleDto address
) {
}

