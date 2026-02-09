package cl.duoc.finance_bff_mobile.service;

import cl.duoc.finance_bff_mobile.model.ResumenMobileDTO;

/**
 * Interfaz del servicio BFF Mobile.
 *
 * Define las operaciones de negocio que el BFF expone hacia
 * el controlador. La implementación (FinanceMobileServiceImpl)
 * se encarga de orquestar las llamadas al backend y transformar
 * las respuestas para consumo móvil.
 *
 * @author Equipo Backend 3 - Duoc UC
 */
public interface FinanceMobileService {

    /**
     * Obtiene un resumen financiero optimizado para móvil.
     *
     * Orquesta llamadas al backend para obtener datos de la cuenta
     * y sus transacciones, retornando solo la información esencial
     * (últimos 5 movimientos, datos básicos de cuenta y saludo).
     *
     * @param id identificador de la cuenta en el backend
     * @return DTO con el resumen simplificado para la app móvil
     */
    ResumenMobileDTO obtenerResumenMobile(Long id);
}
