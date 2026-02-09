package cl.duoc.finance_bff_mobile.model;

import java.util.List;

import lombok.Data;

@Data
public class ResumenMobileDTO {
    private String saludo; // Mensaje corto
    private CuentaLiteDTO cuenta;
    private List<MovimientoLiteDTO> ultimosMovimientos; // Solo enviaremos 5 aquí
}