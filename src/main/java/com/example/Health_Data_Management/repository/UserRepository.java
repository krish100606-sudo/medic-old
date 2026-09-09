package com.example.Health_Data_Management.repository;



import com.example.Health_Data_Management.entity.Role;
import com.example.Health_Data_Management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByActiveTrue();

    List<User> findByRoleAndActiveTrue(Role role);
}
