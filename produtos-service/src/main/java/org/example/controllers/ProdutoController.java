package org.example.controllers;

import org.example.models.Produto;
import org.example.services.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    // -----------------------------
    // CRUD BÁSICO
    // -----------------------------

    @PostMapping
    public ResponseEntity<Produto> criar(@RequestBody Produto produto) {
        Produto novoProduto = service.salvar(produto);
        return ResponseEntity.ok(novoProduto);
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable("id") Long id, @RequestBody Produto produto) {
        return ResponseEntity.ok(service.atualizar(id, produto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------
    // ESTOQUE E INTEGRAÇÃO COM PEDIDOS
    // -----------------------------

    /**
     * Endpoint auxiliar para testar manualmente o fluxo de reserva de estoque.
     * Esse método simula o que o microsserviço de Pedidos faria via RabbitMQ.
     */
    @PostMapping("/{id}/reservar")
    public ResponseEntity<String> reservarEstoque(@PathVariable("id") Long id, @RequestParam("quantidade") Integer quantidade) {
        service.reservarEstoque(id, quantidade);
        return ResponseEntity.ok("Estoque reservado com sucesso!");
    }
}
