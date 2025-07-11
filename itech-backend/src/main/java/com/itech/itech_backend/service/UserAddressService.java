package com.itech.itech_backend.service;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.UserAddress;
import com.itech.itech_backend.repository.UserAddressRepository;
import com.itech.itech_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    public List<UserAddress> getUserAddresses(Long userId) {
        return userAddressRepository.findByUserIdOrderByIsDefaultDesc(userId);
    }

    public UserAddress createAddress(Long userId, UserAddress address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        address.setUser(user);

        // If this is the first address or marked as default, make it default
        if (address.isDefault() || userAddressRepository.countByUserId(userId) == 0) {
            // Remove default from other addresses
            setDefaultAddress(userId, null);
            address.setDefault(true);
        }

        return userAddressRepository.save(address);
    }

    public UserAddress updateAddress(Long userId, Long addressId, UserAddress addressDetails) {
        UserAddress address = userAddressRepository.findByUserIdAndId(userId, addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Update fields
        address.setAddressType(addressDetails.getAddressType());
        address.setFullName(addressDetails.getFullName());
        address.setAddressLine1(addressDetails.getAddressLine1());
        address.setAddressLine2(addressDetails.getAddressLine2());
        address.setCity(addressDetails.getCity());
        address.setState(addressDetails.getState());
        address.setPincode(addressDetails.getPincode());
        address.setPhone(addressDetails.getPhone());

        // Handle default status
        if (addressDetails.isDefault() && !address.isDefault()) {
            setDefaultAddress(userId, addressId);
        }

        return userAddressRepository.save(address);
    }

    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = userAddressRepository.findByUserIdAndId(userId, addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        boolean wasDefault = address.isDefault();
        userAddressRepository.delete(address);

        // If deleted address was default, make another one default
        if (wasDefault) {
            List<UserAddress> remainingAddresses = userAddressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                remainingAddresses.get(0).setDefault(true);
                userAddressRepository.save(remainingAddresses.get(0));
            }
        }
    }

    public UserAddress setDefaultAddress(Long userId, Long addressId) {
        // Remove default from all addresses
        List<UserAddress> userAddresses = userAddressRepository.findByUserId(userId);
        for (UserAddress addr : userAddresses) {
            if (addr.isDefault()) {
                addr.setDefault(false);
                userAddressRepository.save(addr);
            }
        }

        // Set new default if addressId provided
        if (addressId != null) {
            UserAddress address = userAddressRepository.findByUserIdAndId(userId, addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            address.setDefault(true);
            return userAddressRepository.save(address);
        }

        return null;
    }

    public Optional<UserAddress> getDefaultAddress(Long userId) {
        return userAddressRepository.findByUserIdAndIsDefaultTrue(userId);
    }

    public UserAddress getAddressById(Long userId, Long addressId) {
        return userAddressRepository.findByUserIdAndId(userId, addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
    }

    public List<UserAddress> getAddressesByType(Long userId, String addressType) {
        return userAddressRepository.findByUserIdAndAddressType(userId, addressType);
    }
}
