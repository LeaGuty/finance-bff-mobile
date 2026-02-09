package cl.duoc.finance_bff_mobile.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de seguridad JWT que intercepta cada petición HTTP entrante.
 *
 * Extiende OncePerRequestFilter para garantizar que se ejecuta una sola
 * vez por request. Se registra en la cadena de filtros de Spring Security
 * ANTES del filtro estándar UsernamePasswordAuthenticationFilter
 * (configurado en SecurityConfig).
 *
 * Flujo del filtro:
 *   1. Busca el header "Authorization: Bearer <token>" en la petición
 *   2. Si existe, extrae el username del token usando JwtUtil
 *   3. Carga los datos del usuario desde UserDetailsService (InMemory)
 *   4. Valida la integridad y vigencia del token
 *   5. Si es válido, establece la autenticación en el SecurityContext
 *      para que Spring Security autorice el acceso al endpoint
 *   6. Si no hay token o es inválido, la petición continúa sin
 *      autenticación (Spring Security denegará el acceso si es requerido)
 *
 * @author Equipo Backend 3 - Duoc UC
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // UserDetailsService es provisto por el bean InMemoryUserDetailsManager de SecurityConfig
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // Paso 1: Extraer el token del header Authorization (formato "Bearer <token>")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Token malformado, expirado o con firma inválida - se ignora
                System.out.println("Error verificando token: " + e.getMessage());
            }
        }

        // Paso 2: Si se extrajo un username y no existe autenticación previa en este request
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargar datos del usuario (roles, permisos) desde el almacén en memoria
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Paso 3: Verificar que el token sea válido (username coincide + no expirado)
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // Crear token de autenticación de Spring Security con los roles del usuario
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Paso 4: Registrar la autenticación en el contexto de seguridad
                // Esto permite que Spring Security autorice el acceso al endpoint
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuar con la cadena de filtros hacia el Controller
        chain.doFilter(request, response);
    }
}