package com.boombet.boombet_backend.service;

import com.boombet.boombet_backend.dao.AfiliadorRepository;
import com.boombet.boombet_backend.dao.JugadorRepository;
import com.boombet.boombet_backend.dao.UsuarioRepository;
import com.boombet.boombet_backend.dto.AffiliationDTO;
import com.boombet.boombet_backend.dto.AuthDTO;
import com.boombet.boombet_backend.dto.CasinoDTO;
import com.boombet.boombet_backend.dto.LoginRequestDTO;
import com.boombet.boombet_backend.dto.RegistroRequestDTO;
import com.boombet.boombet_backend.dto.UsuarioDTO;
import com.boombet.boombet_backend.entity.Afiliador;
import com.boombet.boombet_backend.entity.Jugador;
import com.boombet.boombet_backend.entity.Usuario;
import com.boombet.boombet_backend.utils.UsuarioUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private JugadorRepository jugadorRepository;
    @Mock
    private AfiliadorRepository afiliadorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailService emailService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RestClient restClient;
    @Mock
    private JugadorService jugadorService;
    @Mock
    private WebSocketService webSocketService;
    @Mock
    private AzureBlobService azureBlobService;
    @Mock
    private FCMService fcmService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        try {
            java.lang.reflect.Field frontVerifyUrlField = UsuarioService.class.getDeclaredField("frontVerifyUrl");
            frontVerifyUrlField.setAccessible(true);
            frontVerifyUrlField.set(usuarioService, "http://localhost:3000/verify/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void register_Success() {
        RegistroRequestDTO request = new RegistroRequestDTO();
        AffiliationDTO confirmedData = new AffiliationDTO();
        confirmedData.setUser("testuser");
        confirmedData.setPassword("Password123!");
        confirmedData.setEmail("test@test.com");
        confirmedData.setDni("12345678");
        confirmedData.setGenero("Masculino");
        confirmedData.setTokenAfiliador("AF123");
        request.setConfirmedData(confirmedData);
        request.setFcmToken("fcm-token");

        when(usuarioRepository.existsByUsername("testuser")).thenReturn(false);
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(jugadorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(jugadorRepository.findByDni(anyString())).thenReturn(Optional.empty());

        Jugador mockJugador = new Jugador();
        mockJugador.setId(1L);
        when(jugadorService.crearJugador(any(AffiliationDTO.class))).thenReturn(mockJugador);

        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        
        Afiliador mockAfiliador = new Afiliador();
        mockAfiliador.setCantAfiliaciones(0);
        when(afiliadorRepository.findByTokenAfiliador("AF123")).thenReturn(Optional.of(mockAfiliador));

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        when(jwtService.generateAccessToken(any(Usuario.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(Usuario.class))).thenReturn("refresh-token");

        try (MockedStatic<UsuarioUtils> utilities = Mockito.mockStatic(UsuarioUtils.class)) {
            utilities.when(() -> UsuarioUtils.validarFormatoPassword(anyString())).thenAnswer(i -> null);
            utilities.when(() -> UsuarioUtils.construirEmailBienvenida(any(), anyString())).thenReturn("<html></html>");

            AuthDTO.AuthResponseDTO response = usuarioService.register(request);

            assertNotNull(response);
            assertEquals("access-token", response.accessToken());
            assertEquals("refresh-token", response.refreshToken());
            
            verify(afiliadorRepository).save(mockAfiliador);
            assertEquals(1, mockAfiliador.getCantAfiliaciones());
            verify(emailService).enviarCorreo(eq("test@test.com"), anyString(), anyString());
        }
    }

    @Test
    void register_UserAlreadyExists() {
        RegistroRequestDTO request = new RegistroRequestDTO();
        AffiliationDTO confirmedData = new AffiliationDTO();
        confirmedData.setUser("existingUser");
        confirmedData.setPassword("Password123!");
        request.setConfirmedData(confirmedData);

        try (MockedStatic<UsuarioUtils> utilities = Mockito.mockStatic(UsuarioUtils.class)) {
            when(usuarioRepository.existsByUsername("existingUser")).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> usuarioService.register(request));
        }
    }

    @Test
    void login_Success() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentifier("testuser");
        request.setPassword("Password123!");
        request.setFcmToken("new-fcm-token");

        Usuario mockUsuario = new Usuario();
        mockUsuario.setId(1L);
        mockUsuario.setUsername("testuser");
        mockUsuario.setEmail("test@test.com");
        mockUsuario.setVerified(true);
        mockUsuario.setPassword("hashedPass");

        when(usuarioRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(mockUsuario));
        when(jwtService.generateAccessToken(mockUsuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(mockUsuario)).thenReturn("refresh-token");

        AuthDTO.AuthResponseDTO response = usuarioService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("new-fcm-token", response.fcmToken());
        
        verify(authenticationManager).authenticate(any());
        verify(usuarioRepository).save(mockUsuario);
        assertEquals("new-fcm-token", mockUsuario.getFcmToken());
    }

    @Test
    void login_Fail_UserNotVerified() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentifier("testuser");
        request.setPassword("Password123!");

        Usuario mockUsuario = new Usuario();
        mockUsuario.setUsername("testuser");
        mockUsuario.setVerified(false);

        when(usuarioRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(mockUsuario));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> usuarioService.login(request));
        assertEquals("Usuario no verificado", exception.getMessage());
    }

    @Test
    void verificarUsuario_Success() {
        String token = "valid-token";
        Usuario mockUsuario = new Usuario();
        mockUsuario.setId(1L);
        mockUsuario.setVerificationToken(token);
        mockUsuario.setVerified(false);

        when(usuarioRepository.findByVerificationToken(token)).thenReturn(Optional.of(mockUsuario));

        usuarioService.verificarUsuario(token);

        assertTrue(mockUsuario.isVerified());
        assertNull(mockUsuario.getVerificationToken());
        verify(usuarioRepository).save(mockUsuario);
    }

    @Test
    void verificarUsuario_Fail_InvalidToken() {
        String token = "invalid-token";
        when(usuarioRepository.findByVerificationToken(token)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> usuarioService.verificarUsuario(token));
        assertEquals("El link de verificación es inválido o ya fue utilizado.", exception.getMessage());
    }

    @Test
    void solicitarCambioDeContraseña_Success() {
        String email = "test@test.com";
        Usuario mockUsuario = new Usuario();
        mockUsuario.setEmail(email);
        mockUsuario.setUsername("testuser");

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(mockUsuario));
        
        try (MockedStatic<UsuarioUtils> utilities = Mockito.mockStatic(UsuarioUtils.class)) {
             utilities.when(() -> UsuarioUtils.construirEmailRecuperacion(anyString(), anyString())).thenReturn("<html></html>");

            usuarioService.solicitarCambioDeContraseña(email);

            assertNotNull(mockUsuario.getResetToken());
            verify(usuarioRepository).save(mockUsuario);
            verify(emailService).enviarCorreo(eq(email), anyString(), anyString());
        }
    }

    @Test
    void solicitarCambioDeContraseña_Fail_EmailNotFound() {
        String email = "nonexistent@test.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> usuarioService.solicitarCambioDeContraseña(email));
        assertEquals("No existe un usuario registrado con este email.", exception.getMessage());
    }

    @Test
    void restablecerContrasena_Success() {
        String token = "reset-token";
        String newPassword = "NewPassword123!";
        Usuario mockUsuario = new Usuario();
        mockUsuario.setResetToken(token);

        when(usuarioRepository.findByResetToken(token)).thenReturn(Optional.of(mockUsuario));
        when(passwordEncoder.encode(newPassword)).thenReturn("hashedNewPass");

        try (MockedStatic<UsuarioUtils> utilities = Mockito.mockStatic(UsuarioUtils.class)) {
            utilities.when(() -> UsuarioUtils.validarFormatoPassword(anyString())).thenAnswer(i -> null);

            usuarioService.restablecerContrasena(token, newPassword);

            assertEquals("hashedNewPass", mockUsuario.getPassword());
            assertNull(mockUsuario.getResetToken());
            verify(usuarioRepository).save(mockUsuario);
        }
    }

    @Test
    void restablecerContrasena_Fail_InvalidToken() {
        String token = "invalid-token";
        String newPassword = "NewPassword123!";
        when(usuarioRepository.findByResetToken(token)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> usuarioService.restablecerContrasena(token, newPassword));
        assertEquals("El enlace de recuperación es inválido o ya fue utilizado.", exception.getMessage());
    }

    @Test
    void obtenerDatosDeUsuario_Success() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("testuser");
        usuario.setDni("12345678");
        usuario.setEmail("test@test.com");
        usuario.setBondaEnabled(true);
        usuario.setDeleted(false);
        usuario.setIconUrl("http://icon.url");

        UsuarioDTO.UsuarioResponse response = usuarioService.obtenerDatosDeUsuario(usuario);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("testuser", response.username());
        assertEquals("test@test.com", response.email());
        assertEquals("http://icon.url", response.iconUrl());
    }

    @Test
    void desafiliar_Success() {
        Long userId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        Jugador jugador = new Jugador();
        jugador.setId(10L);
        usuario.setJugador(jugador);

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(jugadorRepository.findById(10L)).thenReturn(Optional.of(jugador));

        usuarioService.desafiliar(userId);

        verify(usuarioRepository).delete(usuario);
        verify(jugadorRepository).delete(jugador);
    }

    @Test
    void cambiarIcono_Success() {
        Long userId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        usuario.setIconUrl("old-url");

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
        try {
            java.lang.reflect.Field iconsContainerField = UsuarioService.class.getDeclaredField("iconsContainer");
            iconsContainerField.setAccessible(true);
            iconsContainerField.set(usuarioService, "icons-container");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        when(azureBlobService.uploadFile(any(MultipartFile.class), eq("icons-container"))).thenReturn("new-url");

        String newUrl = usuarioService.cambiarIcono(userId, multipartFile);

        assertEquals("new-url", newUrl);
        verify(azureBlobService).deleteBlob("old-url", "icons-container");
        verify(usuarioRepository).save(usuario);
        assertEquals("new-url", usuario.getIconUrl());
    }

    @Test
    void listarCasinosAfiliados_Success() {
        Long userId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        Jugador jugador = new Jugador();
        jugador.setId(10L);
        usuario.setJugador(jugador);

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(jugadorRepository.encontrarCasinosDelJugador(10L)).thenReturn(Collections.emptyList());

        List<CasinoDTO.casinosList> result = usuarioService.listarCasinosAfiliados(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jugadorRepository).encontrarCasinosDelJugador(10L);
    }
}