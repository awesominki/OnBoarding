package com.onboarding.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onboarding.auth.application.dtos.user.SignInReqDto;
import com.onboarding.auth.config.JwtTokenProvider;
import com.onboarding.auth.domain.user.User;
import com.onboarding.auth.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void testUserRegistration() throws Exception {
        User user = new User();
        user.setUserName("testuser");
        user.setPassword("testpassword");
        user.setNickName("testnickname");

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByUserName("testuser").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(passwordEncoder.matches("testpassword", savedUser.getPassword())).isTrue();
    }

    @Test
    public void testUserLogin() throws Exception {
        User user = new User();
        user.setUserName("testuser");
        user.setPassword(passwordEncoder.encode("testpassword"));
        user.setNickName("testnickname");
        userRepository.save(user);

        String loginRequest = objectMapper.writeValueAsString(new SignInReqDto("testuser", "testpassword"));

        MvcResult result = mockMvc.perform(post("/users/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    public void testTokenValidation() throws Exception {
        User user = new User();
        user.setUserName("testuser");
        user.setPassword(passwordEncoder.encode("testpassword"));
        user.setNickName("testnickname");
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), String.valueOf(user.getUserRole()));

        mockMvc.perform(get("/users/validate-token")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
