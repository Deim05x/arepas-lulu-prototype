package com.arepaslulu.order.factory;

import java.util.List;

import org.springframework.stereotype.Component;

import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.domain.Pedido;
import com.arepaslulu.order.domain.PedidoItem;
import com.arepaslulu.order.domain.TipoServicio;
import com.arepaslulu.order.pricing.PricingStrategy;

@Component
public class PedidoFactory {

    private final PricingStrategy pricingStrategy;

    public PedidoFactory(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public Pedido crear(Integer mesaNumero, TipoServicio tipoServicio, List<LineaPedido> items) {
        Pedido pedido = new Pedido(mesaNumero, tipoServicio, EstadoPedido.ENVIADO_COCINA);
        for (LineaPedido item : items) {
            pedido.agregarItem(new PedidoItem(
                    item.productoId(),
                    item.productoNombre(),
                    item.cantidad(),
                    item.precioUnitario(),
                    pricingStrategy.calcularSubtotal(item.precioUnitario(), item.cantidad()),
                    limpiar(item.observacion())
            ));
        }
        return pedido;
    }

    public Pedido crear(int mesaNumero, List<LineaPedido> items) {
        return crear(mesaNumero, TipoServicio.MESA, items);
    }

    private String limpiar(String text) {
        if (text == null) return null;
        String normalized = text.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }
}
