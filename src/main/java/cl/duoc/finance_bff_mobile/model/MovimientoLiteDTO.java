package cl.duoc.finance_bff_mobile.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MovimientoLiteDTO {
    private LocalDate fecha;
    private String transaccion; // Ejemplo: "Abono", "Retiro"
    private Double monto;
}