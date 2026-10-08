package com.arepaslulu.order.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.domain.Pedido;
import com.arepaslulu.order.domain.TipoServicio;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Override
    @EntityGraph(attributePaths = {"items", "items.producto"})
    java.util.Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = {"items", "items.producto"})
    List<Pedido> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"items", "items.producto"})
    List<Pedido> findByMesaNumeroOrderByCreatedAtDesc(Integer mesaNumero);

    @EntityGraph(attributePaths = {"items", "items.producto"})
    List<Pedido> findByEstadoInOrderByCreatedAtAsc(Collection<EstadoPedido> estados);

    @EntityGraph(attributePaths = {"items", "items.producto"})
    List<Pedido> findByTipoServicioOrderByCreatedAtDesc(TipoServicio tipoServicio);
}
