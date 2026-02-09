package cl.duoc.finance_bff_mobile.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.finance_bff_mobile.model.ResumenMobileDTO;
import cl.duoc.finance_bff_mobile.service.FinanceMobileService;

/**
 * Controlador REST principal del BFF Mobile.
 *
 * Expone los endpoints optimizados para la aplicación móvil bajo
 * la ruta base /bff/mobile/v1. Todos los endpoints requieren
 * autenticación JWT con rol CLIENTE_MOVIL.
 *
 * Este controlador delega la lógica de negocio y la orquestación
 * de llamadas al backend en el servicio FinanceMobileService.
 *
 * @author Equipo Backend 3 - Duoc UC
 */
@RestController
@RequestMapping("/bff/mobile/v1")
public class FinanceMobileController {

    @Autowired
    private FinanceMobileService financeMobileService;

    /**
     * Obtiene el resumen financiero de una cuenta, optimizado para móvil.
     *
     * Retorna los datos básicos de la cuenta (nombre, saldo, tipo) junto
     * con los últimos 5 movimientos ordenados por fecha descendente y
     * un mensaje de saludo personalizado.
     *
     * @param id identificador de la cuenta a consultar en el backend
     * @return ResumenMobileDTO con la información agregada y simplificada
     */
    @GetMapping("/cuentas/{id}")
    public ResponseEntity<ResumenMobileDTO> obtenerResumenMobile(@PathVariable Long id) {
        ResumenMobileDTO respuesta = financeMobileService.obtenerResumenMobile(id);
        return ResponseEntity.ok(respuesta);
    }
}