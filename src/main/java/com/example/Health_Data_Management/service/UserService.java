package com.example.Health_Data_Management.service;



import com.example.Health_Data_Management.entity.Role;
import com.example.Health_Data_Management.entity.User;
import com.example.Health_Data_Management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------------------------------------------
    // REGISTER USER
    // ---------------------------------------------------------

    public User registerUser(String name,
                             String email,
                             String password,
                             Role role) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "User with this email already exists"
            );
        }

        User user = new User();

        user.setName(name);
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(password)
        );
        user.setRole(role);
        user.setActive(true);

        return userRepository.save(user);
    }


    // ---------------------------------------------------------
    // FIND USER BY ID
    // ---------------------------------------------------------

    public Optional<User> findById(Long id) {

        return userRepository.findById(id);
    }


    // ---------------------------------------------------------
    // FIND USER BY EMAIL
    // ---------------------------------------------------------

    public Optional<User> findByEmail(String email) {

        return userRepository.findByEmail(email);
    }


    // ---------------------------------------------------------
    // GET ALL USERS
    // ---------------------------------------------------------

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }


    // ---------------------------------------------------------
    // GET USERS BY ROLE
    // ---------------------------------------------------------

    public List<User> getUsersByRole(Role role) {

        return userRepository.findByRole(role);
    }


    // ---------------------------------------------------------
    // GET ACTIVE USERS
    // ---------------------------------------------------------

    public List<User> getActiveUsers() {

        return userRepository.findByActiveTrue();
    }


    // ---------------------------------------------------------
    // GET ACTIVE USERS BY ROLE
    // ---------------------------------------------------------

    public List<User> getActiveUsersByRole(Role role) {

        return userRepository.findByRoleAndActiveTrue(role);
    }


    // ---------------------------------------------------------
    // ACTIVATE USER
    // ---------------------------------------------------------

    public User activateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        user.setActive(true);

        return userRepository.save(user);
    }


    // ---------------------------------------------------------
    // DEACTIVATE USER
    // ---------------------------------------------------------

    public User deactivateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        user.setActive(false);

        return userRepository.save(user);
    }


    // ---------------------------------------------------------
    // UPDATE USER
    // ---------------------------------------------------------

    public User updateUser(Long id,
                           String name,
                           String email) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        user.setName(name);
        user.setEmail(email);

        return userRepository.save(user);
    }


    // ---------------------------------------------------------
    // DELETE USER
    // ---------------------------------------------------------

    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new RuntimeException(
                    "User not found"
            );
        }

        userRepository.deleteById(id);
    }
}
