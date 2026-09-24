package com.arepaslulu.order.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class StandardPricingStrategyTest {

    private final StandardPricingStrategy strategy = new StandardPricingStrategy();

    @Test
    void calculaSubtotal() {
        assertThat(strategy.calcularSubtotal(new BigDecimal("14000.00"), 3))
                .isEqualByComparingTo("42000.00");
    }
}
