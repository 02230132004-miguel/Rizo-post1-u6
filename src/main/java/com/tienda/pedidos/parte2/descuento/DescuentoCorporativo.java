package com.tienda.pedidos.parte2.descuento;

import com.tienda.pedidos.parte1.descuento.EstrategiaDescuento;
import com.tienda.pedidos.parte1.validacion.ContextoPedido;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DescuentoCorporativo implements EstrategiaDescuento {

    private final JdbcTemplate jdbcTemplate;

    public DescuentoCorporativo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public double calcular(ContextoPedido contexto) {
        Long clienteId = contexto.getPedidoRequest().getClienteId();
        if (clienteId == null) {
            return 0.0;
        }

        try {
            String nit = jdbcTemplate.queryForObject(
                    "SELECT nit FROM clientes WHERE id = ?",
                    String.class,
                    clienteId
            );
            if (nit != null && !nit.trim().isEmpty()) {
                return 0.10; // 10% de descuento para clientes empresariales con NIT
            }
        } catch (DataAccessException e) {
            return 0.0;
        }

        return 0.0;
    }
}
