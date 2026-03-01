package cl.duoc.finance_bff_mobile.service;

import cl.duoc.finance_bff_mobile.model.CuentaLiteDTO;
import cl.duoc.finance_bff_mobile.model.MovimientoLiteDTO;
import cl.duoc.finance_bff_mobile.model.ResumenMobileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity; // <--- NUEVO
import org.springframework.http.HttpHeaders; // <--- NUEVO
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder; // <--- NUEVO
import org.springframework.web.context.request.ServletRequestAttributes; // <--- NUEVO

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio BFF Mobile.
 *
 * Orquesta las llamadas al backend principal (API REST en puerto 8080)
 * y transforma las respuestas en DTOs livianos optimizados para la app móvil.
 *
 * Responsabilidades clave:
 * - Propagar el token JWT de la petición entrante hacia el backend (token
 * forwarding)
 * - Consultar datos de cuenta y transacciones desde el backend
 * - Aplicar lógica de negocio: filtrar solo los últimos 5 movimientos
 * - Manejar errores del backend con mensajes amigables para el usuario
 *
 * @author Equipo Backend 3 - Duoc UC
 */
@Service
public class FinanceMobileServiceImpl implements FinanceMobileService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private cl.duoc.finance_bff_mobile.security.JwtUtil jwtUtil;

    // URL base del backend principal (API REST)
    private final String BACKEND_URL = "http://localhost:8080/api/v1";

    /**
     * Extrae el header Authorization de la petición HTTP entrante
     * y lo prepara para reenviarlo al backend (token forwarding).
     *
     * Esto permite que el backend valide el mismo token JWT que
     * el cliente móvil envió al BFF, manteniendo la cadena de confianza.
     *
     * @return HttpHeaders con el token Bearer si existe, vacío si no
     */
    private HttpHeaders getHeadersConToken() {
        HttpHeaders headers = new HttpHeaders();

        try {
            // 1. Obtener el usuario autenticado con GitHub desde el contexto de seguridad
            org.springframework.security.oauth2.core.user.OAuth2User oauthUser = (org.springframework.security.oauth2.core.user.OAuth2User) org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            // 2. Extraer el nombre de usuario de GitHub para log o trazabilidad (opcional)
            String githubUser = oauthUser.getAttribute("login");

            // 3. Generar el JWT para el backend.
            // Mapeamos a "usuario_movil" porque el microservicio finance-batch utiliza
            // un InMemoryUserDetailsManager que solo conoce este usuario para el rol móvil.
            String usernameInternoBff = "usuario_movil";
            String tokenInterno = jwtUtil.generateToken(usernameInternoBff, "ROLE_CLIENTE_MOVIL");

            // 4. Inyectarlo en el Header con el prefijo Bearer
            headers.set("Authorization", "Bearer " + tokenInterno);

        } catch (Exception e) {
            System.err.println("Error generando el token relay para el Core: " + e.getMessage());
        }

        return headers;
    }

    /**
     * {@inheritDoc}
     *
     * Realiza dos llamadas al backend:
     * 1. GET /api/v1/cuentas/{id} -> datos de la cuenta
     * 2. GET /api/v1/cuentas/{id}/transacciones -> lista de movimientos
     *
     * Luego aplica lógica BFF: ordena movimientos por fecha descendente
     * y retorna solo los 5 más recientes junto con un saludo personalizado.
     */
    @Override
    public ResumenMobileDTO obtenerResumenMobile(Long id) {
        ResumenMobileDTO resumen = new ResumenMobileDTO();

        try {
            // Preparar headers con el token JWT para reenviar al backend
            HttpEntity<String> entity = new HttpEntity<>(getHeadersConToken());

            // Llamada 1: Obtener datos básicos de la cuenta
            String urlCuenta = BACKEND_URL + "/cuentas/" + id;
            ResponseEntity<CuentaLiteDTO> responseCuenta = restTemplate.exchange(
                    urlCuenta,
                    HttpMethod.GET,
                    entity,
                    CuentaLiteDTO.class);
            CuentaLiteDTO cuenta = responseCuenta.getBody();
            resumen.setCuenta(cuenta);

            // Llamada 2: Obtener todas las transacciones de la cuenta
            String urlMovimientos = BACKEND_URL + "/cuentas/" + id + "/transacciones";
            ResponseEntity<List<MovimientoLiteDTO>> response = restTemplate.exchange(
                    urlMovimientos,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<MovimientoLiteDTO>>() {
                    });

            List<MovimientoLiteDTO> todosLosMovimientos = response.getBody();

            // Lógica BFF: filtrar solo los últimos 5 movimientos (orden descendente por
            // fecha)
            if (todosLosMovimientos != null) {
                List<MovimientoLiteDTO> ultimos5 = todosLosMovimientos.stream()
                        .sorted(Comparator.comparing(MovimientoLiteDTO::getFecha).reversed())
                        .limit(5)
                        .collect(Collectors.toList());

                resumen.setUltimosMovimientos(ultimos5);
            } else {
                resumen.setUltimosMovimientos(Collections.emptyList());
            }

            resumen.setSaludo("Hola " + cuenta.getNombre() + ", aquí tienes tus últimos movimientos.");

        } catch (HttpClientErrorException.NotFound e) {
            resumen.setSaludo("Cuenta no encontrada");
        } catch (HttpClientErrorException.Forbidden e) {
            resumen.setSaludo("Sesión expirada o inválida");
        } catch (ResourceAccessException e) {
            resumen.setSaludo("Servicio no disponible temporalmente");
        } catch (Exception e) {
            resumen.setSaludo("Error al procesar su solicitud");
        }

        return resumen;
    }
}