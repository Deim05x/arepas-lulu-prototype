package com.arepaslulu.order.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.arepaslulu.catalog.domain.Producto;
import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.dto.PedidoItemRequest;
import com.arepaslulu.order.pricing.StandardPricingStrategy;

class PedidoFactoryTest {

    @Test
    void creaPedidoConTotalYEstado() {
        Producto producto = new Producto("Arepa de pollo", "Arepas", null, new BigDecimal("14000"), true);
        PedidoFactory factory = new PedidoFactory(new StandardPricingStrategy());

        var pedido = factory.crear(4, List.of(
                new PedidoFactory.ItemConProducto(new PedidoItemRequest(1L, 2, "Sin cebolla"), producto)
        ));

        assertThat(pedido.getMesaNumero()).isEqualTo(4);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ENVIADO_COCINA);
        assertThat(pedido.getItems()).hasSize(1);
        assertThat(pedido.getTotal()).isEqualByComparingTo("28000");
        assertThat(pedido.getItems().getFirst().getObservacion()).isEqualTo("Sin cebolla");
    }
}
