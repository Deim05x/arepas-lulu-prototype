package com.arepaslulu.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.arepaslulu.order.domain.EstadoPedido;

public record PedidoResponse(
        Long id,
        int mesaNumero,
        EstadoPedido estado,
        BigDecimal total,
        Instant createdAt,
        List<PedidoItemResponse> items
) {
}
