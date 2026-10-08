package com.arepaslulu.inventory.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryRepository {

    private final JdbcTemplate jdbc;

    public InventoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<IngredienteRow> listarIngredientes() {
        return jdbc.query("SELECT id,nombre,unidad,stock_actual,stock_minimo,updated_at FROM ingredientes ORDER BY nombre",
                (rs, rowNum) -> new IngredienteRow(rs.getLong("id"), rs.getString("nombre"), rs.getString("unidad"),
                        rs.getBigDecimal("stock_actual"), rs.getBigDecimal("stock_minimo"), toInstant(rs.getTimestamp("updated_at"))));
    }

    public Optional<IngredienteRow> buscarIngrediente(Long id) {
        List<IngredienteRow> rows = jdbc.query("SELECT id,nombre,unidad,stock_actual,stock_minimo,updated_at FROM ingredientes WHERE id=?",
                (rs, rowNum) -> new IngredienteRow(rs.getLong("id"), rs.getString("nombre"), rs.getString("unidad"),
                        rs.getBigDecimal("stock_actual"), rs.getBigDecimal("stock_minimo"), toInstant(rs.getTimestamp("updated_at"))), id);
        return rows.stream().findFirst();
    }

    public IngredienteRow crear(String nombre, String unidad, BigDecimal stockActual, BigDecimal stockMinimo) {
        jdbc.update("INSERT INTO ingredientes(nombre,unidad,stock_actual,stock_minimo,updated_at) VALUES (?,?,?,?,CURRENT_TIMESTAMP)",
                nombre, unidad, stockActual, stockMinimo);
        return jdbc.queryForObject("SELECT id,nombre,unidad,stock_actual,stock_minimo,updated_at FROM ingredientes WHERE nombre=?",
                (rs, rowNum) -> new IngredienteRow(rs.getLong("id"), rs.getString("nombre"), rs.getString("unidad"),
                        rs.getBigDecimal("stock_actual"), rs.getBigDecimal("stock_minimo"), toInstant(rs.getTimestamp("updated_at"))), nombre);
    }

    public void establecerStock(Long id, BigDecimal nuevoStock) {
        jdbc.update("UPDATE ingredientes SET stock_actual=?, updated_at=CURRENT_TIMESTAMP WHERE id=?", nuevoStock, id);
    }

    public List<RecetaRow> receta(Long productoId) {
        return jdbc.query("SELECT r.ingrediente_id,i.nombre,i.unidad,r.cantidad FROM recetas r JOIN ingredientes i ON i.id=r.ingrediente_id WHERE r.producto_id=? ORDER BY i.nombre",
                (rs, rowNum) -> new RecetaRow(rs.getLong("ingrediente_id"), rs.getString("nombre"), rs.getString("unidad"), rs.getBigDecimal("cantidad")), productoId);
    }

    public void reemplazarReceta(Long productoId, List<RecetaInput> items) {
        jdbc.update("DELETE FROM recetas WHERE producto_id=?", productoId);
        for (RecetaInput item : items) {
            jdbc.update("INSERT INTO recetas(producto_id,ingrediente_id,cantidad) VALUES (?,?,?)",
                    productoId, item.ingredienteId(), item.cantidad());
        }
    }

    private Instant toInstant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    public record IngredienteRow(Long id, String nombre, String unidad, BigDecimal stockActual, BigDecimal stockMinimo, Instant updatedAt) {}
    public record RecetaRow(Long ingredienteId, String ingredienteNombre, String unidad, BigDecimal cantidad) {}
    public record RecetaInput(Long ingredienteId, BigDecimal cantidad) {}
}
