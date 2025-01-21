package com.authentication.borghi.controller;

import com.authentication.borghi.security.SecurityConfig;
import com.authentication.borghi.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import(SecurityConfig.class)
class LoginControllerTest {


    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldReturnLoginView() throws Exception {
        // WHEN & THEN
        mockMvc.perform(MockMvcRequestBuilders.get("/showMyCustomLogin"))
                .andExpect(status().isOk()) // Verifica que el estado HTTP sea 200
                .andExpect(view().name("login")); // Verifica que el nombre de la vista sea "login"
    }

    @Test
    void shouldReturnCreateAccountViewWithUserDTO() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/showCreateAccount"))
                .andExpect(status().isOk()) // Verifica que el estado HTTP sea 200 (OK)
                .andExpect(view().name("createAccount")) // Verifica que la vista sea "createAccount"
                .andExpect(model().attributeExists("userDTO")); // Verifica que el modelo contiene "userDTO"
    }

    @Test
    void shouldShowHomeForOauth2User() throws Exception {
        // Simular un usuario OIDC
        OAuth2User oAuth2User = Mockito.mock(OAuth2User.class);
        Mockito.when(oAuth2User.getAttribute("name")).thenReturn("John Doe");

        // Configurar el contexto de seguridad con el usuario simulado
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(oAuth2User, null, List.of())
        );

        // Realizar la prueba
        mockMvc.perform(MockMvcRequestBuilders.get("/home"))
                .andExpect(status().isOk()) // Verifica que el estado HTTP sea 200
                .andExpect(view().name("home")) // Verifica que la vista sea "home"
                .andExpect(model().attribute("name", "John Doe")); // Verifica que el modelo tiene el atributo correcto

        // Verificar que el servicio fue llamado
        Mockito.verify(userService).saveOauthUser(oAuth2User);
    }

    @Test
    void shouldShowHomeForLocalUser() throws Exception {
        // Simular un usuario con detalles locales
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn("user@example.com");

        // Configurar el contexto de seguridad con el usuario simulado
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of())
        );

        // Realizar la prueba
        mockMvc.perform(MockMvcRequestBuilders.get("/home"))
                .andExpect(status().isOk()) // Verifica que el estado HTTP sea 200
                .andExpect(view().name("home")) // Verifica que la vista sea "home"
                .andExpect(model().attribute("name", "user@example.com")); // Verifica que el modelo tiene el atributo correcto
    }
}