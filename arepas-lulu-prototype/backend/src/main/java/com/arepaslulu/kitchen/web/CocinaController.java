package com.arepaslulu.kitchen.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arepaslulu.kitchen.service.CocinaService;
import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.dto.PedidoResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/cocina")
public class CocinaController {

    private final CocinaService service;

    public CocinaController(CocinaService service) { this.service = service; }

    @GetMapping("/pedidos")
    public List<PedidoResponse> cola() { return service.cola(); }

    @PatchMapping("/pedidos/{id}/estado")
    public PedidoResponse estado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return service.cambiarEstado(id, request.estado());
    }

    public record EstadoRequest(@NotNull EstadoPedido estado) {}
}
