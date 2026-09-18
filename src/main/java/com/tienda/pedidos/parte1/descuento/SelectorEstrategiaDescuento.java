package com.tienda.pedidos.parte1.descuento;

import org.springframework.stereotype.Component;

@Component
public class SelectorEstrategiaDescuento {

    private final DescuentoVip descuentoVip;
    private final DescuentoFrecuente descuentoFrecuente;
    private final DescuentoEstandar descuentoEstandar;

    public SelectorEstrategiaDescuento(DescuentoVip descuentoVip,
                                       DescuentoFrecuente descuentoFrecuente,
                                       DescuentoEstandar descuentoEstandar) {
        this.descuentoVip = descuentoVip;
        this.descuentoFrecuente = descuentoFrecuente;
        this.descuentoEstandar = descuentoEstandar;
    }

    public EstrategiaDescuento obtenerEstrategia(String tipoCliente) {
        if (tipoCliente == null) {
            return descuentoEstandar;
        }

        switch (tipoCliente.trim().toUpperCase()) {
            case "VIP":
                return descuentoVip;
            case "FRECUENTE":
                return descuentoFrecuente;
            case "ESTANDAR":
            default:
                return descuentoEstandar;
        }
    }
}
