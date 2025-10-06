package org.example.consumers;

import org.example.models.Pedido;
import org.example.services.ProdutoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PedidoConsumer {

    private final ProdutoService produtoService;

    public PedidoConsumer(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /**
     * Ouve a fila configurada em application.properties (pedido.queue.name)
     * Sempre que um novo Pedido for enviado pelo microsserviço de Pedidos,
     * este método será acionado automaticamente pelo RabbitMQ.
     */
    @RabbitListener(queues = "${pedido.queue.name}")
    public void receberPedido(@Payload Pedido pedido) {
        System.out.println("📩 [RabbitMQ] Pedido recebido: " + pedido);
        try {
            produtoService.atualizarEstoque(pedido);
            System.out.println("✅ Estoque atualizado com sucesso para o pedido ID: " + pedido.getId());
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar pedido " + pedido.getId() + ": " + e.getMessage());
        }
    }
}
