package edu.miu.cs489.dental.controller;

import edu.miu.cs489.dental.dto.AddressSimpleDto;
import edu.miu.cs489.dental.dto.SurgeryDto;
import edu.miu.cs489.dental.model.Surgery;
import edu.miu.cs489.dental.service.SurgeryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/adsweb/api/v1")
@Tag(name = "Surgeries", description = "Surgery location management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SurgeryController {

    @Autowired
    private SurgeryService surgeryService;

    @Operation(summary = "Get all surgeries", description = "Retrieve a list of all surgery locations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of surgeries",
                    content = @Content(schema = @Schema(implementation = SurgeryDto.class)))
    })
    @GetMapping("/surgeries")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public List<SurgeryDto> getAllSurgeries() {
        List<Surgery> surgeries = surgeryService.getAllSurgeries();
        return surgeries.stream().map(s -> {
            AddressSimpleDto addr = null;
            if (s.getAddress() != null) {
                addr = new AddressSimpleDto(s.getAddress().getId(), s.getAddress().getStreet(),
                        s.getAddress().getCity(), s.getAddress().getZipCode());
            }
            return new SurgeryDto(s.getId(), s.getSurgeryNo(), addr);
        }).collect(Collectors.toList());
    }
}

