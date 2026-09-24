package com.arepaslulu.catalog.mapper;

import org.springframework.stereotype.Component;

import com.arepaslulu.catalog.domain.Producto;
import com.arepaslulu.catalog.dto.ProductoResponse;

@Component
public class ProductoMapper {
    public ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getCategoria(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.isActivo(),
                producto.isDisponible(),
                producto.getCreatedAt(),
                producto.getUpdatedAt()
        );
    }
}
