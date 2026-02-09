package cl.duoc.finance_bff_mobile.model;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO liviano de movimiento/transacción para consumo móvil.
 *
 * Representa un movimiento financiero simplificado, diseñado para
 * mostrarse en la lista de últimos movimientos de la app móvil.
 * El BFF retorna como máximo los 5 movimientos más recientes.
 *
 * Campos generados por Lombok (@Data): getters, setters, toString, equals, hashCode.
 *
 * @author Equipo Backend 3 - Duoc UC
 */
@Data
public class MovimientoLiteDTO {
    private LocalDate fecha;       // Fecha en que se realizó la transacción
    private String transaccion;    // Tipo de transacción (ej: "Abono", "Retiro")
    private Double monto;          // Monto de la transacción
}