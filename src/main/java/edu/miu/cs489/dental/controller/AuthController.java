package edu.miu.cs489.dental.controller;

import edu.miu.cs489.dental.model.Role;
import edu.miu.cs489.dental.model.User;
import edu.miu.cs489.dental.repository.RoleRepository;
import edu.miu.cs489.dental.repository.UserRepository;
import edu.miu.cs489.dental.security.AuthenticationRequest;
import edu.miu.cs489.dental.security.AuthenticationResponse;
import edu.miu.cs489.dental.security.JwtUtil;
import edu.miu.cs489.dental.security.RegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "User login", description = "Authenticate user and receive JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(request.username());
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }

    @Operation(summary = "User registration", description = "Register a new user with username, password, and role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered", content = @Content),
            @ApiResponse(responseCode = "400", description = "Username already exists", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest req) {
        Optional<User> existing = userRepository.findByUsername(req.username());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        Role role = roleRepository.findByRoleName(req.roleName() == null ? "ROLE_USER" : req.roleName())
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleName(req.roleName() == null ? "ROLE_USER" : req.roleName());
                    return roleRepository.save(r);
                });

        User u = new User();
        u.setUsername(req.username());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setRole(role);
        userRepository.save(u);

        return ResponseEntity.ok("User registered");
    }
}

