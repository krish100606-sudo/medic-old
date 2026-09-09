package com.example.Health_Data_Management.controller;



import com.example.Health_Data_Management.entity.Role;
import com.example.Health_Data_Management.entity.User;
import com.example.Health_Data_Management.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ---------------------------------------------------------
    // REGISTER USER
    // POST /api/users/register
    // ---------------------------------------------------------

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Role role) {

        User user = userService.registerUser(
                name,
                email,
                password,
                role
        );

        return ResponseEntity.ok(user);
    }

    // ---------------------------------------------------------
    // GET USER BY ID
    // GET /api/users/{id}
    // ---------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
            @PathVariable Long id) {

        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------------------------------------------------
    // GET USER BY EMAIL
    // GET /api/users/email?email=abc@gmail.com
    // ---------------------------------------------------------

    @GetMapping("/email")
    public ResponseEntity<User> getUserByEmail(
            @RequestParam String email) {

        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------------------------------------------------
    // GET ALL USERS
    // GET /api/users
    // ---------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    // ---------------------------------------------------------
    // ACTIVATE USER
    // PUT /api/users/{id}/activate
    // ---------------------------------------------------------

    @PutMapping("/{id}/activate")
    public ResponseEntity<User> activateUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.activateUser(id)
        );
    }

    // ---------------------------------------------------------
    // DEACTIVATE USER
    // PUT /api/users/{id}/deactivate
    // ---------------------------------------------------------

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<User> deactivateUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.deactivateUser(id)
        );
    }

    // ---------------------------------------------------------
    // DELETE USER
    // DELETE /api/users/{id}
    // ---------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                "User deleted successfully"
        );
    }
}
