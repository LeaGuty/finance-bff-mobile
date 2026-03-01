package cl.duoc.finance_bff_mobile.service;

import cl.duoc.finance_bff_mobile.model.CuentaLiteDTO;
import cl.duoc.finance_bff_mobile.model.MovimientoLiteDTO;
import cl.duoc.finance_bff_mobile.model.ResumenMobileDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinanceMobileServiceImpl implements FinanceMobileService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private cl.duoc.finance_bff_mobile.security.JwtUtil jwtUtil;

    private final String BACKEND_URL = "http://localhost:8080/api/v1";

    private HttpHeaders getHeadersConToken() {
        HttpHeaders headers = new HttpHeaders();
        try {
            org.springframework.security.oauth2.core.user.OAuth2User oauthUser = (org.springframework.security.oauth2.core.user.OAuth2User) org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            String usernameInternoBff = "usuario_movil";
            String tokenInterno = jwtUtil.generateToken(usernameInternoBff, "ROLE_CLIENTE_MOVIL");

            headers.set("Authorization", "Bearer " + tokenInterno);
        } catch (Exception e) {
            System.err.println("Error generando el token relay para el Core: " + e.getMessage());
        }
        return headers;
    }

    @Override
    @CircuitBreaker(name = "financeCore", fallbackMethod = "fallbackObtenerResumenMobile")
    public ResumenMobileDTO obtenerResumenMobile(Long id) {
        ResumenMobileDTO resumen = new ResumenMobileDTO();
        HttpEntity<String> entity = new HttpEntity<>(getHeadersConToken());

        try {
            // Llamada 1: Obtener datos básicos de la cuenta
            String urlCuenta = BACKEND_URL + "/cuentas/" + id;
            ResponseEntity<CuentaLiteDTO> responseCuenta = restTemplate.exchange(
                    urlCuenta, HttpMethod.GET, entity, CuentaLiteDTO.class);
            CuentaLiteDTO cuenta = responseCuenta.getBody();
            resumen.setCuenta(cuenta);

            // Llamada 2: Obtener todas las transacciones de la cuenta
            String urlMovimientos = BACKEND_URL + "/cuentas/" + id + "/transacciones";
            ResponseEntity<List<MovimientoLiteDTO>> response = restTemplate.exchange(
                    urlMovimientos, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<MovimientoLiteDTO>>() {
                    });

            List<MovimientoLiteDTO> todosLosMovimientos = response.getBody();

            // Lógica BFF: filtrar solo los últimos 5 movimientos
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

        } catch (HttpClientErrorException e) {
            // Atrapamos errores de cliente (4xx) para no abrir el cortacircuitos
            if (e.getStatusCode().value() == 404) {
                resumen.setSaludo("Aviso: Cuenta no encontrada");
            } else if (e.getStatusCode().value() == 403) {
                resumen.setSaludo("Aviso: Sesión expirada o inválida");
            } else {
                resumen.setSaludo("Error al procesar su solicitud");
            }
            resumen.setUltimosMovimientos(Collections.emptyList());
        }
        // Excepciones como ResourceAccessException (backend caído) no se atrapan aquí, 
        // por lo que subirán y dispararán el fallback de Resilience4j.

        return resumen;
    }

    /**
     * MÉTODO DE RESPALDO (FALLBACK)
     * Se activa automáticamente si el backend principal no responde o tarda demasiado.
     */
    public ResumenMobileDTO fallbackObtenerResumenMobile(Long id, Throwable t) {
        System.err.println("¡Circuit Breaker activado en BFF Mobile! Falló la comunicación: " + t.getMessage());
        
        ResumenMobileDTO resumenFallback = new ResumenMobileDTO();
        resumenFallback.setSaludo("Servicios móviles temporalmente no disponibles. Por favor, intente más tarde.");
        resumenFallback.setCuenta(null);
        resumenFallback.setUltimosMovimientos(Collections.emptyList());
        
        return resumenFallback;
    }
}