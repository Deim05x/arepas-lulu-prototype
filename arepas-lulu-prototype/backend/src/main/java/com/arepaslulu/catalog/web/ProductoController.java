package com.arepaslulu.catalog.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.arepaslulu.catalog.dto.DisponibilidadRequest;
import com.arepaslulu.catalog.dto.ProductoRequest;
import com.arepaslulu.catalog.dto.ProductoResponse;
import com.arepaslulu.catalog.event.CatalogEventPublisher;
import com.arepaslulu.catalog.service.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;
    private final CatalogEventPublisher events;

    public ProductoController(ProductoService service, CatalogEventPublisher events) {
        this.service = service;
        this.events = events;
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return events.subscribe();
    }

    @GetMapping
    public List<ProductoResponse> listar(
            @RequestParam(defaultValue = "false") boolean soloDisponibles) {
        return service.listar(soloDisponibles);
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/disponibilidad")
    public ProductoResponse cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestBody DisponibilidadRequest request) {
        return service.cambiarDisponibilidad(id, request.disponible());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
