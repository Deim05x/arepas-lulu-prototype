package com.arepaslulu.inventory.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.common.exception.BusinessRuleException;
import com.arepaslulu.common.exception.ResourceNotFoundException;
import com.arepaslulu.inventory.repository.InventoryRepository;
import com.arepaslulu.inventory.repository.InventoryRepository.IngredienteRow;
import com.arepaslulu.inventory.repository.InventoryRepository.RecetaInput;
import com.arepaslulu.inventory.repository.InventoryRepository.RecetaRow;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<IngredienteRow> listarIngredientes() {
        return repository.listarIngredientes();
    }

    @Transactional
    public IngredienteRow crear(String nombre, String unidad, BigDecimal stockActual, BigDecimal stockMinimo) {
        if (nombre == null || nombre.isBlank()) throw new BusinessRuleException("El ingrediente requiere nombre.");
        if (unidad == null || unidad.isBlank()) throw new BusinessRuleException("El ingrediente requiere unidad.");
        if (stockActual == null || stockActual.signum() < 0) throw new BusinessRuleException("El stock no puede ser negativo.");
        BigDecimal minimo = stockMinimo == null ? BigDecimal.ZERO : stockMinimo;
        if (minimo.signum() < 0) throw new BusinessRuleException("El stock mínimo no puede ser negativo.");
        return repository.crear(nombre.trim(), unidad.trim(), stockActual, minimo);
    }

    @Transactional
    public IngredienteRow ajustarStock(Long id, BigDecimal delta) {
        IngredienteRow actual = obtenerIngrediente(id);
        BigDecimal nuevo = actual.stockActual().add(delta == null ? BigDecimal.ZERO : delta);
        if (nuevo.signum() < 0) throw new BusinessRuleException("El ajuste dejaría el stock en negativo.");
        repository.establecerStock(id, nuevo);
        return obtenerIngrediente(id);
    }

    @Transactional(readOnly = true)
    public List<RecetaRow> receta(Long productoId) {
        return repository.receta(productoId);
    }

    @Transactional
    public List<RecetaRow> definirReceta(Long productoId, List<RecetaItem> items) {
        List<RecetaInput> data = items.stream().map(item -> {
            obtenerIngrediente(item.ingredienteId());
            if (item.cantidad() == null || item.cantidad().signum() <= 0) {
                throw new BusinessRuleException("La cantidad de una receta debe ser mayor que cero.");
            }
            return new RecetaInput(item.ingredienteId(), item.cantidad());
        }).toList();
        repository.reemplazarReceta(productoId, data);
        return repository.receta(productoId);
    }

    @Transactional
    public void consumirParaPedido(Map<Long, Integer> productos) {
        if (productos == null || productos.isEmpty()) return;
        Map<Long, BigDecimal> requeridos = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> producto : productos.entrySet()) {
            for (RecetaRow linea : repository.receta(producto.getKey())) {
                BigDecimal requerido = linea.cantidad().multiply(BigDecimal.valueOf(producto.getValue()));
                requeridos.merge(linea.ingredienteId(), requerido, BigDecimal::add);
            }
        }

        Map<Long, IngredienteRow> ingredientes = new LinkedHashMap<>();
        for (Map.Entry<Long, BigDecimal> requerido : requeridos.entrySet()) {
            IngredienteRow ingrediente = obtenerIngrediente(requerido.getKey());
            if (ingrediente.stockActual().compareTo(requerido.getValue()) < 0) {
                throw new BusinessRuleException("Stock insuficiente de '" + ingrediente.nombre() + "'. Disponible: "
                        + ingrediente.stockActual() + " " + ingrediente.unidad() + ", requerido: " + requerido.getValue());
            }
            ingredientes.put(ingrediente.id(), ingrediente);
        }

        for (Map.Entry<Long, BigDecimal> requerido : requeridos.entrySet()) {
            IngredienteRow ingrediente = ingredientes.get(requerido.getKey());
            repository.establecerStock(ingrediente.id(), ingrediente.stockActual().subtract(requerido.getValue()));
        }
    }

    private IngredienteRow obtenerIngrediente(Long id) {
        return repository.buscarIngrediente(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente no encontrado: " + id));
    }

    public record RecetaItem(Long ingredienteId, BigDecimal cantidad) {}
}
