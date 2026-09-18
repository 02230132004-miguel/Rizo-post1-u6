package com.tienda.pedidos.parte1.service;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.dto.PedidoRequest;
import com.tienda.pedidos.dto.ResultadoPedido;
import com.tienda.pedidos.parte1.descuento.EstrategiaDescuento;
import com.tienda.pedidos.parte1.descuento.SelectorEstrategiaDescuento;
import com.tienda.pedidos.parte1.validacion.ContextoPedido;
import com.tienda.pedidos.parte1.validacion.ValidadorCliente;
import com.tienda.pedidos.parte1.validacion.ValidadorStock;
import org.springframework.stereotype.Service;

@Service("gestorPedidosParte1")
public class GestorPedidos {

    private static final double TASA_IMPUESTO = 0.19; // IVA 19%

    private final ValidadorStock validadorStock;
    private final ValidadorCliente validadorCliente;
    private final SelectorEstrategiaDescuento selectorEstrategiaDescuento;
    private final PedidoRepository pedidoRepository;
    private final NotificacionPedidoService notificacionPedidoService;

    public GestorPedidos(ValidadorStock validadorStock,
                         ValidadorCliente validadorCliente,
                         SelectorEstrategiaDescuento selectorEstrategiaDescuento,
                         PedidoRepository pedidoRepository,
                         NotificacionPedidoService notificacionPedidoService) {
        this.validadorStock = validadorStock;
        this.validadorCliente = validadorCliente;
        this.selectorEstrategiaDescuento = selectorEstrategiaDescuento;
        this.pedidoRepository = pedidoRepository;
        this.notificacionPedidoService = notificacionPedidoService;
    }

    /**
     * Orquestador Parte 1: Realiza validacion en cadena (Stock -> Cliente),
     * calculo de subtotal, calculo de descuento de cliente por Strategy,
     * calculo de impuestos, persistencia y notificacion.
     *
     * @param request Solicitud del pedido
     * @return ResultadoPedido
     */
    public ResultadoPedido procesarPedido(PedidoRequest request) {
        if (request == null) {
            return ResultadoPedido.rechazado("La solicitud de pedido no puede ser nula");
        }

        ContextoPedido contexto = new ContextoPedido(request);

        // 1. Calculo inicial de subtotal
        double subtotal = 0.0;
        if (request.getItems() != null) {
            for (ItemPedido item : request.getItems()) {
                subtotal += item.getCantidad() * item.getPrecioUnitario();
            }
        }
        contexto.setSubtotal(subtotal);

        // 2. Ejecucion de la Cadena de Responsabilidad (Fail-Fast: ValidadorStock -> ValidadorCliente)
        validadorStock.encadenar(validadorCliente);
        validadorStock.validar(contexto);

        if (contexto.isRechazado()) {
            return ResultadoPedido.rechazado(contexto.getMotivoRechazo());
        }

        // 3. Descuento segun tipo de cliente (Strategy)
        EstrategiaDescuento estrategia = selectorEstrategiaDescuento.obtenerEstrategia(contexto.getTipoCliente());
        double porcentajeDescuento = estrategia.calcular(contexto);
        double montoDescuento = subtotal * porcentajeDescuento;
        double baseGravable = subtotal - montoDescuento;
        double impuesto = baseGravable * TASA_IMPUESTO;
        double total = baseGravable + impuesto;

        // 4. Persistencia en Base de Datos H2
        Long pedidoId = pedidoRepository.guardar(contexto, montoDescuento, impuesto, total);

        // 5. Notificacion
        notificacionPedidoService.notificarConfirmacion(pedidoId, contexto, total);

        // 6. Retorno
        return ResultadoPedido.confirmado(pedidoId, total, "Pedido procesado exitosamente (Parte 1)");
    }
}
