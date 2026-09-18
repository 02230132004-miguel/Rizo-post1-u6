package com.tienda.pedidos.parte1.service;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.parte1.validacion.ContextoPedido;
import org.springframework.stereotype.Service;

@Service
public class NotificacionPedidoService {

    private final EmailService emailService;

    public NotificacionPedidoService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void notificarConfirmacion(Long pedidoId, ContextoPedido contexto, double total) {
        Long clienteId = contexto.getPedidoRequest().getClienteId();
        String destinatario = "cliente" + clienteId + "@tienda.com";
        String asunto = "Confirmacion de Pedido #" + pedidoId;

        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Estimado cliente (ID: ").append(clienteId).append(", Tipo: ").append(contexto.getTipoCliente()).append("),\n\n");
        cuerpo.append("Su pedido #").append(pedidoId).append(" ha sido confirmado y procesado con exito.\n");
        cuerpo.append("Detalle de productos:\n");
        for (ItemPedido item : contexto.getPedidoRequest().getItems()) {
            cuerpo.append(" - Producto ID: ").append(item.getProductoId())
                    .append(" | Cantidad: ").append(item.getCantidad())
                    .append(" | Precio unitario: $").append(item.getPrecioUnitario())
                    .append(" | Subtotal item: $").append(item.getCantidad() * item.getPrecioUnitario()).append("\n");
        }
        cuerpo.append("\nSubtotal pedido: $").append(contexto.getSubtotal());
        cuerpo.append("\nTotal a pagar (con descuentos e impuestos aplicados): $").append(total);
        cuerpo.append("\n\nGracias por su compra.");

        emailService.enviar(destinatario, asunto, cuerpo.toString());
    }
}
