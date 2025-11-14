package com.example.auth.services;
import com.example.auth.utils.JWTUtil;
import com.example.auth.models.User;
import com.example.auth.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    public UserService(UserRepository repo, PasswordEncoder passwordEncoder, JWTUtil jwtUtil) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public List<User> list() { return repo.findAll(); }
    public User get(Long id) { return repo.findById(id).orElse(null); }
    // Detecta si parece un hash BCrypt ($2a$, $2b$, $2y$)
    private boolean isBcryptHash(String value) {
        return value != null && value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
    // Registro (guarda siempre la contraseña encriptada)
    public User save(User user) {
        System.out.println("user email: "+user.getEmail());
        System.out.println("user pass: "+user.getPassword());
        // Si ya viene encriptada, no volver a encriptar; si viene en texto plano, encriptar
        if (!isBcryptHash(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return repo.save(user);
    }
    // Update seguro: mantener valores y encriptar si llega una nueva contraseña
    public User update(Long id, User incoming) {
        return repo.findById(id).map(existing -> {
            if (incoming.getEmail() != null) {
                existing.setEmail(incoming.getEmail());
            }
            if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
                existing.setPassword(
                        isBcryptHash(incoming.getPassword())
                                ? incoming.getPassword()
                                : passwordEncoder.encode(incoming.getPassword())
                );
            }
            return repo.save(existing);
        }).orElse(null);
    }
    public void delete(Long id) { repo.deleteById(id); }
    // Login seguro: buscar por email y comparar con el hash

    public String loginAndGetToken(String email, String rawPassword) {
        return repo.findByEmail(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .map(u -> jwtUtil.generateToken(email))
                .orElse(null);
    }

    public boolean resetPassword(String token, String newPassword) {
        try {
            String email = jwtUtil.extractEmail(token);
            return repo.findByEmail(email).map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                repo.save(user);
                return true;
            }).orElse(false);
        } catch (Exception e) {
            return false; // token inválido o expirado
        }
    }
    public String generatePasswordResetToken(String email) {
        return repo.findByEmail(email)
                .map(u -> jwtUtil.generateToken(email)) // usamos JwtUtil
                .orElse(null);
    }
}