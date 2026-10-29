package com.arepaslulu.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.arepaslulu.catalog.dto.ProductoRequest;
import com.arepaslulu.catalog.service.ProductoService;
import com.arepaslulu.common.exception.BusinessRuleException;
import com.arepaslulu.inventory.service.InventoryService;
import com.arepaslulu.inventory.service.InventoryService.RecetaItem;
import com.arepaslulu.order.dto.PedidoItemRequest;
import com.arepaslulu.order.dto.PedidoRequest;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PedidoConcurrencyIntegrationTest {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PedidoService pedidoService;

    @Test
    void soloUnPedidoConsumeLaUltimaUnidadDisponible() throws Exception {
        var producto = productoService.crear(new ProductoRequest(
                "Arepa concurrencia F02",
                "Arepas",
                null,
                new BigDecimal("10000.00"),
                true));
        var ingrediente = inventoryService.crear(
                "Ingrediente concurrencia F02",
                "unidad",
                BigDecimal.ONE,
                BigDecimal.ZERO);
        inventoryService.definirReceta(
                producto.id(),
                List.of(new RecetaItem(ingrediente.id(), BigDecimal.ONE)));

        CountDownLatch inicio = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> primero = executor.submit(() -> crearPedidoAlLiberar(inicio, producto.id(), 1));
            Future<Boolean> segundo = executor.submit(() -> crearPedidoAlLiberar(inicio, producto.id(), 2));

            inicio.countDown();

            assertThat(List.of(primero.get(), segundo.get()))
                    .containsExactlyInAnyOrder(true, false);
        }

        BigDecimal stockFinal = inventoryService.listarIngredientes().stream()
                .filter(item -> item.id().equals(ingrediente.id()))
                .findFirst()
                .orElseThrow()
                .stockActual();

        assertThat(stockFinal).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(pedidoService.listar(null).stream()
                .filter(pedido -> pedido.items().stream()
                        .anyMatch(item -> item.productoId().equals(producto.id()))))
                .hasSize(1);
    }

    private boolean crearPedidoAlLiberar(CountDownLatch inicio, Long productoId, int mesa) throws InterruptedException {
        inicio.await();
        try {
            pedidoService.crear(new PedidoRequest(
                    mesa,
                    List.of(new PedidoItemRequest(productoId, 1, null))));
            return true;
        } catch (BusinessRuleException ex) {
            return false;
        }
    }
}
