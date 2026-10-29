package com.arepaslulu.order.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.arepaslulu.catalog.dto.ProductoRequest;
import com.arepaslulu.catalog.service.ProductoService;
import com.arepaslulu.order.service.PedidoService;

@SpringBootTest
@Transactional
class PedidoControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PedidoService pedidoService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void creaYConsultaPedidoPorIdYPorMesa() throws Exception {
        var producto = productoService.crear(new ProductoRequest(
                "Arepa HTTP F02",
                "Arepas",
                "Producto para verificar el contrato HTTP",
                new BigDecimal("14000.00"),
                true));

        String request = """
                {
                  "mesaNumero": 4,
                  "items": [
                    {"productoId": %d, "cantidad": 2, "observacion": "Sin cebolla"}
                  ]
                }
                """.formatted(producto.id());

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mesaNumero").value(4))
                .andExpect(jsonPath("$.estado").value("ENVIADO_COCINA"))
                .andExpect(jsonPath("$.total").value(28000.00))
                .andExpect(jsonPath("$.items[0].productoId").value(producto.id()))
                .andExpect(jsonPath("$.items[0].observacion").value("Sin cebolla"));

        Long pedidoId = pedidoService.listar(4).getFirst().id();

        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId))
                .andExpect(jsonPath("$.items[0].productoId").value(producto.id()));

        mockMvc.perform(get("/api/pedidos").param("mesa", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pedidoId))
                .andExpect(jsonPath("$[0].mesaNumero").value(4));
    }

    @Test
    void rechazaPayloadSinItemsConHttp400() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mesaNumero\":4,\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.items").exists());
    }

    @Test
    void rechazaProductoNoDisponibleConHttp409() throws Exception {
        var producto = productoService.crear(new ProductoRequest(
                "Arepa no disponible HTTP F02",
                "Arepas",
                null,
                new BigDecimal("12000.00"),
                true));
        productoService.cambiarDisponibilidad(producto.id(), false);

        String request = """
                {"mesaNumero":5,"items":[{"productoId":%d,"cantidad":1}]}
                """.formatted(producto.id());

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no está disponible")));
    }
}
