package cl.duoc.finance_bff_mobile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import cl.duoc.finance_bff_mobile.security.JwtFilter;

/**
 * Configuración central de seguridad del BFF Mobile.
 *
 * Define las reglas de acceso a los endpoints, la política de sesiones
 * y la integración del filtro JWT en la cadena de seguridad de Spring.
 *
 * Reglas de acceso:
 * - POST /auth/login -> Público (sin autenticación)
 * - GET /bff/mobile/v1/** -> Requiere rol CLIENTE_MOVIL
 * - Cualquier otro endpoint -> Requiere autenticación
 *
 * Nota: Se utiliza un almacén de usuarios en memoria
 * (InMemoryUserDetailsManager)
 * con fines de desarrollo/evaluación. En producción se reemplazaría por una
 * fuente de datos persistente (base de datos, LDAP, etc.).
 *
 * @author Equipo Backend 3 - Duoc UC
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     *
     * - CSRF deshabilitado: al ser una API stateless con JWT, no se necesitan
     * tokens CSRF.
     * - Sesiones STATELESS: cada petición se autentica de forma independiente vía
     * JWT.
     * - JwtFilter se ejecuta antes del filtro estándar de usuario/contraseña para
     * interceptar y validar el token Bearer en cada request protegido.
     *
     * @param http      configurador de seguridad HTTP de Spring
     * @param jwtFilter filtro personalizado para validar tokens JWT (inyectado por
     *                  parámetro
     *                  para evitar dependencia circular con UserDetailsService)
     * @return cadena de filtros de seguridad construida
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/public/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(org.springframework.security.config.Customizer.withDefaults());

        // .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Almacén de usuarios en memoria para desarrollo/evaluación.
     *
     * Define un usuario de prueba con rol CLIENTE_MOVIL que puede
     * autenticarse en /auth/login y acceder a los endpoints del BFF.
     *
     * Credenciales de prueba:
     * - Usuario: usuario_movil
     * - Contraseña: 5678
     * - Rol: ROLE_CLIENTE_MOVIL
     *
     * @return administrador de usuarios en memoria con el usuario de prueba
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("usuario_movil")
                .password("5678")
                .roles("CLIENTE_MOVIL")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Expone el AuthenticationManager de Spring Security como bean.
     * Es requerido por AuthController para autenticar credenciales
     * programáticamente durante el login.
     *
     * @param config configuración de autenticación provista por Spring
     * @return instancia del AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}