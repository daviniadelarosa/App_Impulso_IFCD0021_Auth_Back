package com.example.auth.controllers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.auth.models.User;
import com.example.auth.services.EmailService;
import com.example.auth.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    @Autowired
    private final UserService service;
    private final EmailService emailService;

    public UserController(UserService service, EmailService emailService) {
        this.service = service;
        this.emailService = emailService;
    }

    @GetMapping
    public List<User> list() { return service.list(); }

    @GetMapping("/{id}")
    public User get(@PathVariable Long id) { return service.get(id); }

    @PostMapping("register")
    public User register(@RequestBody User user) { return service.save(user); }
    @PostMapping("login")
    public Map<String, String> login(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        String token = service.loginAndGetToken(user.getEmail(), user.getPassword());

        if (token != null) {
            response.put("message", "login ok");
            response.put("token", token);
        } else {
            response.put("message", "usuario o contraseña incorrectos");
        }
        return response;
    }
    @PatchMapping("/{id}")
    public User update(@PathVariable long id, @RequestBody User user) {
        user.setId(id);
        return service.save(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }

    @PatchMapping("/{email}/password")
    public Map<String, String> requestPasswordReset(@PathVariable String email) {
        Map<String, String> response = new HashMap<>();
        String token = service.generatePasswordResetToken(email);

        if (token != null) {
            String resetUrl = "http://localhost:8080/api/users/reset?token=" + token;

            // enviar email
            emailService.sendResetUrl(email, resetUrl);

            response.put("message", "Se ha enviado la URL de reseteo al correo " + email);
        } else {
            response.put("message", "Usuario no encontrado");
        }
        return response;
    }
    @PostMapping("/reset")
    public Map<String, String> resetPassword(
            @RequestParam("token") String token,
            @RequestBody Map<String, String> body) {

        String newPassword = body.get("password");
        Map<String, String> response = new HashMap<>();
        System.out.println(token);
        if (newPassword == null || newPassword.isBlank()) {
            response.put("message", "La nueva contraseña no puede estar vacía");
            return response;
        }

        boolean updated = service.resetPassword(token, newPassword);
        if (updated) {
            response.put("message", "Contraseña actualizada correctamente");
        } else {
            response.put("message", "Token inválido o usuario no encontrado");
        }
        return response;
    }


}
