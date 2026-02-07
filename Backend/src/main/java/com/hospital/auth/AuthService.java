package com.hospital.auth;

import com.hospital.auth.dto.AuthResponse;
import com.hospital.auth.dto.LoginRequest;
import com.hospital.auth.dto.LoginResponse;
import com.hospital.auth.dto.RegisterRequest;
import com.hospital.common.exception.InvalidCredentialsException;
import com.hospital.common.exception.UserAlreadyExistsException;
import com.hospital.security.JwtService;
import com.hospital.user.Role;
import com.hospital.user.User;
import com.hospital.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new UserAlreadyExistsException("User already exists");
    }

    User user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.PATIENT)
        .build();

    userRepository.save(user);
    return new AuthResponse("Registration successful");
  }

  public LoginResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new InvalidCredentialsException("Invalid email or password");
    }

    String token = jwtService.generateToken(user.getEmail(), user.getRole());
    return new LoginResponse(token, user.getRole().name(), user.getEmail());
  }
}
