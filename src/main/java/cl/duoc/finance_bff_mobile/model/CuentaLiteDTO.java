package cl.duoc.finance_bff_mobile.model;

import lombok.Data;

@Data
public class CuentaLiteDTO {
    // Solo lo esencial para mostrar en la pantalla principal del celular
    private String nombre;
    private Double saldo;
    private String tipo;
    // Ocultamos IDs internos y datos demográficos
}