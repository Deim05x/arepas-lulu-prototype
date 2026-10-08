package com.arepaslulu.kitchen.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.common.exception.BusinessRuleException;
import com.arepaslulu.common.exception.ResourceNotFoundException;
import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.domain.Pedido;
import com.arepaslulu.order.dto.PedidoResponse;
import com.arepaslulu.order.mapper.PedidoMapper;
import com.arepaslulu.order.repository.PedidoRepository;

@Service
public class CocinaService {

    private static final Set<EstadoPedido> VISIBLES = EnumSet.of(EstadoPedido.ENVIADO_COCINA, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO, EstadoPedido.SERVIDO);
    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES = Map.of(
            EstadoPedido.ENVIADO_COCINA, EnumSet.of(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO),
            EstadoPedido.EN_PREPARACION, EnumSet.of(EstadoPedido.LISTO, EstadoPedido.CANCELADO),
            EstadoPedido.LISTO, EnumSet.of(EstadoPedido.SERVIDO),
            EstadoPedido.SERVIDO, EnumSet.noneOf(EstadoPedido.class));

    private final PedidoRepository repository;
    private final PedidoMapper mapper;

    public CocinaService(PedidoRepository repository, PedidoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> cola() {
        return repository.findByEstadoInOrderByCreatedAtAsc(VISIBLES).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = repository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + pedidoId));
        Set<EstadoPedido> permitidos = TRANSICIONES.getOrDefault(pedido.getEstado(), Set.of());
        if (!permitidos.contains(nuevoEstado)) {
            throw new BusinessRuleException("No se puede pasar el pedido de " + pedido.getEstado() + " a " + nuevoEstado + ".");
        }
        pedido.cambiarEstado(nuevoEstado);
        return mapper.toResponse(repository.save(pedido));
    }
}
