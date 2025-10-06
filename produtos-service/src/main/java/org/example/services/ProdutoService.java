package org.example.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.models.Pedido;
import org.example.models.Produto;
import org.example.repositories.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    // -----------------------------
    // CRUD
    // -----------------------------

    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

    public List<Produto> listar() {
        return repository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com id: " + id));
    }

    @Transactional
    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto produto = buscarPorId(id);
        produto.setNome(produtoAtualizado.getNome());
        produto.setPreco(produtoAtualizado.getPreco());
        produto.setQuantidadeEmEstoque(produtoAtualizado.getQuantidadeEmEstoque());
        return repository.save(produto);
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado para exclusão: " + id);
        }
        repository.deleteById(id);
    }

    // -----------------------------
    // ESTOQUE / INTEGRAÇÃO PEDIDOS
    // -----------------------------

    /**
     * Reserva estoque de forma segura (testável via endpoint).
     * Retorna true se conseguiu reservar, false se estoque for insuficiente.
     */
    @Transactional
    public boolean reservarEstoque(Long id, Integer quantidade) {
        Produto produto = buscarPorId(id);

        if (produto.getQuantidadeEmEstoque() < quantidade) {
            System.out.println("⚠️ Estoque insuficiente para o produto: " + produto.getNome());
            return false;
        }

        produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() - quantidade);
        repository.save(produto);

        System.out.println("✅ Estoque reservado para produto: " + produto.getNome() +
                " | Quantidade reservada: " + quantidade +
                " | Estoque restante: " + produto.getQuantidadeEmEstoque());
        return true;
    }

    /**
     * Atualiza o estoque a partir de um Pedido recebido via RabbitMQ.
     * Essa função será chamada pelo consumidor (RabbitListener).
     */
    @Transactional
    public void atualizarEstoque(Pedido pedido) {
        System.out.println("📦 Processando pedido ID: " + pedido.getId());

        pedido.getItens().forEach(item -> {
            Produto produto = repository.findById(item.getIdProduto())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + item.getIdProduto()));

            if (produto.getQuantidadeEmEstoque() >= item.getQuantidade()) {
                produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() - item.getQuantidade());
                repository.save(produto);
                System.out.println("✅ Estoque atualizado: " + produto.getNome() +
                        " (-" + item.getQuantidade() + ")");
            } else {
                System.out.println("❌ Falha ao atualizar estoque: produto " +
                        produto.getNome() + " não possui quantidade suficiente.");
            }
        });
    }
}
