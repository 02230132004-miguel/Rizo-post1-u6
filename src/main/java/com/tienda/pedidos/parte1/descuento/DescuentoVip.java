package com.tienda.pedidos.parte1.descuento;

import com.tienda.pedidos.parte1.validacion.ContextoPedido;
import org.springframework.stereotype.Component;

@Component
public class DescuentoVip implements EstrategiaDescuento {

    @Override
    public double calcular(ContextoPedido contexto) {
        double subtotal = contexto.getSubtotal();
        if (subtotal > 1_000_000.0) {
            return 0.15; // 15% para compras mayores a $1.000.000
        } else if (subtotal > 500_000.0) {
            return 0.10; // 10% para compras mayores a $500.000
        } else {
            return 0.05; // 5% base para clientes VIP
        }
    }
}
