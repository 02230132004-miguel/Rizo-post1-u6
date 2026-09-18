package com.tienda.pedidos.parte1.validacion;

public abstract class ValidadorPedido {
    protected ValidadorPedido siguiente;

    public ValidadorPedido encadenar(ValidadorPedido siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    public void validar(ContextoPedido contexto) {
        procesarValidacion(contexto);
        if (!contexto.isRechazado() && siguiente != null) {
            siguiente.validar(contexto);
        }
    }

    protected abstract void procesarValidacion(ContextoPedido contexto);

    public ValidadorPedido getSiguiente() {
        return siguiente;
    }
}
