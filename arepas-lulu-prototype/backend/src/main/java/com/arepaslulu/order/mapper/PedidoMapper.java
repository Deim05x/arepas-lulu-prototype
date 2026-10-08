package com.arepaslulu.order.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.arepaslulu.order.domain.Pedido;
import com.arepaslulu.order.dto.PedidoItemResponse;
import com.arepaslulu.order.dto.PedidoResponse;

@Component
public class PedidoMapper {

    public PedidoResponse toResponse(Pedido pedido) {
        List<PedidoItemResponse> items = pedido.getItems().stream()
                .map(item -> new PedidoItemResponse(
                        item.getProducto().getId(),
                        item.getProductoNombre(),
                        item.getCantidad(),
                        item.getPrecioUnitario(),
                        item.getSubtotal(),
                        item.getObservacion()
                ))
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getMesaNumero(),
                pedido.getTipoServicio(),
                pedido.getEstado(),
                pedido.getTotal(),
                pedido.getCreatedAt(),
                items
        );
    }
}
