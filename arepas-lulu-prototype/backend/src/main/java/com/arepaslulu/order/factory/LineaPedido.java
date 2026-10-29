package com.arepaslulu.order.factory;

import java.math.BigDecimal;

public record LineaPedido(
        Long productoId,
        String productoNombre,
        BigDecimal precioUnitario,
        int cantidad,
        String observacion
) {
}
