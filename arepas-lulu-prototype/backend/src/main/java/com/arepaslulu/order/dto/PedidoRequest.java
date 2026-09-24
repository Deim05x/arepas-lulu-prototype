package com.arepaslulu.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record PedidoRequest(
        @Min(value = 1, message = "La mesa debe ser mayor que cero")
        int mesaNumero,

        @NotEmpty(message = "El pedido debe tener al menos un producto")
        List<@Valid PedidoItemRequest> items
) {
}
