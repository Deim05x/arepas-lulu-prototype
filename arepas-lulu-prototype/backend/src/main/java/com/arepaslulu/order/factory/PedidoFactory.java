package com.arepaslulu.order.factory;

import java.util.List;

import org.springframework.stereotype.Component;

import com.arepaslulu.catalog.domain.Producto;
import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.domain.Pedido;
import com.arepaslulu.order.domain.PedidoItem;
import com.arepaslulu.order.domain.TipoServicio;
import com.arepaslulu.order.dto.PedidoItemRequest;
import com.arepaslulu.order.pricing.PricingStrategy;

@Component
public class PedidoFactory {

    private final PricingStrategy pricingStrategy;

    public PedidoFactory(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public Pedido crear(Integer mesaNumero, TipoServicio tipoServicio, List<ItemConProducto> items) {
        Pedido pedido = new Pedido(mesaNumero, tipoServicio, EstadoPedido.ENVIADO_COCINA);
        for (ItemConProducto item : items) {
            Producto producto = item.producto();
            PedidoItemRequest request = item.request();
            pedido.agregarItem(new PedidoItem(
                    producto,
                    request.cantidad(),
                    producto.getPrecio(),
                    pricingStrategy.calcularSubtotal(producto.getPrecio(), request.cantidad()),
                    limpiar(request.observacion())
            ));
        }
        return pedido;
    }

    public Pedido crear(int mesaNumero, List<ItemConProducto> items) {
        return crear(mesaNumero, TipoServicio.MESA, items);
    }

    private String limpiar(String text) {
        if (text == null) return null;
        String normalized = text.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    public record ItemConProducto(PedidoItemRequest request, Producto producto) {
    }
}
