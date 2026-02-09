package cl.duoc.finance_bff_mobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Clase principal del microservicio BFF (Backend For Frontend) Mobile.
 *
 * Este microservicio actúa como intermediario entre la aplicación móvil
 * y los servicios backend de la plataforma financiera (puerto 8080).
 * Su responsabilidad es adaptar, filtrar y simplificar las respuestas
 * del backend para optimizar el consumo de datos en dispositivos móviles.
 *
 * Flujo general:
 *   Cliente Móvil --> BFF Mobile (puerto 8082/HTTPS) --> Backend API REST (puerto 8080)
 *
 * @author Equipo Backend 3 - Duoc UC
 */
@SpringBootApplication
public class FinanceBffMobileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceBffMobileApplication.class, args);
    }

    /**
     * Bean de RestTemplate utilizado para realizar llamadas HTTP
     * al backend principal (API REST en puerto 8080).
     * Se inyecta en FinanceMobileServiceImpl para consumir los endpoints
     * de cuentas y transacciones.
     *
     * @return instancia de RestTemplate configurada por defecto
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}