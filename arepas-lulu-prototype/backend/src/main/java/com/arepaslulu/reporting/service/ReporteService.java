package com.arepaslulu.reporting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.billing.repository.BillingRepository;
import com.arepaslulu.billing.repository.BillingRepository.CajaRow;
import com.arepaslulu.billing.repository.BillingRepository.FacturaRow;

@Service
public class ReporteService {

    private final BillingRepository billingRepository;

    public ReporteService(BillingRepository billingRepository) { this.billingRepository = billingRepository; }

    @Transactional(readOnly = true)
    public ResumenCaja resumen(LocalDate desde, LocalDate hasta) {
        Rango rango = rango(desde, hasta);
        CajaRow caja = billingRepository.resumen(rango.desde(), rango.hasta());
        long pendientes = billingRepository.pendientesDian(rango.desde(), rango.hasta());
        List<FacturaRow> facturas = billingRepository.facturas(rango.desde(), rango.hasta());
        return new ResumenCaja(desde, hasta, caja.transacciones(), caja.total(), caja.efectivo(), caja.tarjeta(), caja.transferencia(), pendientes, facturas);
    }

    @Transactional
    public ReporteDian reportarDian(LocalDate fecha) {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        Rango rango = rango(dia, dia);
        int reportadas = billingRepository.reportarDian(rango.desde(), rango.hasta());
        return new ReporteDian(dia, reportadas, billingRepository.pendientesDian(rango.desde(), rango.hasta()));
    }

    private Rango rango(LocalDate desde, LocalDate hasta) {
        LocalDate start = desde == null ? LocalDate.now() : desde;
        LocalDate end = hasta == null ? start : hasta;
        return new Rango(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private record Rango(LocalDateTime desde, LocalDateTime hasta) {}
    public record ResumenCaja(LocalDate desde, LocalDate hasta, long transacciones, java.math.BigDecimal total,
            long efectivo, long tarjeta, long transferencia, long pendientesDian, List<FacturaRow> facturas) {}
    public record ReporteDian(LocalDate fecha, int reportadas, long pendientes) {}
}
