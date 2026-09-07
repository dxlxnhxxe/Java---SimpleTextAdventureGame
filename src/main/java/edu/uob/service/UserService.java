package edu.uob.service;

import edu.uob.dto.AuthRequest;
import edu.uob.dto.AuthResponse;
import edu.uob.dto.UserProfileResponse;
import edu.uob.persistence.entity.UserEntity;
import edu.uob.persistence.repository.UserRepository;
import edu.uob.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        String identifier = request.getUsername();
        if (identifier == null || identifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email or username cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 4 characters long");
        }

        String username = identifier.trim();
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email is already taken");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        UserEntity user = new UserEntity(username, hashedPassword, "ROLE_USER");
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(username, "ROLE_USER");
        return new AuthResponse(token, username, "ROLE_USER", jwtTokenProvider.getExpirationMs());
    }

    public AuthResponse login(AuthRequest request) {
        String identifier = request.getUsername();
        if (identifier == null || request.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email or username and password are required");
        }

        String username = identifier.trim();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(username, user.getRole());
        return new AuthResponse(token, username, user.getRole(), jwtTokenProvider.getExpirationMs());
    }

    public UserProfileResponse getProfile(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserProfileResponse(user.getUsername(), user.getRole(), user.getCreatedAt());
    }
}
