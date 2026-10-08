package com.arepaslulu.order.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.catalog.domain.Producto;
import com.arepaslulu.catalog.repository.ProductoRepository;
import com.arepaslulu.common.exception.BusinessRuleException;
import com.arepaslulu.common.exception.ResourceNotFoundException;
import com.arepaslulu.inventory.service.InventoryService;
import com.arepaslulu.order.domain.Pedido;
import com.arepaslulu.order.domain.TipoServicio;
import com.arepaslulu.order.dto.PedidoRequest;
import com.arepaslulu.order.dto.PedidoResponse;
import com.arepaslulu.order.factory.PedidoFactory;
import com.arepaslulu.order.factory.PedidoFactory.ItemConProducto;
import com.arepaslulu.order.mapper.PedidoMapper;
import com.arepaslulu.order.repository.PedidoRepository;
import com.arepaslulu.table.service.MesaService;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PedidoFactory factory;
    private final PedidoMapper mapper;
    private final InventoryService inventoryService;
    private final MesaService mesaService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            PedidoFactory factory,
            PedidoMapper mapper,
            InventoryService inventoryService,
            MesaService mesaService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.factory = factory;
        this.mapper = mapper;
        this.inventoryService = inventoryService;
        this.mesaService = mesaService;
    }

    @Transactional
    public PedidoResponse crear(PedidoRequest request) {
        TipoServicio tipo = request.tipoServicio() == null ? TipoServicio.MESA : request.tipoServicio();
        Integer mesa = validarContexto(tipo, request.mesaNumero());
        Map<Long, Producto> productos = cargarProductos(request);

        List<ItemConProducto> items = request.items().stream()
                .map(item -> {
                    Producto producto = productos.get(item.productoId());
                    validarOrdenable(producto);
                    return new ItemConProducto(item, producto);
                })
                .toList();

        Map<Long, Integer> cantidades = request.items().stream()
                .collect(Collectors.toMap(
                        item -> item.productoId(),
                        item -> item.cantidad(),
                        Integer::sum,
                        LinkedHashMap::new));
        inventoryService.consumirParaPedido(cantidades);

        Pedido pedido = pedidoRepository.save(factory.crear(mesa, tipo, items));
        if (tipo == TipoServicio.MESA) {
            mesaService.ocuparPorNumero(mesa);
        }
        return mapper.toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id) {
        return mapper.toResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(Integer mesa) {
        List<Pedido> pedidos = mesa == null
                ? pedidoRepository.findAllByOrderByCreatedAtDesc()
                : pedidoRepository.findByMesaNumeroOrderByCreatedAtDesc(mesa);
        return pedidos.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Pedido obtenerEntidad(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));
    }

    private Integer validarContexto(TipoServicio tipo, Integer mesa) {
        if (tipo == TipoServicio.MESA) {
            if (mesa == null || mesa < 1) {
                throw new BusinessRuleException("Los pedidos en mesa requieren un número de mesa válido.");
            }
            return mesa;
        }
        return 0;
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
