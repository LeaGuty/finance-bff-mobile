package cl.duoc.finance_bff_mobile.model;

import lombok.Data;

/**
 * DTO liviano de cuenta bancaria para consumo móvil.
 *
 * Contiene solo los campos esenciales para la pantalla principal
 * de la app móvil. Se omiten intencionalmente los IDs internos,
 * datos demográficos y otros campos sensibles que el backend
 * retorna pero que no son necesarios en el cliente móvil.
 *
 * Campos generados por Lombok (@Data): getters, setters, toString, equals, hashCode.
 *
 * @author Equipo Backend 3 - Duoc UC
 */
@Data
public class CuentaLiteDTO {
    private String nombre;  // Nombre del titular de la cuenta
    private Double saldo;   // Saldo actual de la cuenta
    private String tipo;    // Tipo de cuenta (ej: "Ahorro", "Corriente")
}