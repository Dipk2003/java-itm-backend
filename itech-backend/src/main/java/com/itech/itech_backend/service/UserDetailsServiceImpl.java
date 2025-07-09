package com.itech.itech_backend.service;

import com.itech.itech_backend.model.Admins;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.Vendors;
import com.itech.itech_backend.repository.AdminsRepository;
import com.itech.itech_backend.repository.UserRepository;
import com.itech.itech_backend.repository.VendorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final VendorsRepository vendorsRepository;
    private final AdminsRepository adminsRepository;

    @Override
    public UserDetails loadUserByUsername(String emailOrPhone) throws UsernameNotFoundException {
        // Try to find user in User table first
        Optional<User> user = userRepository.findByEmailOrPhone(emailOrPhone, emailOrPhone);
        if (user.isPresent()) {
            User u = user.get();
            return new org.springframework.security.core.userdetails.User(
                    emailOrPhone,
                    u.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(u.getRole()))
            );
        }
        
        // Try to find user in Vendors table
        Optional<Vendors> vendor = vendorsRepository.findByEmailOrPhone(emailOrPhone, emailOrPhone);
        if (vendor.isPresent()) {
            Vendors v = vendor.get();
            return new org.springframework.security.core.userdetails.User(
                    emailOrPhone,
                    v.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(v.getRole()))
            );
        }
        
        // Try to find user in Admins table
        Optional<Admins> admin = adminsRepository.findByEmailOrPhone(emailOrPhone, emailOrPhone);
        if (admin.isPresent()) {
            Admins a = admin.get();
            return new org.springframework.security.core.userdetails.User(
                    emailOrPhone,
                    a.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(a.getRole()))
            );
        }
        
        // If not found in any table, throw exception
        throw new UsernameNotFoundException("User not found with email or phone: " + emailOrPhone);
    }
}
