package edu.miu.cs489.dental.service;

import edu.miu.cs489.dental.model.Address;
import edu.miu.cs489.dental.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public List<Address> getAllAddresses() {
        // Use repository method that fetches related entities to avoid lazy-loading/serialization problems
        return addressRepository.findAllWithRelationsOrderByCity();
    }
}
