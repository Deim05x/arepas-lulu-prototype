package com.arepaslulu.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.domain.TipoServicio;

public record PedidoResponse(
        Long id,
        Integer mesaNumero,
        TipoServicio tipoServicio,
        EstadoPedido estado,
        BigDecimal total,
        Instant createdAt,
        List<PedidoItemResponse> items
) {
}
