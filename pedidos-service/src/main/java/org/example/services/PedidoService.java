package org.example.services;

import org.example.models.ItemPedido;
import org.example.models.Pedido;
import org.example.repositories.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final RestTemplate restTemplate;

    public PedidoService(PedidoRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
    }

    public Pedido criarPedido(Pedido pedido) {
        double total = 0.0;
        boolean sucesso = true;

        for (ItemPedido item : pedido.getItens()) {
            try {
                // Chama o microsserviço de Produtos para reservar estoque
                String url = "http://localhost:8080/produtos/"
                        + item.getIdProduto()
                        + "/reservar?quantidade="
                        + item.getQuantidade();

                restTemplate.postForEntity(url, null, String.class);

                // Aqui você poderia também buscar o preço do produto para calcular o total
                // Exemplo (opcional): total += preco * item.getQuantidade();

            } catch (Exception e) {
                sucesso = false;
                break;
            }
        }

        if (sucesso) {
            pedido.setValorTotal(total);
            pedido.setStatus("CONFIRMADO");
        } else {
            pedido.setStatus("CANCELADO");
        }

        return repository.save(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }
}