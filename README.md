# 🛒 Sistema de Compras Online — Microsserviços com Spring Boot, RabbitMQ e H2

> Plataforma exemplo que ilustra como **pedidos** e **produtos** se conversam de forma assíncrona.
> O objetivo é demonstrar uma arquitetura enxuta de microsserviços para um e-commerce.

---

## 🚀 Tecnologias Utilizadas

| Camada | Ferramentas e Versões | Por que importa |
| --- | --- | --- |
| Linguagem | **Java 17** | Alinha-se ao ecossistema atual do Spring e permite recursos modernos (records, pattern matching). |
| Framework | **Spring Boot 3.3.4** com *Web*, *Data JPA*, *AMQP* | Simplifica a criação de APIs REST, persistência e mensageria. |
| Mensageria | **RabbitMQ (CloudAMQP)** | Viabiliza a comunicação event-driven entre os serviços. |
| Banco de Dados | **H2** (em memória) | Inicializa rápido e permite testes isolados para cada serviço. |
| Produtividade | **Lombok**, **Maven** | Reduz boilerplate e facilita o build. |

> 🧠 Ambos os serviços expõem APIs REST e utilizam H2; a troca de mensagens é sempre feita via RabbitMQ.

---

## 🧩 Estrutura do Projeto

```bash
sistemacompras/
├── pedidos-service/
│   ├── configs/        # Beans do RabbitMQ (filas, exchanges, bindings)
│   ├── consumers/      # Consome respostas de produtos para atualizar pedidos
│   ├── controllers/    # Endpoints REST (POST/GET de pedidos)
│   ├── models/         # Pedido, ItemPedido e DTOs de status
│   ├── producers/      # Publica eventos do pedido em RabbitMQ
│   ├── repositories/   # Spring Data JPA (PedidoRepository)
│   └── services/       # Orquestra fluxo: salvar, publicar, reagir à resposta
│
├── produtos-service/
│   ├── configs/        # Configuração RabbitMQ compartilhada
│   ├── consumers/      # Recebe pedidos, checa estoque e devolve status
│   ├── controllers/    # CRUD de produtos e endpoint para reserva manual
│   ├── models/         # Produto e payloads consumidos da fila
│   ├── producers/      # Publica status do processamento do pedido
│   ├── repositories/   # Spring Data JPA (ProdutoRepository)
│   ├── services/       # Lógica de estoque e integração com pedidos
│   └── resources/
│       └── data.sql    # 5 produtos iniciais para testes rápidos
│
└── README.md
```

> ✨ A presença do `data.sql` garante que o serviço de produtos já inicie com itens como **Notebook Dell Inspiron** e **Mouse Logitech M170** disponíveis para consulta.

---

## ⚙️ Fluxo de Comunicação entre os Microsserviços

```mermaid
sequenceDiagram
    participant Cliente
    participant Pedidos as Pedidos-Service
    participant Broker as RabbitMQ
    participant Produtos as Produtos-Service

    Cliente->>Pedidos: POST /pedidos
    Pedidos->>Broker: Evento "pedidos.fila"
    Broker-->>Produtos: Mensagem para validação de estoque
    Produtos->>Produtos: Reserva/baixa de estoque
    Produtos->>Broker: Evento "pedidos.resposta"
    Broker-->>Pedidos: Atualiza status do pedido
    Pedidos-->>Cliente: Pedido confirmado ou recusado
```

> O serviço de pedidos nunca consulta o banco de produtos diretamente — ele reage somente à mensagem de retorno.

---

🟧 **Pedidos-Service (localhost:8081)**

| Tipo | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/pedidos` | Persiste o pedido, publica evento e retorna o registro com status inicial. |
| `GET` | `/pedidos` | Lista pedidos com itens, valores e status atual. |
| `GET` | `/pedidos/{id}` | Recupera um pedido específico. |

> 🔄 Internamente o `PedidoService` cria o pedido, envia para a fila `pedidos.fila` e aguarda a mensagem que atualiza o status para `CONFIRMADO` ou `RECUSADO`.

---

🧪 **Como Testar o Projeto**

### ✅ Pré-requisitos

- Conta no **CloudAMQP** (ou RabbitMQ compatível)
- **Java 17** e **Maven** instalados
- IDE ou terminal com suporte a múltiplas janelas

### 🧰 1. Configure o RabbitMQ

1. No painel do CloudAMQP, copie a URL completa, por exemplo: `amqps://usuario:senha@servidor.cloudamqp.com/vhost`.
2. Atualize a propriedade nos dois `application.properties`:
   ```properties
   spring.rabbitmq.addresses=amqps://usuario:senha@servidor.cloudamqp.com/vhost
   ```
3. Ao subir as aplicações, as filas abaixo são criadas automaticamente:
   - `pedidos.fila`
   - `pedidos.resposta`

### ▶️ 2. Rode os dois microsserviços

```bash
# Terminal 1
cd produtos-service
mvn spring-boot:run
```

```bash
# Terminal 2
cd pedidos-service
mvn spring-boot:run
```

### 🧭 3. Teste as requisições no Postman, Insomnia ou HTTPie

> Antes de criar pedidos, consulte os produtos disponíveis (dados vindos do `data.sql`).

**🟩 Consultar Produtos**

```
GET http://localhost:8080/produtos
```

Resposta esperada:

```json
[
  { "id": 1, "nome": "Notebook Dell Inspiron", "preco": 4200.0, "quantidadeEmEstoque": 10 },
  { "id": 2, "nome": "Mouse Logitech M170", "preco": 120.0, "quantidadeEmEstoque": 50 },
  { "id": 3, "nome": "Teclado Mecânico Redragon", "preco": 310.0, "quantidadeEmEstoque": 30 },
  { "id": 4, "nome": "Monitor LG 24\"", "preco": 900.0, "quantidadeEmEstoque": 20 },
  { "id": 5, "nome": "Headset HyperX Cloud", "preco": 450.0, "quantidadeEmEstoque": 25 }
]
```

**🟧 Criar Pedido**

```
POST http://localhost:8081/pedidos
Content-Type: application/json
```

```json
{
  "itens": [
    { "idProduto": 1, "quantidade": 2 },
    { "idProduto": 4, "quantidade": 1 }
  ]
}
```

- O pedido é salvo no H2 do serviço de pedidos.
- A mensagem é enviada para `pedidos.fila` com os produtos informados.
- O produtos-service verifica se existem 2 unidades do **Notebook Dell Inspiron** e 1 do **Monitor LG 24"**.
- Havendo estoque, o status retorna como `CONFIRMADO`.

**🟦 Listar Pedidos**

```
GET http://localhost:8081/pedidos
```

```json
[
  {
    "id": 1,
    "itens": [
      { "idProduto": 1, "quantidade": 2 },
      { "idProduto": 4, "quantidade": 1 }
    ],
    "valorTotal": 9300.0,
    "status": "CONFIRMADO"
  }
]
```

> 💡 Ajuste os IDs ou quantidades para provocar cenários de falta de estoque e observar o status `RECUSADO`.

### 🧩 4. Acessar os bancos de dados (H2)

| Serviço | Console H2 | JDBC URL | Usuário | Senha |
| --- | --- | --- | --- | --- |
| produtos-service | `http://localhost:8080/h2-console` | `jdbc:h2:mem:produtosdb` | `sa` | *(vazio)* |
| pedidos-service | `http://localhost:8081/h2-console` | `jdbc:h2:mem:pedidosdb` | `sa` | *(vazio)* |

> Use o console para inspecionar as tabelas `produto`, `pedido` e `item_pedido` durante os testes.

---

🧱 **Arquitetura**

- Microsserviços independentes com bancos isolados (H2) e comunicação assíncrona.
- `PedidoService` coordena eventos de criação/atualização de pedidos.
- `ProdutoService` valida estoque, atualiza quantidade e devolve status pela fila.
- Facilmente adaptável para ambientes reais trocando H2 por um banco persistente e RabbitMQ local ou gerenciado.

---

📜 **Licença**

Projeto de uso educacional — fique à vontade para clonar, estudar e adaptar.

---

👨‍💻 **Autor**

Caio Victor — Exercício prático de microsserviços para um Sistema de Compras Online.
