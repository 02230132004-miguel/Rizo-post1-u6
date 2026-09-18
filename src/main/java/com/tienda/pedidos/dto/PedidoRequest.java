package com.tienda.pedidos.dto;

import java.util.ArrayList;
import java.util.List;

public class PedidoRequest {
    private Long clienteId;
    private List<ItemPedido> items = new ArrayList<>();

    public PedidoRequest() {
    }

    public PedidoRequest(Long clienteId, List<ItemPedido> items) {
        this.clienteId = clienteId;
        this.items = items != null ? items : new ArrayList<>();
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<ItemPedido> getItems() {
        return items;
    }

    public void setItems(List<ItemPedido> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "PedidoRequest{" +
                "clienteId=" + clienteId +
                ", items=" + items +
                '}';
    }
}
