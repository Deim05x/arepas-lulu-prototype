package com.arepaslulu.order.pricing;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calcularSubtotal(BigDecimal precioUnitario, int cantidad);
}
