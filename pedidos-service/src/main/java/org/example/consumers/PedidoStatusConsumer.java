package org.example.consumers;

import org.example.models.PedidoStatusResponse;
import org.example.repositories.PedidoRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoStatusConsumer {

    private final PedidoRepository repository;

    public PedidoStatusConsumer(PedidoRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "${pedido.queue.response}")
    public void receberStatus(PedidoStatusResponse response) {
        System.out.println("📨 Resposta recebida do estoque: " + response);

        repository.findById(response.getPedidoId()).ifPresent(pedido -> {
            pedido.setStatus(response.getStatus());
            repository.save(pedido);
            System.out.println("🟢 Pedido atualizado no banco com status: " + response.getStatus());
        });
    }
}