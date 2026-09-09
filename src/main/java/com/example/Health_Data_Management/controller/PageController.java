package com.example.Health_Data_Management.controller;

import com.example.Health_Data_Management.entity.Patient;
import com.example.Health_Data_Management.entity.Role;
import com.example.Health_Data_Management.entity.User;
import com.example.Health_Data_Management.repository.PatientRepository;
import com.example.Health_Data_Management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class PageController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public PageController(
            UserRepository userRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {
        if (error != null) model.addAttribute("errorMessage", "Invalid identifier or password. Please try again.");
        if (logout != null) model.addAttribute("logoutMessage", "You have been safely logged out.");
        if (registered != null) model.addAttribute("successMessage", "Registration completed successfully. Please sign in.");
        return "login";
    }

    @GetMapping("/doctor/login")
    public String doctorLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) model.addAttribute("errorMessage", "Invalid Doctor credentials. Please try again.");
        if (logout != null) model.addAttribute("logoutMessage", "Doctor session logged out successfully.");
        return "doctor-login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "age", defaultValue = "30") Integer age,
            @RequestParam(value = "gender", defaultValue = "Male") String gender,
            @RequestParam(value = "department", defaultValue = "General Medicine") String department,
            @RequestParam(value = "preferredLanguage", defaultValue = "English") String preferredLanguage,
            @RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "abhaId", required = false) String abhaId,
            Model model) {

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("errorMessage", "An account with this email already exists.");
            return "register";
        }

        User user = new User(name, email, passwordEncoder.encode(password), Role.PATIENT);
        userRepository.save(user);

        String generatedPatientId = (patientId != null && !patientId.isBlank()) ? patientId : "P-" + (10000 + user.getId());

        Patient patient = new Patient(user, generatedPatientId, age, gender, phone, department, preferredLanguage);
        patient.setAbhaId(abhaId);
        patient.setCreatedAt(LocalDateTime.now());
        patientRepository.save(patient);

        return "redirect:/login?registered=true";
    }
}
