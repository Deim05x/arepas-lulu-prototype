package com.arepaslulu.delivery.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arepaslulu.delivery.repository.DeliveryRepository.ClienteRow;
import com.arepaslulu.delivery.repository.DeliveryRepository.DomicilioRow;
import com.arepaslulu.delivery.service.DeliveryService;
import com.arepaslulu.delivery.service.DeliveryService.ResultadoDomicilio;
import com.arepaslulu.order.dto.PedidoItemRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api")
public class DeliveryController {

    private final DeliveryService service;

    public DeliveryController(DeliveryService service) { this.service = service; }

    @GetMapping("/clientes")
    public List<ClienteRow> clientes() { return service.clientes(); }

    @PostMapping("/clientes")
    public ResponseEntity<ClienteRow> cliente(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearCliente(request.nombre(), request.telefono(), request.direccion()));
    }

    @GetMapping("/domicilios")
    public List<DomicilioRow> domicilios() { return service.domicilios(); }

    @PostMapping("/domicilios")
    public ResponseEntity<ResultadoDomicilio> domicilio(@Valid @RequestBody DomicilioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearDomicilio(request.clienteId(), request.direccion(), request.items()));
    }

    @PatchMapping("/domicilios/{id}/estado")
    public DomicilioRow estado(@PathVariable Long id, @RequestBody DomicilioEstadoRequest request) {
        return service.cambiarEstado(id, request.estado(), request.repartidor());
    }

    public record ClienteRequest(@NotBlank String nombre, @NotBlank String telefono, String direccion) {}
    public record DomicilioRequest(@NotNull Long clienteId, String direccion, @NotEmpty List<@Valid PedidoItemRequest> items) {}
    public record DomicilioEstadoRequest(String estado, String repartidor) {}
}
