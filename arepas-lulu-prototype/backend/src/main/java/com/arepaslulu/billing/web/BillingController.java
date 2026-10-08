package com.arepaslulu.billing.web;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arepaslulu.billing.domain.MedioPago;
import com.arepaslulu.billing.repository.BillingRepository.FacturaRow;
import com.arepaslulu.billing.service.BillingService;
import com.arepaslulu.billing.service.BillingService.ResultadoPago;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api")
public class BillingController {

    private final BillingService service;

    public BillingController(BillingService service) { this.service = service; }

    @PostMapping("/pagos")
    public ResponseEntity<ResultadoPago> pagar(@Valid @RequestBody PagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.pagar(request.pedidoId(), request.medio(), request.montoRecibido()));
    }

    @GetMapping("/facturas/pedido/{pedidoId}")
    public FacturaRow factura(@PathVariable Long pedidoId) { return service.factura(pedidoId); }

    public record PagoRequest(@NotNull Long pedidoId, @NotNull MedioPago medio, BigDecimal montoRecibido) {}
}
