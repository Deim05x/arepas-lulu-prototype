package com.arepaslulu.delivery.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DeliveryRepository {

    private final JdbcTemplate jdbc;

    public DeliveryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<ClienteRow> clientes() {
        return jdbc.query("SELECT id,nombre,telefono,direccion,created_at FROM clientes ORDER BY nombre",
                (rs, rowNum) -> new ClienteRow(rs.getLong("id"), rs.getString("nombre"), rs.getString("telefono"), rs.getString("direccion"), toInstant(rs.getTimestamp("created_at"))));
    }

    public Optional<ClienteRow> cliente(Long id) {
        return jdbc.query("SELECT id,nombre,telefono,direccion,created_at FROM clientes WHERE id=?",
                (rs, rowNum) -> new ClienteRow(rs.getLong("id"), rs.getString("nombre"), rs.getString("telefono"), rs.getString("direccion"), toInstant(rs.getTimestamp("created_at"))), id).stream().findFirst();
    }

    public ClienteRow crearCliente(String nombre, String telefono, String direccion) {
        jdbc.update("INSERT INTO clientes(nombre,telefono,direccion,created_at) VALUES (?,?,?,CURRENT_TIMESTAMP)", nombre, telefono, direccion);
        return jdbc.queryForObject("SELECT id,nombre,telefono,direccion,created_at FROM clientes WHERE telefono=? ORDER BY id DESC LIMIT 1",
                (rs, rowNum) -> new ClienteRow(rs.getLong("id"), rs.getString("nombre"), rs.getString("telefono"), rs.getString("direccion"), toInstant(rs.getTimestamp("created_at"))), telefono);
    }

    public DomicilioRow crearDomicilio(Long pedidoId, Long clienteId, String direccion) {
        jdbc.update("INSERT INTO domicilios(pedido_id,cliente_id,direccion,estado,created_at) VALUES (?,?,?,'PENDIENTE',CURRENT_TIMESTAMP)", pedidoId, clienteId, direccion);
        return domicilioPorPedido(pedidoId).orElseThrow();
    }

    public Optional<DomicilioRow> domicilio(Long id) {
        return consultaDomicilios("WHERE d.id=?", id).stream().findFirst();
    }

    public Optional<DomicilioRow> domicilioPorPedido(Long pedidoId) {
        return consultaDomicilios("WHERE d.pedido_id=?", pedidoId).stream().findFirst();
    }

    public List<DomicilioRow> domicilios() { return consultaDomicilios("ORDER BY d.created_at DESC"); }

    public void actualizarEstado(Long id, String estado, String repartidor) {
        if ("ENTREGADO".equals(estado)) {
            jdbc.update("UPDATE domicilios SET estado=?, repartidor=?, delivered_at=CURRENT_TIMESTAMP WHERE id=?", estado, repartidor, id);
        } else {
            jdbc.update("UPDATE domicilios SET estado=?, repartidor=? WHERE id=?", estado, repartidor, id);
        }
    }

    private List<DomicilioRow> consultaDomicilios(String suffix, Object... args) {
        String sql = "SELECT d.id,d.pedido_id,d.cliente_id,c.nombre cliente_nombre,d.direccion,d.estado,d.repartidor,d.created_at,d.delivered_at " +
                "FROM domicilios d JOIN clientes c ON c.id=d.cliente_id " + suffix;
        return jdbc.query(sql, (rs, rowNum) -> new DomicilioRow(rs.getLong("id"), rs.getLong("pedido_id"), rs.getLong("cliente_id"),
                rs.getString("cliente_nombre"), rs.getString("direccion"), rs.getString("estado"), rs.getString("repartidor"),
                toInstant(rs.getTimestamp("created_at")), toInstant(rs.getTimestamp("delivered_at"))), args);
    }

    private Instant toInstant(Timestamp value) { return value == null ? null : value.toInstant(); }

    public record ClienteRow(Long id, String nombre, String telefono, String direccion, Instant createdAt) {}
    public record DomicilioRow(Long id, Long pedidoId, Long clienteId, String clienteNombre, String direccion,
            String estado, String repartidor, Instant createdAt, Instant deliveredAt) {}
}
