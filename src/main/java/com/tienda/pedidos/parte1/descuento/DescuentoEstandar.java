package com.tienda.pedidos.parte1.descuento;

import com.tienda.pedidos.parte1.validacion.ContextoPedido;
import org.springframework.stereotype.Component;

@Component
public class DescuentoEstandar implements EstrategiaDescuento {

    @Override
    public double calcular(ContextoPedido contexto) {
        return 0.0; // Clientes estandar no poseen descuento base por tipo
    }
}
