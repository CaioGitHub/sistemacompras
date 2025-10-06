package org.example.services;

import org.example.models.ItemPedido;
import org.example.models.Pedido;
import org.example.producers.PedidoProducer;
import org.example.repositories.PedidoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class PedidoService {
    private static final String PRODUTO_SERVICE_URL = "http://localhost:8080/produtos/";

    private final PedidoRepository repository;
    private final RestTemplate restTemplate;
    private final PedidoProducer producer;

    public PedidoService(PedidoRepository repository, PedidoProducer producer) {
        this.repository = repository;
        this.producer = producer;
        this.restTemplate = new RestTemplate();
    }

    public Pedido criarPedido(Pedido pedido) {
        double valorTotal = 0.0;

        System.out.println("📦 Iniciando criação de novo pedido com " + pedido.getItens().size() + " itens...");

        for (var item : pedido.getItens()) {
            String url = PRODUTO_SERVICE_URL + item.getIdProduto();

            try {
                ResponseEntity<Map> resposta = restTemplate.getForEntity(url, Map.class);
                if (resposta.getStatusCode().is2xxSuccessful() && resposta.getBody() != null) {
                    Map<String, Object> produto = resposta.getBody();

                    Integer estoque = (Integer) produto.get("quantidadeEmEstoque");
                    Double preco = ((Number) produto.get("preco")).doubleValue();
                    String nome = (String) produto.get("nome");

                    if (estoque < item.getQuantidade()) {
                        throw new RuntimeException("❌ Estoque insuficiente para o produto '" + nome + "' (ID: " + item.getIdProduto() + ")");
                    }

                    double subtotal = preco * item.getQuantidade();
                    valorTotal += subtotal;

                    System.out.printf("🧾 Produto: %s | Quantidade: %d | Preço Unitário: %.2f | Subtotal: %.2f%n",
                            nome, item.getQuantidade(), preco, subtotal);

                } else {
                    throw new RuntimeException("❌ Produto não encontrado no serviço de produtos (ID: " + item.getIdProduto() + ")");
                }
            } catch (Exception e) {
                throw new RuntimeException("⚠️ Erro ao consultar produto no serviço de produtos: " + e.getMessage());
            }
        }

        pedido.setValorTotal(valorTotal);
        pedido.setStatus("PENDENTE");

        System.out.printf("💰 Valor total do pedido: R$ %.2f%n", valorTotal);

        Pedido salvo = repository.save(pedido);
        producer.enviarPedido(salvo);

        System.out.println("✅ Pedido salvo no banco e enviado à fila RabbitMQ (ID: " + salvo.getId() + ")");

        return salvo;
    }

    public Pedido buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    public List<Pedido> listarPedidos() {
        return repository.findAll();
    }
}