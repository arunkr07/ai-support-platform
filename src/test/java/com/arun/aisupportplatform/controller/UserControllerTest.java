package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.entity.User;
import com.arun.aisupportplatform.entity.UserRole;
import com.arun.aisupportplatform.repository.UserRepository;
import com.arun.aisupportplatform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("mock")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void registerUser_shouldCreateCustomer() throws Exception {
        String request = """
                {
                    "name": "Test Customer",
                    "email": "customer@test.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Test Customer"))
                .andExpect(jsonPath("$.email")
                        .value("customer@test.com"))
                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"));
    }

    @Test
    void registerUser_shouldRejectDuplicateEmail() throws Exception {
        User existingUser = User.builder()
                .name("Existing User")
                .email("existing@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(existingUser);

        String request = """
                {
                    "name": "Another User",
                    "email": "existing@test.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.message")
                        .value("Email already exists"));
    }

    @Test
    void registerUser_shouldRejectInvalidData() throws Exception {
        String request = """
                {
                    "name": "",
                    "email": "invalid-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(
                        post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"));
    }

    @Test
    void loginUser_shouldReturnJwtToken() throws Exception {
        User user = User.builder()
                .name("Login User")
                .email("login@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user);

        String request = """
                {
                    "email": "login@test.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertFalse(
                                result.getResponse()
                                        .getContentAsString()
                                        .isBlank()
                        ));
    }

    @Test
    void loginUser_shouldRejectWrongPassword() throws Exception {
        User user = User.builder()
                .name("Login User")
                .email("wrong-password@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user);

        String request = """
                {
                    "email": "wrong-password@test.com",
                    "password": "wrongpassword"
                }
                """;

        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.message")
                        .value("Wrong password"));
    }

    @Test
    void loginUser_shouldRejectUnknownEmail() throws Exception {
        String request = """
                {
                    "email": "unknown@test.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.message")
                        .value("User not found"));
    }

    @Test
    void getCurrentUser_shouldRejectUnauthenticatedRequest()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/me")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser_shouldReturnAuthenticatedUser()
            throws Exception {

        User user = User.builder()
                .name("Authenticated User")
                .email("authenticated@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getEmail()
        );

        mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Authenticated User"))
                .andExpect(jsonPath("$.email")
                        .value("authenticated@test.com"))
                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"));
    }

    @Test
    void registerUser_shouldStoreEncodedPassword() throws Exception {

        String rawPassword = "password123";

        String request = """
                {
                    "name": "Password Test",
                    "email": "password@test.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk());

        User savedUser = userRepository
                .findByEmail("password@test.com")
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertNotEquals(
                rawPassword,
                savedUser.getPassword()
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        savedUser.getPassword()
                )
        );
    }
}