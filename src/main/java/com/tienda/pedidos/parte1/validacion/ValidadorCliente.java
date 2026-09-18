package com.tienda.pedidos.parte1.validacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;

@Component
public class ValidadorCliente extends ValidadorPedido {

    private static final LocalTime HORA_LIMITE_MORA = LocalTime.of(20, 0);

    private final JdbcTemplate jdbcTemplate;
    private Clock clock;

    @Autowired
    public ValidadorCliente(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, Clock.systemDefaultZone());
    }

    public ValidadorCliente(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock != null ? clock : Clock.systemDefaultZone();
    }

    @Override
    protected void procesarValidacion(ContextoPedido contexto) {
        Long clienteId = contexto.getPedidoRequest().getClienteId();
        if (clienteId == null) {
            contexto.rechazar("El pedido no especifica un ID de cliente valido");
            return;
        }

        // 1. Validacion de existencia de cliente
        String tipoCliente;
        try {
            tipoCliente = jdbcTemplate.queryForObject(
                    "SELECT tipo_cliente FROM clientes WHERE id = ?",
                    String.class,
                    clienteId
            );
            contexto.setTipoCliente(tipoCliente);
        } catch (EmptyResultDataAccessException e) {
            contexto.rechazar("Cliente no encontrado con ID: " + clienteId);
            return; // Corte anticipado
        }

        // 2. Validacion de mora (facturas impagas en horario restringido)
        Integer facturasPendientes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM facturas WHERE cliente_id = ? AND pagada = FALSE",
                Integer.class,
                clienteId
        );

        if (facturasPendientes != null && facturasPendientes > 0) {
            if (esHorarioRestringido()) {
                contexto.rechazar("Cliente con facturas pendientes en horario restringido (antes de las 20:00)");
            }
        }
    }

    public boolean esHorarioRestringido() {
        LocalTime horaActual = LocalTime.now(clock);
        return horaActual.isBefore(HORA_LIMITE_MORA);
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
