package com.laybhari.service;

import com.laybhari.dto.AddressDto;
import com.laybhari.dto.AddressRequest;
import com.laybhari.entity.Address;
import com.laybhari.entity.User;
import com.laybhari.repository.AddressRepository;
import com.laybhari.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    private User getUserByEmail(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new IllegalArgumentException("User not found for: " + identifier));
    }

    @Transactional(readOnly = true)
    public List<AddressDto> getUserAddresses(String userEmail) {
        User user = getUserByEmail(userEmail);
        return addressRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toAddressDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDto createAddress(String userEmail, AddressRequest request) {
        validateAddressRequest(request);
        User user = getUserByEmail(userEmail);

        List<Address> existingAddresses = addressRepository.findByUserOrderByCreatedAtDesc(user);
        boolean isFirstAddress = existingAddresses.isEmpty();

        boolean setAsDefault = Boolean.TRUE.equals(request.getIsDefault()) || isFirstAddress;

        if (setAsDefault) {
            clearDefaultAddressForUser(user);
        }

        Address address = new Address();
        address.setUser(user);
        address.setFullName(request.getFullName().trim());
        address.setPhone(request.getPhone().trim());
        address.setLine1(request.getLine1().trim());
        address.setLine2(request.getLine2() != null ? request.getLine2().trim() : null);
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());
        address.setIsDefault(setAsDefault);
        address.setCreatedAt(LocalDateTime.now());

        Address saved = addressRepository.save(address);
        return toAddressDto(saved);
    }

    @Transactional
    public AddressDto updateAddress(String userEmail, Long addressId, AddressRequest request) {
        validateAddressRequest(request);
        User user = getUserByEmail(userEmail);

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + addressId));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefaultAddressForUser(user);
        }

        address.setFullName(request.getFullName().trim());
        address.setPhone(request.getPhone().trim());
        address.setLine1(request.getLine1().trim());
        address.setLine2(request.getLine2() != null ? request.getLine2().trim() : null);
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        Address saved = addressRepository.save(address);
        return toAddressDto(saved);
    }

    @Transactional
    public void deleteAddress(String userEmail, Long addressId) {
        User user = getUserByEmail(userEmail);
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + addressId));

        addressRepository.delete(address);
    }

    private void clearDefaultAddressForUser(User user) {
        List<Address> defaults = addressRepository.findByUserAndIsDefaultTrue(user);
        for (Address addr : defaults) {
            addr.setIsDefault(false);
            addressRepository.save(addr);
        }
    }

    private void validateAddressRequest(AddressRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Address payload is required");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("fullName is required");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("phone is required");
        }
        if (request.getLine1() == null || request.getLine1().isBlank()) {
            throw new IllegalArgumentException("line1 is required");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new IllegalArgumentException("city is required");
        }
        if (request.getState() == null || request.getState().isBlank()) {
            throw new IllegalArgumentException("state is required");
        }
        if (request.getPincode() == null || request.getPincode().isBlank()) {
            throw new IllegalArgumentException("pincode is required");
        }
    }

    public AddressDto toAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        if (address.getUser() != null) {
            dto.setUserId(address.getUser().getId());
        }
        dto.setFullName(address.getFullName());
        dto.setPhone(address.getPhone());
        dto.setLine1(address.getLine1());
        dto.setLine2(address.getLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPincode(address.getPincode());
        dto.setIsDefault(address.getIsDefault());
        dto.setCreatedAt(address.getCreatedAt());
        return dto;
    }
}
