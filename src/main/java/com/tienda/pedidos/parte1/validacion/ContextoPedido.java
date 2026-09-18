package com.tienda.pedidos.parte1.validacion;

import com.tienda.pedidos.dto.PedidoRequest;

public class ContextoPedido {
    private PedidoRequest pedidoRequest;
    private String tipoCliente;
    private double subtotal;
    private boolean rechazado;
    private String motivoRechazo;

    public ContextoPedido(PedidoRequest pedidoRequest) {
        this.pedidoRequest = pedidoRequest;
        this.rechazado = false;
    }

    public void rechazar(String motivo) {
        this.rechazado = true;
        this.motivoRechazo = motivo;
    }

    public PedidoRequest getPedidoRequest() {
        return pedidoRequest;
    }

    public void setPedidoRequest(PedidoRequest pedidoRequest) {
        this.pedidoRequest = pedidoRequest;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public boolean isRechazado() {
        return rechazado;
    }

    public void setRechazado(boolean rechazado) {
        this.rechazado = rechazado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }
}
