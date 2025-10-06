
# 🛒 Sistema de Compras Online — Microsserviços com Spring Boot, RabbitMQ e H2

Este projeto implementa um **sistema de compras online** dividido em **dois microsserviços** principais:  
um responsável pelos **produtos** e outro responsável pelos **pedidos**.

A comunicação entre os serviços é feita de forma **assíncrona via RabbitMQ**, garantindo desacoplamento e escalabilidade.

---

## 🚀 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.3.4**
  - Spring Web
  - Spring Data JPA
  - Spring AMQP (RabbitMQ)
  - H2 Database
- **RabbitMQ (CloudAMQP)**
- **Lombok**
- **Maven**

---

## 🧩 Estrutura do Projeto

``` 
sistemacompras/
│
├── pedidos-service/
│ ├── src/main/java/org/example/
│ │ ├── configs/ → Configuração do RabbitMQ
│ │ ├── consumers/ → Recebe respostas do serviço de produtos
│ │ ├── controllers/ → Endpoints REST de pedidos
│ │ ├── models/ → Entidades: Pedido, ItemPedido, PedidoStatusResponse
│ │ ├── producers/ → Envia pedidos para a fila RabbitMQ
│ │ ├── repositories/ → Acesso JPA (PedidoRepository)
│ │ └── services/ → Regras de negócio e integração com Produtos
│ └── resources/
│ └── application.properties
│
├── produtos-service/
│ ├── src/main/java/org/example/
│ │ ├── configs/ → Configuração do RabbitMQ
│ │ ├── consumers/ → Recebe pedidos e atualiza estoque
│ │ ├── controllers/ → Endpoints REST de produtos
│ │ ├── models/ → Entidades Produto e Pedido
│ │ ├── producers/ → Envia status do pedido de volta
│ │ ├── repositories/ → Acesso JPA (ProdutoRepository)
│ │ └── services/ → Lógica de estoque e integração com Pedidos
│ └── resources/
│ ├── application.properties
│ └── data.sql → Script de inicialização com produtos de exemplo
│
└── README.md
```

---

## ⚙️ Fluxo de Comunicação entre os Microsserviços

```text
Cliente → (POST) /pedidos → Pedidos-Service
           ↓
       [RabbitMQ: pedidos.fila]
           ↓
     Produtos-Service consome
           ↓
 Atualiza estoque e envia resposta
           ↓
       [RabbitMQ: pedidos.resposta]
           ↓
 Pedidos-Service consome e atualiza status
```
---

🟧 Pedidos-Service (localhost:8081)

* Criar novo pedido (POST /pedidos)

* Listar todos os pedidos (GET /pedidos)

* Buscar pedido por ID (GET /pedidos/{id})

* Integração automática com o serviço de produtos:

    * Consulta estoque via REST

    * Envia pedido ao RabbitMQ

    * Atualiza status conforme resposta do estoque

---

🧪 Como Testar o Projeto
✅ Pré-requisitos
* Conta no CloudAMQP
* Java 17 instalado
* Maven configurado
* IDE ou terminal

---

🧰 1. Configure o RabbitMQ

Acesse seu painel CloudAMQP e copie a URL de conexão (exemplo):
```bash
amqps://usuario:senha@servidor.cloudamqp.com/vhost
```
Cole essa URL no application.properties de ambos os serviços:
```bash
spring.rabbitmq.addresses=amqps://usuario:senha@servidor.cloudamqp.com/vhost
```
As filas são criadas automaticamente ao iniciar os projetos:
* pedidos.fila
* pedidos.resposta

---

▶️ 2. Rode os dois microsserviços

No IntelliJ ou terminal:
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

---

🧭 3. Teste as requisições no Postman ou Insomnia
🟧 Criar Pedido

POST http://localhost:8081/pedidos

```json
{
  "itens": [
    { "idProduto": 1, "quantidade": 2 },
    { "idProduto": 2, "quantidade": 1 }
  ]
}
```
🔹 O pedido é salvo no banco H2 e enviado para a fila RabbitMQ
🔹 O produtos-service consome, atualiza estoque e devolve o status
🔹 O pedidos-service atualiza o pedido com status = CONFIRMADO

---

🟦 Listar Pedidos

GET http://localhost:8081/pedidos

```json
[
  {
    "id": 1,
    "itens": [
      { "idProduto": 1, "quantidade": 2 }
    ],
    "valorTotal": 8520.00,
    "status": "CONFIRMADO"
  }
]
```

--- 

🧩 4. Acessar os bancos de dados (H2)

Cada microsserviço usa um banco **em memória (H2)** independente:

| Serviço           | URL do Console H2                  | JDBC URL                  |
|-------------------|------------------------------------|---------------------------|
| produtos-service  | `http://localhost:8080/h2-console` | `jdbc:h2:mem:produtosdb`  |
| pedidos-service   | `http://localhost:8081/h2-console` | `jdbc:h2:mem:pedidosdb`   |

Usuário: `sa`  
Senha: *(vazio)*

Credenciais:
```yaml
JDBC URL: jdbc:h2:mem:produtosdb  (ou pedidosdb)
User: sa
Password:
```

--- 

🧱 Arquitetura

* Comunicação entre microsserviços: RabbitMQ (mensageria)
* Banco de dados isolado por serviço: H2
* Escalável e assíncrono
* Baseado em eventos → o serviço de pedidos não depende diretamente do estoque

---

📜 Licença

Este projeto é de uso educacional, desenvolvido para fins de estudo sobre microsserviços com Spring Boot e RabbitMQ.
Sinta-se livre para clonar e adaptar o código.

---

👨‍💻 Autor

Caio Victor

📘 Projeto criado como exercício de integração entre microsserviços — Sistema de Compras Online

