package com.laybhari.repository;

import com.laybhari.entity.Address;
import com.laybhari.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserOrderByCreatedAtDesc(User user);
    Optional<Address> findByIdAndUser(Long id, User user);
    List<Address> findByUserAndIsDefaultTrue(User user);
}
