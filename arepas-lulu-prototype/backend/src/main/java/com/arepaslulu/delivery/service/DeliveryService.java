package com.arepaslulu.delivery.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.common.exception.BusinessRuleException;
import com.arepaslulu.common.exception.ResourceNotFoundException;
import com.arepaslulu.delivery.repository.DeliveryRepository;
import com.arepaslulu.delivery.repository.DeliveryRepository.ClienteRow;
import com.arepaslulu.delivery.repository.DeliveryRepository.DomicilioRow;
import com.arepaslulu.order.domain.TipoServicio;
import com.arepaslulu.order.dto.PedidoItemRequest;
import com.arepaslulu.order.dto.PedidoRequest;
import com.arepaslulu.order.dto.PedidoResponse;
import com.arepaslulu.order.service.PedidoService;

@Service
public class DeliveryService {

    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "DESPACHADO", "ENTREGADO", "CANCELADO");
    private final DeliveryRepository repository;
    private final PedidoService pedidoService;

    public DeliveryService(DeliveryRepository repository, PedidoService pedidoService) {
        this.repository = repository;
        this.pedidoService = pedidoService;
    }

    @Transactional(readOnly = true)
    public List<ClienteRow> clientes() { return repository.clientes(); }

    @Transactional
    public ClienteRow crearCliente(String nombre, String telefono, String direccion) {
        if (nombre == null || nombre.isBlank() || telefono == null || telefono.isBlank()) {
            throw new BusinessRuleException("Nombre y teléfono son obligatorios para el cliente.");
        }
        return repository.crearCliente(nombre.trim(), telefono.trim(), limpiar(direccion));
    }

    @Transactional(readOnly = true)
    public List<DomicilioRow> domicilios() { return repository.domicilios(); }

    @Transactional
    public ResultadoDomicilio crearDomicilio(Long clienteId, String direccion, List<PedidoItemRequest> items) {
        ClienteRow cliente = repository.cliente(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clienteId));
        String destino = direccion == null || direccion.isBlank() ? cliente.direccion() : direccion.trim();
        if (destino == null || destino.isBlank()) throw new BusinessRuleException("El domicilio requiere una dirección de entrega.");
        PedidoResponse pedido = pedidoService.crear(new PedidoRequest(0, TipoServicio.DOMICILIO, items));
        DomicilioRow domicilio = repository.crearDomicilio(pedido.id(), clienteId, destino);
        return new ResultadoDomicilio(pedido, domicilio);
    }

    @Transactional
    public DomicilioRow cambiarEstado(Long id, String estado, String repartidor) {
        DomicilioRow actual = repository.domicilio(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domicilio no encontrado: " + id));
        String normalized = estado == null ? "" : estado.trim().toUpperCase();
        if (!ESTADOS.contains(normalized)) throw new BusinessRuleException("Estado de domicilio no permitido: " + estado);
        repository.actualizarEstado(id, normalized, limpiar(repartidor));
        return repository.domicilio(actual.id()).orElseThrow();
    }

    private String limpiar(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record ResultadoDomicilio(PedidoResponse pedido, DomicilioRow domicilio) {}
}
