package com.arepaslulu.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PedidoItemRequest(
        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        int cantidad,

        @Size(max = 300, message = "La observación no puede superar 300 caracteres")
        String observacion
) {
}
