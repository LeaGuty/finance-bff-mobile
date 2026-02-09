package cl.duoc.finance_bff_mobile.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.finance_bff_mobile.model.ResumenMobileDTO;
import cl.duoc.finance_bff_mobile.service.FinanceMobileService;

@RestController
@RequestMapping("/bff/mobile/v1")
public class FinanceMobileController {

    @Autowired
    private FinanceMobileService financeMobileService;

    @GetMapping("/cuentas/{id}")
    public ResponseEntity<ResumenMobileDTO> obtenerResumenMobile(@PathVariable Long id) {
        ResumenMobileDTO respuesta = financeMobileService.obtenerResumenMobile(id);
        return ResponseEntity.ok(respuesta);
    }
}