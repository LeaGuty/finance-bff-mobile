package cl.duoc.finance_bff_mobile.service;

import cl.duoc.finance_bff_mobile.model.CuentaLiteDTO;
import cl.duoc.finance_bff_mobile.model.MovimientoLiteDTO;
import cl.duoc.finance_bff_mobile.model.ResumenMobileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinanceMobileServiceImpl implements FinanceMobileService {

    @Autowired
    private RestTemplate restTemplate;

    private final String BACKEND_URL = "http://localhost:8080/api/v1";

    @Override
    public ResumenMobileDTO obtenerResumenMobile(Long id) {
        ResumenMobileDTO resumen = new ResumenMobileDTO();

        try {
            // 1. Obtener Cuenta (Mapeo automático a versión Lite)
            String urlCuenta = BACKEND_URL + "/cuentas/" + id;
            CuentaLiteDTO cuenta = restTemplate.getForObject(urlCuenta, CuentaLiteDTO.class);
            resumen.setCuenta(cuenta);

            // 2. Obtener Todos los Movimientos
            String urlMovimientos = BACKEND_URL + "/cuentas/" + id + "/transacciones";
            ResponseEntity<List<MovimientoLiteDTO>> response = restTemplate.exchange(
                urlMovimientos,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MovimientoLiteDTO>>() {}
            );

            List<MovimientoLiteDTO> todosLosMovimientos = response.getBody();

            // 3. Lógica de Negocio: Filtrar últimos 5
            if (todosLosMovimientos != null) {
                List<MovimientoLiteDTO> ultimos5 = todosLosMovimientos.stream()
                    // Ordenar por fecha descendente (lo más nuevo primero)
                    .sorted(Comparator.comparing(MovimientoLiteDTO::getFecha).reversed())
                    // Tomar solo los primeros 5
                    .limit(5)
                    .collect(Collectors.toList());
                
                resumen.setUltimosMovimientos(ultimos5);
            } else {
                resumen.setUltimosMovimientos(Collections.emptyList());
            }

            resumen.setSaludo("Hola " + cuenta.getNombre() + ", aquí tienes tus últimos movimientos.");

        } catch (HttpClientErrorException.NotFound e) {
            resumen.setSaludo("Cuenta no encontrada");
            resumen.setCuenta(null);
            resumen.setUltimosMovimientos(Collections.emptyList());
        } catch (ResourceAccessException e) {
            resumen.setSaludo("Servicio no disponible temporalmente");
        } catch (Exception e) {
            resumen.setSaludo("Error al procesar su solicitud");
        }

        return resumen;
    }
}