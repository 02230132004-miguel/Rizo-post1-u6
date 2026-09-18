package com.tienda.pedidos.dto;

public class ResultadoPedido {
    private boolean exitoso;
    private Long pedidoId;
    private double total;
    private String mensaje;

    public ResultadoPedido() {
    }

    public ResultadoPedido(boolean exitoso, Long pedidoId, double total, String mensaje) {
        this.exitoso = exitoso;
        this.pedidoId = pedidoId;
        this.total = total;
        this.mensaje = mensaje;
    }

    public static ResultadoPedido confirmado(Long pedidoId, double total, String mensaje) {
        return new ResultadoPedido(true, pedidoId, total, mensaje);
    }

    public static ResultadoPedido confirmado(Long pedidoId, double total) {
        return new ResultadoPedido(true, pedidoId, total, "Pedido confirmado exitosamente");
    }

    public static ResultadoPedido rechazado(String motivo) {
        return new ResultadoPedido(false, null, 0.0, motivo);
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return "ResultadoPedido{" +
                "exitoso=" + exitoso +
                ", pedidoId=" + pedidoId +
                ", total=" + total +
                ", mensaje='" + mensaje + '\'' +
                '}';
    }
}
