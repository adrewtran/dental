package edu.miu.cs489.dental.configuration;

import edu.miu.cs489.dental.model.Address;
import edu.miu.cs489.dental.model.Appointment;
import edu.miu.cs489.dental.model.Dentist;
import edu.miu.cs489.dental.model.Patient;
import edu.miu.cs489.dental.model.Role;
import edu.miu.cs489.dental.model.Surgery;
import edu.miu.cs489.dental.model.User;
import edu.miu.cs489.dental.repository.AddressRepository;
import edu.miu.cs489.dental.repository.AppointmentRepository;
import edu.miu.cs489.dental.repository.DentistRepository;
import edu.miu.cs489.dental.repository.PatientRepository;
import edu.miu.cs489.dental.repository.RoleRepository;
import edu.miu.cs489.dental.repository.SurgeryRepository;
import edu.miu.cs489.dental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DentistRepository dentistRepository;
    @Autowired
    private SurgeryRepository surgeryRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists to prevent duplicate insertion
        if (userRepository.findByUsername("admin").isPresent()) {
            System.out.println("Data already initialized. Skipping data initialization.");
            return;
        }

        System.out.println("Initializing database with sample data...");

        // Create Addresses
        Address addr1 = new Address();
        addr1.setStreet("123 Main St");
        addr1.setCity("New York");
        addr1.setZipCode("10001");
        addressRepository.save(addr1);

        Address addr2 = new Address();
        addr2.setStreet("456 Oak St");
        addr2.setCity("Los Angeles");
        addr2.setZipCode("90001");
        addressRepository.save(addr2);

        // Create Patients
        Patient p1 = new Patient();
        p1.setPatNo("P100");
        p1.setName("Gillian White");
        p1.setAddress(addr1);
        patientRepository.save(p1);

        Patient p2 = new Patient();
        p2.setPatNo("P105");
        p2.setName("Jill Bell");
        p2.setAddress(addr2);
        patientRepository.save(p2);

        // Create Dentists
        Dentist d1 = new Dentist();
        d1.setDentistName("Tony Smith");
        d1.setAddress(addr1);
        dentistRepository.save(d1);

        Dentist d2 = new Dentist();
        d2.setDentistName("Helen Pearson");
        d2.setAddress(addr2);
        dentistRepository.save(d2);

        // Create Surgeries
        Surgery s1 = new Surgery();
        s1.setSurgeryNo("S15");
        s1.setAddress(addr1);
        surgeryRepository.save(s1);

        Surgery s2 = new Surgery();
        s2.setSurgeryNo("S10");
        s2.setAddress(addr2);
        surgeryRepository.save(s2);

        // Create Appointments
        Appointment a1 = new Appointment();
        a1.setAppointmentDateTime(LocalDateTime.of(2025, 9, 12, 10, 0));
        a1.setPatient(p1);
        a1.setDentist(d1);
        a1.setSurgery(s1);
        appointmentRepository.save(a1);

        Appointment a2 = new Appointment();
        a2.setAppointmentDateTime(LocalDateTime.of(2025, 9, 12, 12, 0));
        a2.setPatient(p2);
        a2.setDentist(d1);
        a2.setSurgery(s1);
        appointmentRepository.save(a2);

        // Create Roles
        String roleName = "ROLE_OFFICE_MANAGER";
        Role role = roleRepository.findByRoleName(roleName).orElseGet(() -> {
            Role r = new Role();
            r.setRoleName(roleName);
            return roleRepository.save(r);
        });

        // Create Users
        User u1 = new User();
        u1.setUsername("admin");
        u1.setPassword(passwordEncoder.encode("password"));
        u1.setRole(role);
        userRepository.save(u1);

        System.out.println("Database initialization completed successfully!");
    }
}