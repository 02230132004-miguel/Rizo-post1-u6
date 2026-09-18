package com.tienda.pedidos.parte1.descuento;

import com.tienda.pedidos.parte1.validacion.ContextoPedido;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DescuentoFrecuente implements EstrategiaDescuento {

    private final JdbcTemplate jdbcTemplate;

    public DescuentoFrecuente(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public double calcular(ContextoPedido contexto) {
        Long clienteId = contexto.getPedidoRequest().getClienteId();
        Integer pedidosPrevios = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pedidos WHERE cliente_id = ? AND estado = 'COMPLETADO'",
                Integer.class,
                clienteId
        );

        int total = pedidosPrevios != null ? pedidosPrevios : 0;
        if (total > 10) {
            return 0.08; // 8% de descuento para mas de 10 pedidos historicos
        } else if (total > 3) {
            return 0.04; // 4% de descuento para mas de 3 pedidos historicos
        } else {
            return 0.0;
        }
    }
}
