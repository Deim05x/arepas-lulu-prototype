package com.arepaslulu.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.catalog.domain.Producto;
import com.arepaslulu.catalog.dto.ProductoRequest;
import com.arepaslulu.catalog.dto.ProductoResponse;
import com.arepaslulu.catalog.event.CatalogEventPublisher;
import com.arepaslulu.catalog.mapper.ProductoMapper;
import com.arepaslulu.catalog.repository.ProductoRepository;
import com.arepaslulu.common.exception.BusinessRuleException;
import com.arepaslulu.common.exception.ResourceNotFoundException;

@Service
public class ProductoService {

    private final ProductoRepository repository;
    private final ProductoMapper mapper;
    private final CatalogEventPublisher events;

    public ProductoService(ProductoRepository repository, ProductoMapper mapper, CatalogEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(boolean soloDisponibles) {
        List<Producto> productos = soloDisponibles
                ? repository.findByActivoTrueAndDisponibleTrueOrderByNombreAsc()
                : repository.findAllByOrderByNombreAsc();
        return productos.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return mapper.toResponse(obtenerEntidad(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        String nombre = normalizar(request.nombre());
        validarNombreDuplicado(nombre, null);
        Producto producto = new Producto(
                nombre,
                normalizar(request.categoria()),
                limpiar(request.descripcion()),
                request.precio(),
                request.disponible()
        );
        Producto saved = repository.save(producto);
        events.catalogChanged();
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = obtenerEntidad(id);
        if (!producto.isActivo()) {
            throw new BusinessRuleException("No se puede editar un producto desactivado.");
        }
        String nombre = normalizar(request.nombre());
        validarNombreDuplicado(nombre, id);
        producto.actualizar(
                nombre,
                normalizar(request.categoria()),
                limpiar(request.descripcion()),
                request.precio(),
                request.disponible()
        );
        repository.flush();
        events.catalogChanged();
        return mapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse cambiarDisponibilidad(Long id, boolean disponible) {
        Producto producto = obtenerEntidad(id);
        if (!producto.isActivo()) {
            throw new BusinessRuleException("No se puede cambiar la disponibilidad de un producto desactivado.");
        }
        producto.cambiarDisponibilidad(disponible);
        repository.flush();
        events.catalogChanged();
        return mapper.toResponse(producto);
    }

    @Transactional
    public void desactivar(Long id) {
        Producto producto = obtenerEntidad(id);
        producto.desactivar();
        events.catalogChanged();
    }

    private Producto obtenerEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
    }

    private void validarNombreDuplicado(String nombre, Long idActual) {
        boolean duplicado = idActual == null
                ? repository.existsByNombreIgnoreCase(nombre)
                : repository.existsByNombreIgnoreCaseAndIdNot(nombre, idActual);
        if (duplicado) {
            throw new BusinessRuleException("Ya existe un producto con el nombre '" + nombre + "'.");
        }
    }

    private String normalizar(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private String limpiar(String value) {
        String normalized = normalizar(value);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }
}
