package com.tienda.pedidos.parte2.descuento;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.parte1.descuento.EstrategiaDescuento;
import com.tienda.pedidos.parte1.validacion.ContextoPedido;
import org.springframework.stereotype.Component;

@Component
public class DescuentoVolumen implements EstrategiaDescuento {

    @Override
    public double calcular(ContextoPedido contexto) {
        if (contexto.getPedidoRequest() == null || contexto.getPedidoRequest().getItems() == null) {
            return 0.0;
        }

        int totalUnidades = contexto.getPedidoRequest().getItems().stream()
                .mapToInt(ItemPedido::getCantidad)
                .sum();

        if (totalUnidades > 20) {
            return 0.12; // 12% de descuento por volumen para pedidos mayores a 20 unidades
        }

        return 0.0;
    }
}
