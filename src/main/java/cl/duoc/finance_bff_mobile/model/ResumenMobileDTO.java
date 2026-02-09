package cl.duoc.finance_bff_mobile.model;

import java.util.List;

import lombok.Data;

/**
 * DTO de respuesta principal del BFF Mobile.
 *
 * Agrupa toda la información que la app móvil necesita en una
 * sola respuesta: saludo personalizado, datos de la cuenta y
 * los últimos 5 movimientos. Este patrón BFF reduce la cantidad
 * de llamadas HTTP que el cliente móvil necesita realizar.
 *
 * Campos generados por Lombok (@Data): getters, setters, toString, equals, hashCode.
 *
 * @author Equipo Backend 3 - Duoc UC
 */
@Data
public class ResumenMobileDTO {
    private String saludo;                              // Mensaje personalizado (ej: "Hola Juan, ...")
    private CuentaLiteDTO cuenta;                       // Datos básicos de la cuenta
    private List<MovimientoLiteDTO> ultimosMovimientos;  // Últimos 5 movimientos ordenados por fecha desc
}