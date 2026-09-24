package com.arepaslulu.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arepaslulu.catalog.domain.Producto;
import com.arepaslulu.catalog.dto.ProductoRequest;
import com.arepaslulu.catalog.event.CatalogEventPublisher;
import com.arepaslulu.catalog.mapper.ProductoMapper;
import com.arepaslulu.catalog.repository.ProductoRepository;
import com.arepaslulu.common.exception.BusinessRuleException;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    private ProductoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoService(repository, new ProductoMapper(), new CatalogEventPublisher());
    }

    @Test
    void creaProductoValido() {
        when(repository.existsByNombreIgnoreCase("Arepa Todo Terreno")).thenReturn(false);
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.crear(new ProductoRequest(
                "Arepa Todo Terreno",
                "Arepas",
                "Producto de prueba",
                new BigDecimal("18000.00"),
                true
        ));

        assertThat(response.nombre()).isEqualTo("Arepa Todo Terreno");
        assertThat(response.precio()).isEqualByComparingTo("18000.00");
        assertThat(response.disponible()).isTrue();
    }

    @Test
    void rechazaNombreDuplicado() {
        when(repository.existsByNombreIgnoreCase("Arepa mixta")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(new ProductoRequest(
                "Arepa mixta", "Arepas", null, new BigDecimal("10000"), true)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Ya existe");
    }
}
