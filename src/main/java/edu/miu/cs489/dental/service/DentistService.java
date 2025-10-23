package edu.miu.cs489.dental.service;

import edu.miu.cs489.dental.model.Dentist;
import edu.miu.cs489.dental.repository.DentistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DentistService {

    @Autowired
    private DentistRepository dentistRepository;

    public List<Dentist> getAllDentists() {
        return dentistRepository.findAll();
    }

    public Optional<Dentist> getDentistById(Long id) {
        return dentistRepository.findById(id);
    }

    public Dentist createDentist(Dentist dentist) {
        return dentistRepository.save(dentist);
    }

    public Dentist updateDentist(Long id, Dentist dentistDetails) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dentist not found with id: " + id));
        dentist.setDentistName(dentistDetails.getDentistName());
        dentist.setAddress(dentistDetails.getAddress());
        return dentistRepository.save(dentist);
    }

    public void deleteDentist(Long id) {
        dentistRepository.deleteById(id);
    }

    public List<Dentist> searchDentists(String searchString) {
        return dentistRepository.findByDentistNameContainingIgnoreCase(searchString);
    }
}

