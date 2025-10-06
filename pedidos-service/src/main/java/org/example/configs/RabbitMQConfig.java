package org.example.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${pedido.queue.name}")
    private String pedidoQueueName;

    @Value("${pedido.queue.response}")
    private String pedidoResponseQueueName;

    /**
     * Fila principal — onde o pedidos-service envia novos pedidos.
     */
    @Bean
    public Queue pedidoQueue() {
        // true = fila persistente (não é apagada quando o broker reinicia)
        return new Queue(pedidoQueueName, true);
    }

    /**
     * Fila de resposta — onde o produtos-service envia status de confirmação.
     */
    @Bean
    public Queue pedidoResponseQueue() {
        return new Queue(pedidoResponseQueueName, true);
    }

    /**
     * Conversor para mensagens JSON (RabbitMQ <-> Objetos Java)
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
