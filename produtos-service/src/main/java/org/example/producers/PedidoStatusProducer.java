package org.example.producers;

import org.example.models.PedidoStatusResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PedidoStatusProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${pedido.queue.response}")
    private String pedidoResponseQueue;

    public PedidoStatusProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarStatus(PedidoStatusResponse response) {
        System.out.println("📤 Enviando status do pedido de volta ao RabbitMQ: " + response);
        rabbitTemplate.convertAndSend(pedidoResponseQueue, response);
    }
}