package com.arepaslulu.table.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arepaslulu.table.repository.MesaRepository.MesaRow;
import com.arepaslulu.table.service.MesaService;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService service;

    public MesaController(MesaService service) { this.service = service; }

    @GetMapping
    public List<MesaRow> listar() { return service.listar(); }

    @PatchMapping("/{numero}")
    public MesaRow estado(@PathVariable Integer numero, @RequestBody EstadoMesaRequest request) {
        return service.cambiarEstado(numero, request.estado(), request.referencia());
    }

    public record EstadoMesaRequest(String estado, String referencia) {}
}
