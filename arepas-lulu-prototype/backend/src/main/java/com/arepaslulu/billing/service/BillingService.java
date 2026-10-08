package com.arepaslulu.billing.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.billing.domain.MedioPago;
import com.arepaslulu.billing.repository.BillingRepository;
import com.arepaslulu.billing.repository.BillingRepository.FacturaRow;
import com.arepaslulu.billing.repository.BillingRepository.PagoRow;
import com.arepaslulu.common.exception.BusinessRuleException;
import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.domain.Pedido;
import com.arepaslulu.order.dto.PedidoResponse;
import com.arepaslulu.order.mapper.PedidoMapper;
import com.arepaslulu.order.repository.PedidoRepository;
import com.arepaslulu.order.service.PedidoService;
import com.arepaslulu.table.service.MesaService;

@Service
public class BillingService {

    private final BillingRepository billingRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;
    private final PedidoMapper mapper;
    private final MesaService mesaService;

    public BillingService(BillingRepository billingRepository, PedidoRepository pedidoRepository, PedidoService pedidoService,
            PedidoMapper mapper, MesaService mesaService) {
        this.billingRepository = billingRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoService = pedidoService;
        this.mapper = mapper;
        this.mesaService = mesaService;
    }

    @Transactional
    public ResultadoPago pagar(Long pedidoId, MedioPago medio, BigDecimal montoRecibido) {
        Pedido pedido = pedidoService.obtenerEntidad(pedidoId);
        if (billingRepository.pagoPorPedido(pedidoId).isPresent()) {
            throw new BusinessRuleException("El pedido ya tiene un pago registrado.");
        }
        if (pedido.getEstado() != EstadoPedido.SERVIDO && pedido.getEstado() != EstadoPedido.LISTO) {
            throw new BusinessRuleException("El pedido debe estar listo o servido antes de pagar. Estado actual: " + pedido.getEstado());
        }

        BigDecimal total = pedido.getTotal();
        BigDecimal recibido = montoRecibido == null ? total : montoRecibido;
        BigDecimal cambio = BigDecimal.ZERO;
        if (medio == MedioPago.EFECTIVO) {
            if (recibido.compareTo(total) < 0) throw new BusinessRuleException("El efectivo recibido es menor al total del pedido.");
            cambio = recibido.subtract(total);
        } else {
            recibido = total;
        }

        PagoRow pago = billingRepository.crearPago(pedidoId, medio.name(), total, cambio);
        FacturaRow factura = billingRepository.crearFactura(pedidoId, pago.id(), "AL-" + String.format("%08d", pedidoId), total);
        pedido.cambiarEstado(EstadoPedido.PAGADO);
        pedidoRepository.save(pedido);
        if (pedido.esDeMesa()) mesaService.liberarPorNumero(pedido.getMesaNumero());
        return new ResultadoPago(pago, factura, mapper.toResponse(pedido));
    }

    @Transactional(readOnly = true)
    public FacturaRow factura(Long pedidoId) {
        return billingRepository.facturaPorPedido(pedidoId)
                .orElseThrow(() -> new BusinessRuleException("El pedido todavía no tiene factura."));
    }

    public record ResultadoPago(PagoRow pago, FacturaRow factura, PedidoResponse pedido) {}
}
