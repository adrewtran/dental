package edu.miu.cs489.dental.controller;

import edu.miu.cs489.dental.dto.AddressSimpleDto;
import edu.miu.cs489.dental.dto.DentistDto;
import edu.miu.cs489.dental.exception.ResourceNotFoundException;
import edu.miu.cs489.dental.model.Dentist;
import edu.miu.cs489.dental.service.DentistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/adsweb/api/v1")
public class DentistController {

    @Autowired
    private DentistService dentistService;

    @GetMapping("/dentists")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public List<DentistDto> getAllDentists() {
        List<Dentist> dentists = dentistService.getAllDentists();
        return dentists.stream().map(d -> {
            AddressSimpleDto addr = null;
            if (d.getAddress() != null) {
                addr = new AddressSimpleDto(d.getAddress().getId(), d.getAddress().getStreet(),
                        d.getAddress().getCity(), d.getAddress().getZipCode());
            }
            return new DentistDto(d.getId(), d.getDentistName(), addr);
        }).collect(Collectors.toList());
    }

    @GetMapping("/dentists/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public ResponseEntity<DentistDto> getDentistById(@PathVariable Long id) {
        Dentist dentist = dentistService.getDentistById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));

        AddressSimpleDto addr = null;
        if (dentist.getAddress() != null) {
            addr = new AddressSimpleDto(dentist.getAddress().getId(), dentist.getAddress().getStreet(),
                    dentist.getAddress().getCity(), dentist.getAddress().getZipCode());
        }
        DentistDto dto = new DentistDto(dentist.getId(), dentist.getDentistName(), addr);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/dentists")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<DentistDto> createDentist(@RequestBody Dentist dentist) {
        Dentist created = dentistService.createDentist(dentist);
        AddressSimpleDto addr = null;
        if (created.getAddress() != null) {
            addr = new AddressSimpleDto(created.getAddress().getId(), created.getAddress().getStreet(),
                    created.getAddress().getCity(), created.getAddress().getZipCode());
        }
        DentistDto dto = new DentistDto(created.getId(), created.getDentistName(), addr);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/dentist/{id}")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<DentistDto> updateDentist(@PathVariable Long id, @RequestBody Dentist dentistDetails) {
        Dentist updated = dentistService.updateDentist(id, dentistDetails);
        AddressSimpleDto addr = null;
        if (updated.getAddress() != null) {
            addr = new AddressSimpleDto(updated.getAddress().getId(), updated.getAddress().getStreet(),
                    updated.getAddress().getCity(), updated.getAddress().getZipCode());
        }
        DentistDto dto = new DentistDto(updated.getId(), updated.getDentistName(), addr);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/dentist/{id}")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<Void> deleteDentist(@PathVariable Long id) {
        dentistService.deleteDentist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dentist/search/{searchString}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public List<DentistDto> searchDentists(@PathVariable String searchString) {
        List<Dentist> results = dentistService.searchDentists(searchString);
        return results.stream().map(d -> {
            AddressSimpleDto addr = null;
            if (d.getAddress() != null) {
                addr = new AddressSimpleDto(d.getAddress().getId(), d.getAddress().getStreet(),
                        d.getAddress().getCity(), d.getAddress().getZipCode());
            }
            return new DentistDto(d.getId(), d.getDentistName(), addr);
        }).collect(Collectors.toList());
    }
}

