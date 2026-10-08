package com.arepaslulu.table.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.common.exception.BusinessRuleException;
import com.arepaslulu.table.repository.MesaRepository;
import com.arepaslulu.table.repository.MesaRepository.MesaRow;

@Service
public class MesaService {

    private static final Set<String> ESTADOS = Set.of("DISPONIBLE", "OCUPADA", "RESERVADA", "INACTIVA");
    private final MesaRepository repository;

    public MesaService(MesaRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<MesaRow> listar() { return repository.listar(); }

    @Transactional
    public MesaRow cambiarEstado(Integer numero, String estado, String referencia) {
        String normalized = normalizarEstado(estado);
        asegurarMesa(numero);
        repository.actualizar(numero, normalized, limpiar(referencia));
        return repository.buscar(numero).orElseThrow();
    }

    @Transactional
    public void ocuparPorNumero(Integer numero) {
        MesaRow mesa = asegurarMesa(numero);
        if ("INACTIVA".equals(mesa.estado())) {
            throw new BusinessRuleException("La mesa " + numero + " está inactiva.");
        }
        repository.actualizar(numero, "OCUPADA", mesa.referencia());
    }

    @Transactional
    public void liberarPorNumero(Integer numero) {
        if (numero == null || numero <= 0) return;
        MesaRow mesa = asegurarMesa(numero);
        repository.actualizar(numero, "DISPONIBLE", null);
    }

    private MesaRow asegurarMesa(Integer numero) {
        if (numero == null || numero <= 0) throw new BusinessRuleException("Número de mesa inválido.");
        return repository.buscar(numero).orElseGet(() -> {
            repository.crear(numero, 4);
            return repository.buscar(numero).orElseThrow();
        });
    }

    private String normalizarEstado(String estado) {
        String value = estado == null ? "" : estado.trim().toUpperCase();
        if (!ESTADOS.contains(value)) throw new BusinessRuleException("Estado de mesa no permitido: " + estado);
        return value;
    }

    private String limpiar(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
