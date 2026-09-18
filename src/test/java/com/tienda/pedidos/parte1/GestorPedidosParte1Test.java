package com.tienda.pedidos.parte1;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.dto.PedidoRequest;
import com.tienda.pedidos.dto.ResultadoPedido;
import com.tienda.pedidos.parte1.service.GestorPedidos;
import com.tienda.pedidos.parte1.validacion.ValidadorCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GestorPedidosParte1Test {

    @Autowired
    @Qualifier("gestorPedidosParte1")
    private GestorPedidos gestorPedidosParte1;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ValidadorCliente validadorCliente;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("UPDATE inventario SET stock = 100 WHERE producto_id IN (101, 102, 103)");
        Instant instant = Instant.parse("2026-09-18T14:00:00Z");
        validadorCliente.setClock(Clock.fixed(instant, ZoneId.of("UTC")));
    }

    @Test
    @DisplayName("Parte 1: Rechazo por stock insuficiente con corte anticipado (fail-fast)")
    void testRechazoPorStockInsuficienteCorteAnticipado() {
        PedidoRequest request = new PedidoRequest(999L, List.of(
                new ItemPedido(101L, 150, 10000.0)
        ));

        ResultadoPedido resultado = gestorPedidosParte1.procesarPedido(request);

        assertFalse(resultado.isExitoso(), "El pedido debe ser rechazado por falta de stock");
        assertNotNull(resultado.getMensaje());
        assertTrue(resultado.getMensaje().contains("Stock insuficiente"),
                "El motivo debe ser corte anticipado de stock sin haber llegado a validar el cliente");
    }

    @Test
    @DisplayName("Parte 1: Rechazo por cliente inexistente tras pasar validacion de stock")
    void testRechazoPorClienteInexistente() {
        PedidoRequest request = new PedidoRequest(9999L, List.of(
                new ItemPedido(101L, 2, 50000.0)
        ));

        ResultadoPedido resultado = gestorPedidosParte1.procesarPedido(request);

        assertFalse(resultado.isExitoso(), "El pedido debe ser rechazado");
        assertTrue(resultado.getMensaje().contains("Cliente no encontrado"),
                "Debe indicar que el cliente no fue encontrado en la base de datos");
    }

    @Test
    @DisplayName("Parte 1: Rechazo de cliente moroso en horario restringido (< 20:00)")
    void testRechazoClienteMorosoEnHorarioRestringido() {
        PedidoRequest request = new PedidoRequest(3L, List.of(
                new ItemPedido(101L, 1, 80000.0)
        ));

        ResultadoPedido resultado = gestorPedidosParte1.procesarPedido(request);

        assertFalse(resultado.isExitoso(), "El pedido debe ser rechazado por mora");
        assertTrue(resultado.getMensaje().contains("facturas pendientes"),
                "Debe indicar que posee facturas pendientes en horario restringido");
    }

    @Test
    @DisplayName("Parte 1: Confirmacion de pedido VIP con escala de descuento segun subtotal")
    void testConfirmacionPedidoVipConEscalas() {
        // Caso 1: Subtotal > 1.000.000 (15% VIP) -> Subtotal: 1.200.000 | Desc: 180.000 | Base: 1.020.000 | IVA: 193.800 | Total: 1.213.800
        PedidoRequest requestVip15 = new PedidoRequest(1L, List.of(
                new ItemPedido(101L, 12, 100000.0)
        ));
        ResultadoPedido res15 = gestorPedidosParte1.procesarPedido(requestVip15);
        assertTrue(res15.isExitoso());
        assertEquals(1213800.0, res15.getTotal(), 0.01);

        // Caso 2: Subtotal > 500.000 (10% VIP) -> Subtotal: 800.000 | Desc: 80.000 | Base: 720.000 | IVA: 136.800 | Total: 856.800
        PedidoRequest requestVip10 = new PedidoRequest(1L, List.of(
                new ItemPedido(102L, 8, 100000.0)
        ));
        ResultadoPedido res10 = gestorPedidosParte1.procesarPedido(requestVip10);
        assertTrue(res10.isExitoso());
        assertEquals(856800.0, res10.getTotal(), 0.01);

        // Caso 3: Subtotal <= 500.000 (5% VIP base) -> Subtotal: 200.000 | Desc: 10.000 | Base: 190.000 | IVA: 36.100 | Total: 226.100
        PedidoRequest requestVip5 = new PedidoRequest(1L, List.of(
                new ItemPedido(103L, 2, 100000.0)
        ));
        ResultadoPedido res5 = gestorPedidosParte1.procesarPedido(requestVip5);
        assertTrue(res5.isExitoso());
        assertEquals(226100.0, res5.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Parte 1: Confirmacion de cliente Frecuente con descuento por historial (> 10 pedidos)")
    void testConfirmacionDescuentoFrecuente() {
        // Cliente 2 tiene 12 pedidos en data.sql -> 8% de descuento
        // Subtotal = 100.000 | Descuento (8%) = 8.000 | Base = 92.000 | IVA (19%) = 17.480 | Total = 109.480
        PedidoRequest request = new PedidoRequest(2L, List.of(
                new ItemPedido(101L, 2, 50000.0)
        ));

        ResultadoPedido resultado = gestorPedidosParte1.procesarPedido(request);

        assertTrue(resultado.isExitoso());
        assertEquals(109480.0, resultado.getTotal(), 0.01);
    }
}
