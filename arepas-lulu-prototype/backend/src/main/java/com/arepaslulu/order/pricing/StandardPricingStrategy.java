package com.arepaslulu.order.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class StandardPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calcularSubtotal(BigDecimal precioUnitario, int cantidad) {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
