package com.tienda.pedidos.parte2.descuento;

import com.tienda.pedidos.parte1.descuento.EstrategiaDescuento;
import com.tienda.pedidos.parte1.validacion.ContextoPedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DescuentoBlackFriday implements EstrategiaDescuento {

    private boolean activa;

    public DescuentoBlackFriday(@Value("${promo.black-friday.activa:false}") boolean activa) {
        this.activa = activa;
    }

    @Override
    public double calcular(ContextoPedido contexto) {
        return activa ? 0.25 : 0.0;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
