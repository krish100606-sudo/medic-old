package com.example.Health_Data_Management.security;

import com.example.Health_Data_Management.entity.Patient;
import com.example.Health_Data_Management.entity.User;
import com.example.Health_Data_Management.repository.PatientRepository;
import com.example.Health_Data_Management.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public CustomUserDetailsService(UserRepository userRepository, PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // 1. Try finding by email
        Optional<User> userOpt = userRepository.findByEmail(identifier);

        // 2. If not found, try patient ID or phone
        if (userOpt.isEmpty()) {
            Optional<Patient> patientOpt = patientRepository.findByPatientId(identifier);
            if (patientOpt.isEmpty()) {
                patientOpt = patientRepository.findByPhone(identifier);
            }
            if (patientOpt.isPresent()) {
                userOpt = Optional.ofNullable(patientOpt.get().getUser());
            }
        }

        User user = userOpt.orElseThrow(() ->
                new UsernameNotFoundException("User not found with identifier: " + identifier));

        String role = String.valueOf(user.getRole());
        if (role == null || role.isBlank()) {
            role = "PATIENT";
        }
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
