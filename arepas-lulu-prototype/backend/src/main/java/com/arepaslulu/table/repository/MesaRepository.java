package com.arepaslulu.table.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MesaRepository {

    private final JdbcTemplate jdbc;

    public MesaRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<MesaRow> listar() {
        return jdbc.query("SELECT numero,capacidad,estado,referencia,updated_at FROM mesas ORDER BY numero",
                (rs, rowNum) -> new MesaRow(rs.getInt("numero"), rs.getInt("capacidad"), rs.getString("estado"),
                        rs.getString("referencia"), toInstant(rs.getTimestamp("updated_at"))));
    }

    public Optional<MesaRow> buscar(Integer numero) {
        return jdbc.query("SELECT numero,capacidad,estado,referencia,updated_at FROM mesas WHERE numero=?",
                (rs, rowNum) -> new MesaRow(rs.getInt("numero"), rs.getInt("capacidad"), rs.getString("estado"),
                        rs.getString("referencia"), toInstant(rs.getTimestamp("updated_at"))), numero).stream().findFirst();
    }

    public void crear(Integer numero, int capacidad) {
        jdbc.update("INSERT INTO mesas(numero,capacidad,estado,updated_at) VALUES (?,?,'DISPONIBLE',CURRENT_TIMESTAMP)", numero, capacidad);
    }

    public void actualizar(Integer numero, String estado, String referencia) {
        jdbc.update("UPDATE mesas SET estado=?, referencia=?, updated_at=CURRENT_TIMESTAMP WHERE numero=?", estado, referencia, numero);
    }

    private Instant toInstant(Timestamp timestamp) { return timestamp == null ? null : timestamp.toInstant(); }

    public record MesaRow(Integer numero, Integer capacidad, String estado, String referencia, Instant updatedAt) {}
}
