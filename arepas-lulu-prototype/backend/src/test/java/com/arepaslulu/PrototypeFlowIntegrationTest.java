package com.arepaslulu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.catalog.dto.ProductoRequest;
import com.arepaslulu.catalog.service.ProductoService;
import com.arepaslulu.common.exception.BusinessRuleException;
import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.dto.PedidoItemRequest;
import com.arepaslulu.order.dto.PedidoRequest;
import com.arepaslulu.order.service.PedidoService;

@SpringBootTest
@Transactional
class PrototypeFlowIntegrationTest {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PedidoService pedidoService;

    @Test
    void flujoF01F02MantieneTrazabilidadYReglas() {
        var producto = productoService.crear(new ProductoRequest(
                "Arepa Todo Terreno Integracion",
                "Arepas",
                "Producto de integración",
                new BigDecimal("18000.00"),
                true
        ));

        assertThat(producto.id()).isNotNull();
        assertThat(producto.disponible()).isTrue();

        var pedido = pedidoService.crear(new PedidoRequest(
                4,
                List.of(new PedidoItemRequest(producto.id(), 2, "Sin cebolla"))
        ));

        assertThat(pedido.id()).isNotNull();
        assertThat(pedido.mesaNumero()).isEqualTo(4);
        assertThat(pedido.estado()).isEqualTo(EstadoPedido.ENVIADO_COCINA);
        assertThat(pedido.total()).isEqualByComparingTo("36000.00");
        assertThat(pedido.items()).singleElement().satisfies(item -> {
            assertThat(item.productoId()).isEqualTo(producto.id());
            assertThat(item.cantidad()).isEqualTo(2);
            assertThat(item.observacion()).isEqualTo("Sin cebolla");
        });

        productoService.cambiarDisponibilidad(producto.id(), false);

        assertThatThrownBy(() -> pedidoService.crear(new PedidoRequest(
                5,
                List.of(new PedidoItemRequest(producto.id(), 1, null))
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no está disponible");
    }
}
