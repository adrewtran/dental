package edu.miu.cs489.dental.service;

import edu.miu.cs489.dental.model.Surgery;
import edu.miu.cs489.dental.repository.SurgeryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SurgeryService {

    @Autowired
    private SurgeryRepository surgeryRepository;

    public List<Surgery> getAllSurgeries() {
        return surgeryRepository.findAll();
    }

    public Optional<Surgery> getSurgeryById(Long id) {
        return surgeryRepository.findById(id);
    }
}

