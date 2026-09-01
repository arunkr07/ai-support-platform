package com.arun.aisupportplatform.service;

import com.arun.aisupportplatform.dto.AdminUserResponse;
import com.arun.aisupportplatform.dto.LoginRequest;
import com.arun.aisupportplatform.dto.RegisterRequest;
import com.arun.aisupportplatform.dto.UserResponse;
import com.arun.aisupportplatform.entity.Ticket;
import com.arun.aisupportplatform.entity.User;
import com.arun.aisupportplatform.entity.UserRole;
import com.arun.aisupportplatform.exception.EmailAlreadyExistsException;
import com.arun.aisupportplatform.exception.UserDeletionException;
import com.arun.aisupportplatform.exception.UserNotFoundException;
import com.arun.aisupportplatform.repository.TicketRepository;
import com.arun.aisupportplatform.repository.UserRepository;
import com.arun.aisupportplatform.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final TicketRepository ticketRepository;

    public UserResponse registerUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }


    public String loginUser(LoginRequest request){

        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                )
        );

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if(!matches){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Wrong password"
            );
        }

        return jwtService.generateToken(
                user.getEmail()
        );
    }

    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public List<AdminUserResponse> getAllAgents() {

        return userRepository
                .findByRole(UserRole.AGENT)
                .stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name()
                ))
                .toList();
    }

    public List<AdminUserResponse> getAllCustomers() {

        return userRepository
                .findByRole(UserRole.CUSTOMER)
                .stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name()
                ))
                .toList();
    }

    public AdminUserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new UserDeletionException("Admin user cannot be deleted");
        }

        if (user.getRole() == UserRole.AGENT) {

            List<Ticket> assignedTickets =
                    ticketRepository.findByAssignedAgent(user);

            if (!assignedTickets.isEmpty()) {
                throw new UserDeletionException(
                        "Agent cannot be deleted while tickets are assigned"
                );
            }
        }

        userRepository.delete(user);
    }

    public AdminUserResponse createAgent(
            String name,
            String email,
            String password
    ) {

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User agent = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(UserRole.AGENT)
                .build();

        User savedAgent = userRepository.save(agent);

        return new AdminUserResponse(
                savedAgent.getId(),
                savedAgent.getName(),
                savedAgent.getEmail(),
                savedAgent.getRole().name()
        );
    }

    public AdminUserResponse updateAgent(
            Long id,
            String name,
            String email
    ) {

        User agent = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        if (agent.getRole() != UserRole.AGENT) {
            throw new RuntimeException("User is not an agent");
        }

        if (!agent.getEmail().equals(email)
                && userRepository.existsByEmail(email)) {

            throw new RuntimeException("Email already exists");
        }

        agent.setName(name);
        agent.setEmail(email);

        User updatedAgent = userRepository.save(agent);

        return new AdminUserResponse(
                updatedAgent.getId(),
                updatedAgent.getName(),
                updatedAgent.getEmail(),
                updatedAgent.getRole().name()
        );
    }

}
