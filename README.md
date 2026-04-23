# 📦 Cadastro de Produtos — JavaFX + SceneBuilder + MySQL

Projeto de **CRUD de Produtos** com interface moderna usando **JavaFX**, layout criado com **SceneBuilder** (FXML), e persistência de dados com **MySQL**.

---

## 📁 Estrutura do Projeto

```
cadastro-produtos/
├── database.sql                    ← Script do banco de dados
├── README.md                       ← Este arquivo
└── src/
    ├── application/
    │   └── App.java                ← Classe principal (main)
    ├── controller/
    │   └── ProdutoController.java  ← Controller da tela FXML
    ├── dao/
    │   ├── ConexaoDB.java          ← Conexão com MySQL
    │   └── ProdutoDAO.java         ← Operações CRUD no banco
    ├── model/
    │   └── Produto.java            ← Modelo de dados
    └── view/
        ├── ProdutoView.fxml        ← Layout (editável no SceneBuilder)
        └── styles.css              ← Tema dark premium
```

---

## 🚀 Como Configurar no Eclipse

### 1. Banco de Dados MySQL

1. Abra o MySQL Workbench (ou terminal do MySQL)
2. Execute o script `database.sql`:
   ```sql
   source C:/caminho/para/cadastro-produtos/database.sql
   ```
3. Isso cria o banco `cadastro_produtos` com a tabela e dados de exemplo

### 2. Importar no Eclipse

1. **File → Import → General → Existing Projects into Workspace**
2. Selecione a pasta `cadastro-produtos`
3. Se o Eclipse não reconhecer como projeto, crie um novo projeto Java e copie a pasta `src` para dentro

### 3. Configurar o Build Path

Adicione os seguintes JARs ao **Build Path** (botão direito no projeto → Build Path → Add External JARs):

#### JavaFX SDK
- Baixe em: [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)
- Adicione todos os JARs da pasta `lib` do JavaFX SDK

#### MySQL Connector/J
- Baixe em: [https://dev.mysql.com/downloads/connector/j/](https://dev.mysql.com/downloads/connector/j/)
- Adicione o `mysql-connector-j-X.X.X.jar`

### 4. Configurar VM Arguments para JavaFX

Na configuração de execução (**Run → Run Configurations → Arguments → VM Arguments**):

```
--module-path "C:/caminho/para/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
```

### 5. Configurar Conexão do Banco

Se sua senha do MySQL **não for vazia**, edite o arquivo `src/dao/ConexaoDB.java`:

```java
private static final String SENHA = "sua_senha_aqui";
```

### 6. Executar

- Botão direito em `App.java` → **Run As → Java Application**

---

## 🎨 Editar no SceneBuilder

1. Abra o **SceneBuilder**
2. **File → Open** e selecione `src/view/ProdutoView.fxml`
3. Edite visualmente os componentes
4. Salve e as mudanças refletem automaticamente no Eclipse

---

## ✨ Funcionalidades

| Função | Descrição |
|--------|-----------|
| ➕ Adicionar | Cadastra novo produto no banco |
| ✏️ Editar | Clique na tabela e altere os dados |
| 🗑️ Excluir | Remove com confirmação |
| 🔍 Buscar | Filtra produtos por nome |
| 🔄 Limpar | Limpa o formulário |
| ✅ Validação | Campos obrigatórios validados |
| 📊 Contagem | Mostra total de produtos |

---

## 🎨 Visual

- **Tema Dark Premium** com gradientes roxo/azul
- **Glassmorphism** nos painéis
- **Efeitos hover** nos botões
- **Responsive** — redimensionável
- **Emojis** nos botões para melhor UX
