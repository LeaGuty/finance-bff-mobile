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

@Service
public class FinanceMobileServiceImpl implements FinanceMobileService {

    @Autowired
    private RestTemplate restTemplate;

    private final String BACKEND_URL = "http://localhost:8080/api/v1";

    // --- MÉTODO PARA OBTENER TOKEN DE LA PETICIÓN ENTRANTE ---
    private HttpHeaders getHeadersConToken() {
        HttpHeaders headers = new HttpHeaders();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null) {
                headers.set("Authorization", authHeader);
            }
        }
        return headers;
    }

    @Override
    public ResumenMobileDTO obtenerResumenMobile(Long id) {
        ResumenMobileDTO resumen = new ResumenMobileDTO();

        try {
            // Preparamos el Header con el Token
            HttpEntity<String> entity = new HttpEntity<>(getHeadersConToken());

            // 1. Obtener Cuenta (Con exchange)
            String urlCuenta = BACKEND_URL + "/cuentas/" + id;
            ResponseEntity<CuentaLiteDTO> responseCuenta = restTemplate.exchange(
                urlCuenta, 
                HttpMethod.GET, 
                entity, 
                CuentaLiteDTO.class
            );
            CuentaLiteDTO cuenta = responseCuenta.getBody();
            resumen.setCuenta(cuenta);

            // 2. Obtener Movimientos (Con exchange)
            String urlMovimientos = BACKEND_URL + "/cuentas/" + id + "/transacciones";
            ResponseEntity<List<MovimientoLiteDTO>> response = restTemplate.exchange(
                urlMovimientos,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<MovimientoLiteDTO>>() {}
            );

            List<MovimientoLiteDTO> todosLosMovimientos = response.getBody();

            // 3. Lógica de Negocio: Filtrar últimos 5
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
        } catch (HttpClientErrorException.Forbidden e) { // <--- Seguridad
            resumen.setSaludo("Sesión expirada o inválida");
        } catch (ResourceAccessException e) {
            resumen.setSaludo("Servicio no disponible temporalmente");
        } catch (Exception e) {
            resumen.setSaludo("Error al procesar su solicitud");
        }

        return resumen;
    }
}