package com.arepaslulu.inventory.web;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arepaslulu.inventory.repository.InventoryRepository.IngredienteRow;
import com.arepaslulu.inventory.repository.InventoryRepository.RecetaRow;
import com.arepaslulu.inventory.service.InventoryService;
import com.arepaslulu.inventory.service.InventoryService.RecetaItem;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/inventario")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @GetMapping("/ingredientes")
    public List<IngredienteRow> ingredientes() { return service.listarIngredientes(); }

    @PostMapping("/ingredientes")
    public ResponseEntity<IngredienteRow> crear(@Valid @RequestBody IngredienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request.nombre(), request.unidad(), request.stockActual(), request.stockMinimo()));
    }

    @PatchMapping("/ingredientes/{id}/stock")
    public IngredienteRow ajustar(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return service.ajustarStock(id, request.delta());
    }

    @GetMapping("/recetas/{productoId}")
    public List<RecetaRow> receta(@PathVariable Long productoId) { return service.receta(productoId); }

    @PutMapping("/recetas/{productoId}")
    public List<RecetaRow> receta(@PathVariable Long productoId, @Valid @RequestBody List<RecetaItemRequest> request) {
        return service.definirReceta(productoId, request.stream().map(i -> new RecetaItem(i.ingredienteId(), i.cantidad())).toList());
    }

    public record IngredienteRequest(@NotBlank String nombre, @NotBlank String unidad,
            @NotNull @DecimalMin("0.0") BigDecimal stockActual, @DecimalMin("0.0") BigDecimal stockMinimo) {}
    public record StockRequest(@NotNull BigDecimal delta) {}
    public record RecetaItemRequest(@NotNull Long ingredienteId, @NotNull @DecimalMin(value = "0.001") BigDecimal cantidad) {}
}
