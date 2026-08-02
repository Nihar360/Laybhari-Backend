package com.laybhari.controller;

import com.laybhari.dto.AddressDto;
import com.laybhari.dto.AddressRequest;
import com.laybhari.service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    // GET /api/addresses → list current user's saved addresses
    @GetMapping
    public ResponseEntity<List<AddressDto>> getAddresses(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(addressService.getUserAddresses(email));
    }

    // POST /api/addresses → add new address
    @PostMapping
    public ResponseEntity<AddressDto> addAddress(Authentication authentication,
                                                @RequestBody AddressRequest request) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(email, request));
    }

    // PUT /api/addresses/{id} → update an address
    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(Authentication authentication,
                                                   @PathVariable Long id,
                                                   @RequestBody AddressRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(addressService.updateAddress(email, id, request));
    }

    // DELETE /api/addresses/{id} → delete an address
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(Authentication authentication,
                                              @PathVariable Long id) {
        String email = authentication.getName();
        addressService.deleteAddress(email, id);
        return ResponseEntity.noContent().build();
    }
}
