package edu.miu.cs489.dental.controller;

import edu.miu.cs489.dental.dto.AddressDto;
import edu.miu.cs489.dental.dto.PatientDto;
import edu.miu.cs489.dental.model.Address;
import edu.miu.cs489.dental.model.Patient;
import edu.miu.cs489.dental.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/adsweb/api/v1")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/addresses")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public List<AddressDto> getAllAddresses() {
        List<Address> addresses = addressService.getAllAddresses();
        return addresses.stream().map(a -> {
            Patient p = a.getPatient();
            PatientDto pd = null;
            if (p != null) {
                pd = new PatientDto(p.getId(), p.getPatNo(), p.getName());
            }
            return new AddressDto(a.getId(), a.getStreet(), a.getCity(), a.getZipCode(), pd);
        }).collect(Collectors.toList());
    }
}
