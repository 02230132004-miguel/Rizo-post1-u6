package com.tienda.pedidos.parte2.descuento;

import com.tienda.pedidos.parte1.descuento.EstrategiaDescuento;
import com.tienda.pedidos.parte1.descuento.SelectorEstrategiaDescuento;
import com.tienda.pedidos.parte1.validacion.ContextoPedido;
import org.springframework.stereotype.Component;

@Component
public class CalculadorDescuentoFinal {

    private final SelectorEstrategiaDescuento selectorEstrategiaDescuento;
    private final DescuentoBlackFriday descuentoBlackFriday;
    private final DescuentoCorporativo descuentoCorporativo;
    private final DescuentoVolumen descuentoVolumen;

    public CalculadorDescuentoFinal(SelectorEstrategiaDescuento selectorEstrategiaDescuento,
                                    DescuentoBlackFriday descuentoBlackFriday,
                                    DescuentoCorporativo descuentoCorporativo,
                                    DescuentoVolumen descuentoVolumen) {
        this.selectorEstrategiaDescuento = selectorEstrategiaDescuento;
        this.descuentoBlackFriday = descuentoBlackFriday;
        this.descuentoCorporativo = descuentoCorporativo;
        this.descuentoVolumen = descuentoVolumen;
    }

    /**
     * Evalua las estrategias de cliente y las promociones disponibles, seleccionando
     * el mayor porcentaje de descuento aplicable mediante Math.max.
     *
     * @param contexto Contexto del pedido
     * @return Porcentaje de descuento final (entre 0.0 y 1.0)
     */
    public double calcularDescuento(ContextoPedido contexto) {
        // 1. Descuento base por tipo de cliente
        EstrategiaDescuento estrategiaCliente = selectorEstrategiaDescuento.obtenerEstrategia(contexto.getTipoCliente());
        double descuentoCliente = estrategiaCliente.calcular(contexto);

        // 2. Descuentos por campanas promocionales
        double descBlackFriday = descuentoBlackFriday.calcular(contexto);
        double descCorporativo = descuentoCorporativo.calcular(contexto);
        double descVolumen = descuentoVolumen.calcular(contexto);

        // 3. Seleccion del maximo beneficio
        double maxCampana = Math.max(descBlackFriday, Math.max(descCorporativo, descVolumen));
        return Math.max(descuentoCliente, maxCampana);
    }
}
