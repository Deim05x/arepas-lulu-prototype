package com.arepaslulu.reporting.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arepaslulu.reporting.service.ReporteService;
import com.arepaslulu.reporting.service.ReporteService.ReporteDian;
import com.arepaslulu.reporting.service.ReporteService.ResumenCaja;

@RestController
@RequestMapping("/api/caja")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) { this.service = service; }

    @GetMapping("/resumen")
    public ResumenCaja resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.resumen(desde, hasta);
    }

    @PostMapping("/dian")
    public ReporteDian dian(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.reportarDian(fecha);
    }
}
