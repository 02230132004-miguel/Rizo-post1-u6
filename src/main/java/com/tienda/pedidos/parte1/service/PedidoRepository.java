package com.tienda.pedidos.parte1.service;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.parte1.validacion.ContextoPedido;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

@Repository
public class PedidoRepository {

    private final JdbcTemplate jdbcTemplate;

    public PedidoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Long guardar(ContextoPedido contexto, double descuento, double impuesto, double total) {
        // 1. Insercion en tabla pedidos con generacion de clave
        String sqlPedido = "INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Timestamp fecha = new Timestamp(System.currentTimeMillis());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, contexto.getPedidoRequest().getClienteId());
            ps.setDouble(2, contexto.getSubtotal());
            ps.setDouble(3, descuento);
            ps.setDouble(4, impuesto);
            ps.setDouble(5, total);
            ps.setTimestamp(6, fecha);
            ps.setString(7, "COMPLETADO");
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey == null) {
            throw new IllegalStateException("Error al obtener la clave primaria generada para el pedido");
        }
        Long pedidoId = generatedKey.longValue();

        // 2. Insercion de detalles del pedido y decremento de stock en inventario
        String sqlDetalle = "INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad) VALUES (?, ?, ?)";
        String sqlStock = "UPDATE inventario SET stock = stock - ? WHERE producto_id = ?";

        for (ItemPedido item : contexto.getPedidoRequest().getItems()) {
            jdbcTemplate.update(sqlDetalle, pedidoId, item.getProductoId(), item.getCantidad());
            jdbcTemplate.update(sqlStock, item.getCantidad(), item.getProductoId());
        }

        return pedidoId;
    }
}
