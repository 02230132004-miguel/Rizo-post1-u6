package com.tienda.pedidos.parte2;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.dto.PedidoRequest;
import com.tienda.pedidos.dto.ResultadoPedido;
import com.tienda.pedidos.parte1.validacion.ValidadorCliente;
import com.tienda.pedidos.parte2.descuento.DescuentoBlackFriday;
import com.tienda.pedidos.parte2.service.GestorPedidos;
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
class GestorPedidosParte2Test {

    @Autowired
    @Qualifier("gestorPedidosParte2")
    private GestorPedidos gestorPedidosParte2;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ValidadorCliente validadorCliente;

    @Autowired
    private DescuentoBlackFriday descuentoBlackFriday;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("UPDATE inventario SET stock = 100 WHERE producto_id IN (101, 102, 103)");
        descuentoBlackFriday.setActiva(false);
        Instant instant = Instant.parse("2026-09-18T14:00:00Z");
        validadorCliente.setClock(Clock.fixed(instant, ZoneId.of("UTC")));
    }

    @Test
    @DisplayName("Parte 2: Aplicacion de promocion Black Friday (25% mayor beneficio)")
    void testPromocionBlackFriday() {
        descuentoBlackFriday.setActiva(true);

        // Cliente 4 (ESTANDAR). Con Black Friday obtiene 25%
        // Subtotal = 100.000 | Descuento (25%) = 25.000 | Base = 75.000 | IVA (19%) = 14.250 | Total = 89.250
        PedidoRequest request = new PedidoRequest(4L, List.of(
                new ItemPedido(101L, 1, 100000.0)
        ));

        ResultadoPedido resultado = gestorPedidosParte2.procesarPedido(request);

        assertTrue(resultado.isExitoso());
        assertEquals(89250.0, resultado.getTotal(), 0.01, "Total con 25% de Black Friday e IVA");
    }

    @Test
    @DisplayName("Parte 2: Aplicacion de promocion Corporativo con NIT (10%)")
    void testPromocionCorporativoConNit() {
        descuentoBlackFriday.setActiva(false);

        // Cliente 5 tiene NIT en BD -> 10% corporativo
        // Subtotal = 200.000 | Descuento (10%) = 20.000 | Base = 180.000 | IVA (19%) = 34.200 | Total = 214.200
        PedidoRequest request = new PedidoRequest(5L, List.of(
                new ItemPedido(101L, 2, 100000.0)
        ));

        ResultadoPedido resultado = gestorPedidosParte2.procesarPedido(request);

        assertTrue(resultado.isExitoso());
        assertEquals(214200.0, resultado.getTotal(), 0.01, "Total con 10% corporativo e IVA");
    }

    @Test
    @DisplayName("Parte 2: Aplicacion de promocion por Volumen (> 20 unidades -> 12%)")
    void testPromocionPorVolumen() {
        descuentoBlackFriday.setActiva(false);

        // Cliente 4 (ESTANDAR), compra 25 unidades (> 20 unidades -> 12%)
        // Subtotal = 25 * 10.000 = 250.000 | Descuento (12%) = 30.000 | Base = 220.000 | IVA = 41.800 | Total = 261.800
        PedidoRequest request = new PedidoRequest(4L, List.of(
                new ItemPedido(101L, 25, 10000.0)
        ));

        ResultadoPedido resultado = gestorPedidosParte2.procesarPedido(request);

        assertTrue(resultado.isExitoso());
        assertEquals(261800.0, resultado.getTotal(), 0.01, "Total con 12% por volumen e IVA");
    }

    @Test
    @DisplayName("Parte 2: Persistencia integral y decremento de stock en base de datos H2")
    void testPersistenciaYDecrementoDeStock() {
        descuentoBlackFriday.setActiva(false);

        Integer stockInicial101 = jdbcTemplate.queryForObject("SELECT stock FROM inventario WHERE producto_id = 101", Integer.class);
        assertNotNull(stockInicial101);

        PedidoRequest request = new PedidoRequest(4L, List.of(
                new ItemPedido(101L, 5, 20000.0)
        ));

        ResultadoPedido resultado = gestorPedidosParte2.procesarPedido(request);

        assertTrue(resultado.isExitoso());
        assertNotNull(resultado.getPedidoId());

        // Verificar decremento de stock
        Integer stockFinal101 = jdbcTemplate.queryForObject("SELECT stock FROM inventario WHERE producto_id = 101", Integer.class);
        assertEquals(stockInicial101 - 5, stockFinal101);

        // Verificar insercion en tabla pedidos
        Integer pedidosRegistrados = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pedidos WHERE id = ?",
                Integer.class,
                resultado.getPedidoId()
        );
        assertEquals(1, pedidosRegistrados);
    }
}
