package edu.miu.cs489.dental.controller;

import edu.miu.cs489.dental.dto.AddressSimpleDto;
import edu.miu.cs489.dental.dto.DentistDto;
import edu.miu.cs489.dental.exception.ResourceNotFoundException;
import edu.miu.cs489.dental.model.Dentist;
import edu.miu.cs489.dental.service.DentistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/adsweb/api/v1")
@Tag(name = "Dentists", description = "Dentist management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DentistController {

    @Autowired
    private DentistService dentistService;

    @Operation(summary = "Get all dentists", description = "Retrieve a list of all dentists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of dentists",
                    content = @Content(schema = @Schema(implementation = DentistDto.class)))
    })
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

    @Operation(summary = "Get dentist by ID", description = "Retrieve a specific dentist by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dentist",
                    content = @Content(schema = @Schema(implementation = DentistDto.class))),
            @ApiResponse(responseCode = "404", description = "Dentist not found", content = @Content)
    })
    @GetMapping("/dentists/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public ResponseEntity<DentistDto> getDentistById(
            @Parameter(description = "Dentist ID", required = true) @PathVariable Long id) {
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

    @Operation(summary = "Create new dentist", description = "Create a new dentist record (requires OFFICE_MANAGER role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dentist successfully created",
                    content = @Content(schema = @Schema(implementation = DentistDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires OFFICE_MANAGER role", content = @Content)
    })
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

    @Operation(summary = "Update dentist", description = "Update an existing dentist record (requires OFFICE_MANAGER role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dentist successfully updated",
                    content = @Content(schema = @Schema(implementation = DentistDto.class))),
            @ApiResponse(responseCode = "404", description = "Dentist not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires OFFICE_MANAGER role", content = @Content)
    })
    @PutMapping("/dentist/{id}")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<DentistDto> updateDentist(
            @Parameter(description = "Dentist ID", required = true) @PathVariable Long id,
            @RequestBody Dentist dentistDetails) {
        Dentist updated = dentistService.updateDentist(id, dentistDetails);
        AddressSimpleDto addr = null;
        if (updated.getAddress() != null) {
            addr = new AddressSimpleDto(updated.getAddress().getId(), updated.getAddress().getStreet(),
                    updated.getAddress().getCity(), updated.getAddress().getZipCode());
        }
        DentistDto dto = new DentistDto(updated.getId(), updated.getDentistName(), addr);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Delete dentist", description = "Delete a dentist record (requires OFFICE_MANAGER role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Dentist successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Dentist not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires OFFICE_MANAGER role", content = @Content)
    })
    @DeleteMapping("/dentist/{id}")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<Void> deleteDentist(
            @Parameter(description = "Dentist ID", required = true) @PathVariable Long id) {
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

