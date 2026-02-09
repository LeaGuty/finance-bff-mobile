package cl.duoc.finance_bff_mobile.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.finance_bff_mobile.security.JwtUtil;

/**
 * Controlador REST de autenticación.
 *
 * Expone el endpoint público POST /auth/login que permite a los clientes
 * móviles autenticarse con usuario y contraseña, y recibir un token JWT
 * firmado para acceder a los endpoints protegidos del BFF.
 *
 * Flujo de autenticación:
 *   1. El cliente envía credenciales en JSON (username + password)
 *   2. Spring Security valida contra el almacén de usuarios (InMemory)
 *   3. Si es válido, se genera un token JWT con el rol del usuario
 *   4. Se retorna el token al cliente para uso en peticiones posteriores
 *
 * @author Equipo Backend 3 - Duoc UC
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint de login que autentica al usuario y genera un token JWT.
     *
     * @param request JSON con campos "username" y "password"
     * @return 200 OK con el token JWT si las credenciales son válidas,
     *         401 UNAUTHORIZED si las credenciales son incorrectas
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Validar credenciales contra el UserDetailsService configurado en SecurityConfig
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Obtener el primer rol/authority del usuario autenticado
            String role = auth.getAuthorities().iterator().next().getAuthority();

            // Generar token JWT firmado con HS512, incluyendo el rol como claim
            String token = jwtUtil.generateToken(request.getUsername(), role);

            return ResponseEntity.ok(new LoginResponse(token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Credenciales inválidas");
        }
    }

    // ========================== DTOs internos ==========================

    /**
     * DTO para deserializar el body del request de login.
     * Espera JSON: { "username": "...", "password": "..." }
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * DTO para la respuesta de login exitoso.
     * Retorna JSON: { "token": "eyJhbG..." }
     */
    public static class LoginResponse {
        private String token;

        public LoginResponse(String token) { this.token = token; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}