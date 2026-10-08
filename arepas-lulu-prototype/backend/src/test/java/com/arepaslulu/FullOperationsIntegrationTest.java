package com.arepaslulu;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.arepaslulu.billing.domain.MedioPago;
import com.arepaslulu.billing.service.BillingService;
import com.arepaslulu.catalog.dto.ProductoRequest;
import com.arepaslulu.catalog.service.ProductoService;
import com.arepaslulu.delivery.service.DeliveryService;
import com.arepaslulu.inventory.service.InventoryService;
import com.arepaslulu.inventory.service.InventoryService.RecetaItem;
import com.arepaslulu.kitchen.service.CocinaService;
import com.arepaslulu.order.domain.EstadoPedido;
import com.arepaslulu.order.domain.TipoServicio;
import com.arepaslulu.order.dto.PedidoItemRequest;
import com.arepaslulu.order.dto.PedidoRequest;
import com.arepaslulu.order.service.PedidoService;
import com.arepaslulu.reporting.service.ReporteService;
import com.arepaslulu.table.service.MesaService;

@SpringBootTest
@Transactional
class FullOperationsIntegrationTest {

    @Autowired ProductoService productoService;
    @Autowired PedidoService pedidoService;
    @Autowired InventoryService inventoryService;
    @Autowired MesaService mesaService;
    @Autowired CocinaService cocinaService;
    @Autowired BillingService billingService;
    @Autowired ReporteService reporteService;
    @Autowired DeliveryService deliveryService;

    @Test
    void flujoCompletoIntegraF01HastaF08() {
        // F-01: catálogo
        var producto = productoService.crear(new ProductoRequest(
                "Arepa Flujo Completo",
                "Comidas",
                "Producto para prueba integrada F-01 a F-08",
                new BigDecimal("15000.00"),
                true));
        assertThat(producto.id()).isNotNull();

        // F-07: ingrediente + receta
        var ingrediente = inventoryService.crear(
                "Masa flujo completo", "kg", new BigDecimal("10.00"), new BigDecimal("2.00"));
        inventoryService.definirReceta(producto.id(),
                List.of(new RecetaItem(ingrediente.id(), new BigDecimal("1.50"))));
        assertThat(inventoryService.receta(producto.id())).hasSize(1);

        // F-02 + F-08: pedido en mesa; consume receta y ocupa mesa
        var pedido = pedidoService.crear(new PedidoRequest(
                12,
                TipoServicio.MESA,
                List.of(new PedidoItemRequest(producto.id(), 2, "Sin salsas"))));
        assertThat(pedido.estado()).isEqualTo(EstadoPedido.ENVIADO_COCINA);
        assertThat(pedido.total()).isEqualByComparingTo("30000.00");
        assertThat(stockIngrediente(ingrediente.id())).isEqualByComparingTo("7.00");
        assertThat(estadoMesa(12)).isEqualTo("OCUPADA");

        // F-03: cola KDS y transiciones de cocina
        assertThat(cocinaService.cola()).extracting(p -> p.id()).contains(pedido.id());
        cocinaService.cambiarEstado(pedido.id(), EstadoPedido.EN_PREPARACION);
        cocinaService.cambiarEstado(pedido.id(), EstadoPedido.LISTO);
        var servido = cocinaService.cambiarEstado(pedido.id(), EstadoPedido.SERVIDO);
        assertThat(servido.estado()).isEqualTo(EstadoPedido.SERVIDO);

        // F-04: pago + factura; libera mesa
        var pago = billingService.pagar(pedido.id(), MedioPago.TRANSFERENCIA, null);
        assertThat(pago.pedido().estado()).isEqualTo(EstadoPedido.PAGADO);
        assertThat(pago.factura().numero()).startsWith("AL-");
        assertThat(pago.factura().total()).isEqualByComparingTo("30000.00");
        assertThat(estadoMesa(12)).isEqualTo("DISPONIBLE");

        // F-05: cuadre y reporte DIAN simulado
        LocalDate hoy = LocalDate.now();
        var resumen = reporteService.resumen(hoy, hoy);
        assertThat(resumen.transacciones()).isGreaterThanOrEqualTo(1);
        assertThat(resumen.total()).isGreaterThanOrEqualTo(new BigDecimal("30000.00"));
        assertThat(resumen.pendientesDian()).isGreaterThanOrEqualTo(1);
        var dian = reporteService.reportarDian(hoy);
        assertThat(dian.reportadas()).isGreaterThanOrEqualTo(1);

        // F-06: cliente + domicilio reutilizando el mismo motor de pedidos
        var cliente = deliveryService.crearCliente(
                "Cliente Integración", "3009990011", "Carrera 10 # 20-30");
        var domicilio = deliveryService.crearDomicilio(
                cliente.id(),
                null,
                List.of(new PedidoItemRequest(producto.id(), 1, "Tostar bien")));
        assertThat(domicilio.pedido().tipoServicio()).isEqualTo(TipoServicio.DOMICILIO);
        assertThat(domicilio.domicilio().estado()).isEqualTo("PENDIENTE");
        assertThat(stockIngrediente(ingrediente.id())).isEqualByComparingTo("5.50");

        var despachado = deliveryService.cambiarEstado(
                domicilio.domicilio().id(), "DESPACHADO", "Domiciliario prueba");
        assertThat(despachado.estado()).isEqualTo("DESPACHADO");
        assertThat(despachado.repartidor()).isEqualTo("Domiciliario prueba");
    }

    private BigDecimal stockIngrediente(Long id) {
        return inventoryService.listarIngredientes().stream()
                .filter(i -> i.id().equals(id))
                .findFirst()
                .orElseThrow()
                .stockActual();
    }

    private String estadoMesa(int numero) {
        return mesaService.listar().stream()
                .filter(m -> m.numero().equals(numero))
                .findFirst()
                .orElseThrow()
                .estado();
    }
}
