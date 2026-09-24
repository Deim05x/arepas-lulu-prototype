package com.arepaslulu.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductoResponse(
        Long id,
        String nombre,
        String categoria,
        String descripcion,
        BigDecimal precio,
        boolean activo,
        boolean disponible,
        Instant createdAt,
        Instant updatedAt
) {
}
