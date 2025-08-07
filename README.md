# 🚁 Sistema de Entrega com Drones

Este projeto simula um sistema de entregas com drones, incluindo regras de validação de carga, alcance, prioridade de pedidos, controle de status e um painel web para visualização.

---

## 🛠 Tecnologias Utilizadas

### Backend
- Java 17
- Spring Boot
- Maven
- JUnit
- Lombok

### Frontend
- HTML5
- CSS3
- JavaScript (Vanilla)

---

## ▶️ Como Executar

### Pré-requisitos

- Java 17 instalado
- Maven instalado

### Passos

1. **Clone o repositório:**

```bash
git clone https://github.com/Airesp4/drone-delivery-simulator-dti.git
cd drone-delivery-simulator-dti
```

2. **Execute o backend:**

Com Maven wrapper:

```bash
./mvnw spring-boot:run
```

Ou com Maven instalado globalmente:

```bash
mvn spring-boot:run
```

A aplicação será iniciada em:  
👉 `http://localhost:8080`

3. **Acesse o dashboard:**

Abra o navegador e vá para:  
👉 `http://localhost:8080`

> A interface web estará disponível diretamente na raiz da aplicação.

---

## ✨ Funcionalidades

- Cadastro de pedidos com:
  - Peso da carga
  - Prioridade
  - Coordenadas do cliente
- Validação automática de:
  - Capacidade de carga dos drones
  - Alcance de voo (ida e volta)
- Geração de pedidos como:
  - `PENDING`, quando entregável
  - `RECUSED`, quando nenhum drone é capaz de realizar a entrega
- Painel com:
  - Lista de pedidos
  - Envio em massa
  - Estatísticas visuais

---

### 🧠 Decisões Técnicas

- A **distância** é calculada como a diferença entre dois pontos em um plano cartesiano, utilizando a fórmula da distância euclidiana.
- Considera-se que **1 unidade de distância = 1km**.
- A **persistência dos dados** é feita **em memória**, utilizando `Map` para simular um banco de dados temporário.
- **Threads** são utilizadas para simular o comportamento de drones realizando entregas em paralelo.
- A **tentativa de alocação de pedidos** aos drones ocorre de forma periódica (com intervalo configurável) por meio de uma **tarefa agendada**.
- Os **pedidos recusados (RECUSED)** são salvos para análise posterior, mesmo quando não há drones aptos no momento da solicitação.
- Drones **não precisam estar disponíveis no momento da validação**, pois a regra considera a **capacidade teórica de entrega**.

### 💬 Prompts Utilizados

Durante o desenvolvimento, os seguintes prompts foram usados com auxílio da IA:

- "Explique a regra de validação para drones considerando peso e distância."
- "Quais opções para persistir em memória e não perder a referência de objetos."
- "Me mostre técnicas mais utilizadas para lidar com a concorrência em alocações e usos dos recursos do sistema."

### ✅ Conclusão

Este projeto simula de forma eficiente um sistema de entregas com drones, priorizando clareza, modularidade e realismo dentro de uma arquitetura simples. A implementação com persistência em memória e uso de threads para simular entregas paralelas permite testar a lógica de negócios sem a necessidade de infraestrutura externa. 

O sistema pode ser facilmente estendido para utilizar bancos de dados reais, autenticação de usuários e escalabilidade com microsserviços. As decisões técnicas foram pensadas para manter o equilíbrio entre didática e funcionalidade.

