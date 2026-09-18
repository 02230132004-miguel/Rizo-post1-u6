package com.tienda.pedidos.parte1.descuento;

import com.tienda.pedidos.parte1.validacion.ContextoPedido;

public interface EstrategiaDescuento {
    /**
     * Calcula el porcentaje de descuento aplicable al pedido (e.g., 0.15 para 15%).
     * @param contexto Contexto con los datos del pedido.
     * @return Fraccion de descuento entre 0.0 y 1.0.
     */
    double calcular(ContextoPedido contexto);
}
