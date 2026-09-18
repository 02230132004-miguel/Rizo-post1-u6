package com.tienda.pedidos.parte1.validacion;

import com.tienda.pedidos.dto.ItemPedido;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ValidadorStock extends ValidadorPedido {

    private final JdbcTemplate jdbcTemplate;

    public ValidadorStock(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void procesarValidacion(ContextoPedido contexto) {
        if (contexto.getPedidoRequest().getItems() == null || contexto.getPedidoRequest().getItems().isEmpty()) {
            contexto.rechazar("El pedido no contiene productos");
            return;
        }

        for (ItemPedido item : contexto.getPedidoRequest().getItems()) {
            try {
                Integer stockDisponible = jdbcTemplate.queryForObject(
                        "SELECT stock FROM inventario WHERE producto_id = ?",
                        Integer.class,
                        item.getProductoId()
                );

                if (stockDisponible == null || stockDisponible < item.getCantidad()) {
                    contexto.rechazar("Stock insuficiente para el producto ID: " + item.getProductoId() +
                            " (Solicitado: " + item.getCantidad() + ", Disponible: " + (stockDisponible != null ? stockDisponible : 0) + ")");
                    return; // Corte anticipado (fail-fast)
                }
            } catch (EmptyResultDataAccessException e) {
                contexto.rechazar("Producto no encontrado en inventario con ID: " + item.getProductoId());
                return; // Corte anticipado
            }
        }
    }
}
