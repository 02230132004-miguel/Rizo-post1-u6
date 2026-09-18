package com.tienda.pedidos.parte1.service;

public interface EmailService {
    void enviar(String destinatario, String asunto, String cuerpo);
}
