package com.arepaslulu.order.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.pricing.StandardPricingStrategy;

class PedidoFactoryTest {

    @Test
    void creaPedidoConTotalYEstado() {
        PedidoFactory factory = new PedidoFactory(new StandardPricingStrategy());

        var pedido = factory.crear(4, List.of(
                new LineaPedido(1L, "Arepa de pollo", new BigDecimal("14000"), 2, "Sin cebolla")
        ));

        assertThat(pedido.getMesaNumero()).isEqualTo(4);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ENVIADO_COCINA);
        assertThat(pedido.getItems()).hasSize(1);
        assertThat(pedido.getItems().getFirst().getProductoId()).isEqualTo(1L);
        assertThat(pedido.getItems().getFirst().getProductoNombre()).isEqualTo("Arepa de pollo");
        assertThat(pedido.getTotal()).isEqualByComparingTo("28000");
        assertThat(pedido.getItems().getFirst().getObservacion()).isEqualTo("Sin cebolla");
    }
}
