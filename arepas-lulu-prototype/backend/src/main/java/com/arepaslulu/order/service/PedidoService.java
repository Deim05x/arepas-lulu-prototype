package com.arepaslulu.order.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.catalog.domain.Producto;
import com.arepaslulu.catalog.repository.ProductoRepository;
import com.arepaslulu.common.exception.BusinessRuleException;
import com.arepaslulu.common.exception.ResourceNotFoundException;
import com.arepaslulu.order.domain.Pedido;
import com.arepaslulu.order.dto.PedidoRequest;
import com.arepaslulu.order.dto.PedidoResponse;
import com.arepaslulu.order.factory.PedidoFactory;
import com.arepaslulu.order.factory.PedidoFactory.ItemConProducto;
import com.arepaslulu.order.mapper.PedidoMapper;
import com.arepaslulu.order.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PedidoFactory factory;
    private final PedidoMapper mapper;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            PedidoFactory factory,
            PedidoMapper mapper) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.factory = factory;
        this.mapper = mapper;
    }

    @Transactional
    public PedidoResponse crear(PedidoRequest request) {
        Map<Long, Producto> productos = cargarProductos(request);

        List<ItemConProducto> items = request.items().stream()
                .map(item -> {
                    Producto producto = productos.get(item.productoId());
                    validarOrdenable(producto);
                    return new ItemConProducto(item, producto);
                })
                .toList();

        Pedido pedido = factory.crear(request.mesaNumero(), items);
        return mapper.toResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id) {
        return mapper.toResponse(pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id)));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(Integer mesa) {
        List<Pedido> pedidos = mesa == null
                ? pedidoRepository.findAllByOrderByCreatedAtDesc()
                : pedidoRepository.findByMesaNumeroOrderByCreatedAtDesc(mesa);
        return pedidos.stream().map(mapper::toResponse).toList();
    }

    private Map<Long, Producto> cargarProductos(PedidoRequest request) {
        List<Long> ids = request.items().stream()
                .map(item -> item.productoId())
                .distinct()
                .toList();

        Map<Long, Producto> productos = new LinkedHashMap<>();
        productoRepository.findAllById(ids).forEach(p -> productos.put(p.getId(), p));

        if (productos.size() != ids.size()) {
            List<Long> faltantes = ids.stream().filter(id -> !productos.containsKey(id)).toList();
            throw new ResourceNotFoundException("Productos no encontrados: " + faltantes);
        }
        return productos;
    }

    private void validarOrdenable(Producto producto) {
        if (!producto.isActivo()) {
            throw new BusinessRuleException("El producto '" + producto.getNombre() + "' está desactivado.");
        }
        if (!producto.isDisponible()) {
            throw new BusinessRuleException("El producto '" + producto.getNombre() + "' no está disponible.");
        }
    }
}
