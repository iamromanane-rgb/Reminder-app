package com.reminder.backend;

import com.reminder.backend.admin.AdminAccessService;
import com.reminder.backend.auth.JwtService;
import com.reminder.backend.models.AccessLevel;
import com.reminder.backend.models.Event;
import com.reminder.backend.models.User;
import com.reminder.backend.repositories.EventRepository;
import com.reminder.backend.repositories.UserRepository;
import com.reminder.backend.scheduler.DynamicSchedulerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=01234567890123456789012345678901",
        "jwt.expiration-ms=3600000"
})
@AutoConfigureMockMvc
class SecurityHardeningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AdminAccessService adminAccessService;

    @MockBean
    private DynamicSchedulerService dynamicSchedulerService;

    @Test
    void unauthenticatedUserEndpointIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginEndpointIsPublicNotForbidden() throws Exception {
        Mockito.when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"secret123"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminCannotListUsers() throws Exception {
        String token = bearerToken("user1@example.com", 1L);
        Mockito.when(adminAccessService.isAdmin("user1@example.com")).thenReturn(false);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotReadAnotherUsersEvents() throws Exception {
        String token = bearerToken("user1@example.com", 1L);
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        Mockito.when(adminAccessService.isAdmin("user1@example.com")).thenReturn(false);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        mockMvc.perform(get("/api/users/2/events")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanReadOwnEvents() throws Exception {
        String token = bearerToken("user1@example.com", 1L);
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        Mockito.when(adminAccessService.isAdmin("user1@example.com")).thenReturn(false);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(eventRepository.findByUserId(1L)).thenReturn(List.of(new Event()));

        mockMvc.perform(get("/api/users/1/events")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    private String bearerToken(String email, Long userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setUsername("tester");
        user.setEmpId(1234L + userId);
        user.setAccessLevel(AccessLevel.READ);
        return "Bearer " + jwtService.generateToken(user);
    }
}

