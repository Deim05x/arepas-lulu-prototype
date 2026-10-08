package com.arepaslulu.billing.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BillingRepository {

    private final JdbcTemplate jdbc;

    public BillingRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<PagoRow> pagoPorPedido(Long pedidoId) {
        return jdbc.query("SELECT id,pedido_id,medio,monto,cambio,created_at FROM pagos WHERE pedido_id=?",
                (rs, rowNum) -> new PagoRow(rs.getLong("id"), rs.getLong("pedido_id"), rs.getString("medio"),
                        rs.getBigDecimal("monto"), rs.getBigDecimal("cambio"), toInstant(rs.getTimestamp("created_at"))), pedidoId).stream().findFirst();
    }

    public PagoRow crearPago(Long pedidoId, String medio, BigDecimal monto, BigDecimal cambio) {
        jdbc.update("INSERT INTO pagos(pedido_id,medio,monto,cambio,created_at) VALUES (?,?,?,?,CURRENT_TIMESTAMP)", pedidoId, medio, monto, cambio);
        return pagoPorPedido(pedidoId).orElseThrow();
    }

    public Optional<FacturaRow> facturaPorPedido(Long pedidoId) {
        return jdbc.query("SELECT id,pedido_id,pago_id,numero,total,reportada_dian,created_at,reported_at FROM facturas WHERE pedido_id=?",
                (rs, rowNum) -> facturaRow(rs), pedidoId).stream().findFirst();
    }

    public FacturaRow crearFactura(Long pedidoId, Long pagoId, String numero, BigDecimal total) {
        jdbc.update("INSERT INTO facturas(pedido_id,pago_id,numero,total,reportada_dian,created_at) VALUES (?,?,?,?,FALSE,CURRENT_TIMESTAMP)", pedidoId, pagoId, numero, total);
        return facturaPorPedido(pedidoId).orElseThrow();
    }

    public CajaRow resumen(LocalDateTime desde, LocalDateTime hasta) {
        return jdbc.queryForObject("SELECT COUNT(*) transacciones, COALESCE(SUM(monto),0) total, " +
                        "SUM(CASE WHEN medio='EFECTIVO' THEN 1 ELSE 0 END) efectivo, " +
                        "SUM(CASE WHEN medio='TARJETA' THEN 1 ELSE 0 END) tarjeta, " +
                        "SUM(CASE WHEN medio='TRANSFERENCIA' THEN 1 ELSE 0 END) transferencia " +
                        "FROM pagos WHERE created_at>=? AND created_at<?",
                (rs, rowNum) -> new CajaRow(rs.getLong("transacciones"), rs.getBigDecimal("total"), rs.getLong("efectivo"), rs.getLong("tarjeta"), rs.getLong("transferencia")), desde, hasta);
    }

    public long pendientesDian(LocalDateTime desde, LocalDateTime hasta) {
        Long value = jdbc.queryForObject("SELECT COUNT(*) FROM facturas WHERE created_at>=? AND created_at<? AND reportada_dian=FALSE", Long.class, desde, hasta);
        return value == null ? 0 : value;
    }

    public int reportarDian(LocalDateTime desde, LocalDateTime hasta) {
        return jdbc.update("UPDATE facturas SET reportada_dian=TRUE, reported_at=CURRENT_TIMESTAMP WHERE created_at>=? AND created_at<? AND reportada_dian=FALSE", desde, hasta);
    }

    public List<FacturaRow> facturas(LocalDateTime desde, LocalDateTime hasta) {
        return jdbc.query("SELECT id,pedido_id,pago_id,numero,total,reportada_dian,created_at,reported_at FROM facturas WHERE created_at>=? AND created_at<? ORDER BY created_at DESC",
                (rs, rowNum) -> facturaRow(rs), desde, hasta);
    }

    private FacturaRow facturaRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new FacturaRow(rs.getLong("id"), rs.getLong("pedido_id"), rs.getLong("pago_id"), rs.getString("numero"),
                rs.getBigDecimal("total"), rs.getBoolean("reportada_dian"), toInstant(rs.getTimestamp("created_at")), toInstant(rs.getTimestamp("reported_at")));
    }

    private Instant toInstant(Timestamp value) { return value == null ? null : value.toInstant(); }

    public record PagoRow(Long id, Long pedidoId, String medio, BigDecimal monto, BigDecimal cambio, Instant createdAt) {}
    public record FacturaRow(Long id, Long pedidoId, Long pagoId, String numero, BigDecimal total, boolean reportadaDian, Instant createdAt, Instant reportedAt) {}
    public record CajaRow(long transacciones, BigDecimal total, long efectivo, long tarjeta, long transferencia) {}
}
