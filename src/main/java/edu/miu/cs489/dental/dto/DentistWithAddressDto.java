package edu.miu.cs489.dental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DentistWithAddressDto {
    private Long id;
    private String dentistName;
    private AddressSimpleDto address;
}

