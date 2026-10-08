package com.arepaslulu.order.dto;

import java.util.List;

import com.arepaslulu.order.domain.TipoServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record PedidoRequest(
        Integer mesaNumero,
        TipoServicio tipoServicio,
        @NotEmpty(message = "El pedido debe tener al menos un producto")
        List<@Valid PedidoItemRequest> items
) {
    public PedidoRequest(int mesaNumero, List<PedidoItemRequest> items) {
        this(mesaNumero, TipoServicio.MESA, items);
    }
}
