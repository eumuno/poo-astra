# Segurança do Cinema Astra: modelo de ameaças (STRIDE)

Este documento descreve **o que pode dar errado do ponto de vista de segurança** no Cinema Astra, **o que o
sistema faz a respeito** e **o que ficou como risco assumido**. Ele usa o STRIDE, uma lista de seis categorias de
ameaça (Spoofing, Tampering, Repudiation, Information disclosure, Denial of service, Elevation of privilege).

Tudo o que está marcado como defesa implementada existe no código e é exercitado por um teste automático
(`src/teste/TesteSeguranca.java`, com 92 verificações). Onde o sistema **não** protege, isso está dito.

## 1. Escopo

O Astra é uma aplicação **desktop** (Java Swing) com banco **SQLite local**. Não há servidor nem rede, e o
pagamento é simulado.

| | |
|---|---|
| **Atacantes considerados** | Um cliente mal-intencionado usando a interface; alguém digitando textos malformados ou gigantes; alguém tentando adivinhar a senha de outra pessoa; alguém com acesso de leitura ao arquivo `astra.db` na mesma máquina |
| **Fora do escopo** | Quem controla o processo Java ou o computador (malware, depurador, alteração do código); engenharia social; ataques de rede (não existe rede); fraude de pagamento (não há pagamento real) |

## 2. O que precisa ser protegido e por onde os dados passam

| Ativo | Por que importa |
|---|---|
| Contas e senhas | Quem controla uma conta age em nome da pessoa |
| Dados pessoais (nome, e-mail, CPF) | São dados pessoais protegidos por lei (LGPD) |
| Preços, estoque e sessões | Adulterá-los dá prejuízo ou venda indevida |
| Pedidos e ingressos | Representam dinheiro e direito de entrada |
| Trilha de auditoria | É a prova de quem fez o quê |

```
 Pessoa ──► Interface gráfica ──► Serviços (regras) ──► Repositórios ──► Arquivo astra.db ◄── Sistema operacional
            (telas Swing)         (autorização,         (SQL parametrizado,   (restrições do        (permissões do
                                   validação,            transações)           banco)                arquivo)
                                   auditoria)
              ▲ fronteira 1          ▲ fronteira 2           ▲ fronteira 3                            ▲ fronteira 4
```

As regras de segurança ficam nos **serviços**, e não na tela: a tela pode ser contornada, o serviço não. As quatro
fronteiras são os pontos em que algo vindo de fora precisa ser tratado como não confiável.

## 3. Análise por categoria

### S: Spoofing (fingir ser outra pessoa)

| Ameaça | Defesa implementada | Onde | Evidência |
|---|---|---|---|
| Quem rouba o arquivo do banco descobre as senhas | Senha guardada com **PBKDF2-HMAC-SHA256**, **sal aleatório de 16 bytes** por senha e **600.000 iterações**. Duas contas com a mesma senha têm hashes diferentes | `CriptografiaSenha` | `TesteSeguranca`, seção S |
| Contas criadas antes ficaram com SHA-256 sem sal | Continuam entrando, e o hash é **trocado pelo formato novo no primeiro login** | `UsuarioRepositorioJdbc.autenticar` | Teste da conta antiga |
| Adivinhar senhas por tentativa e erro | Depois de **5 senhas erradas seguidas** para o mesmo e-mail, o login é recusado por **30 segundos**, inclusive com a senha certa. Acerto zera a contagem. Vale também trocando maiúsculas e minúsculas do e-mail | `ContaService` | Teste com relógio controlado |
| Descobrir quais e-mails existem pelo login | A resposta é a mesma para "e-mail inexistente" e "senha errada" | `ContaService`, `UsuarioRepositorio` | Teste S |
| Comparação de hash revelar a senha pelo tempo de resposta | Comparação em tempo constante (`MessageDigest.isEqual`) | `CriptografiaSenha` | Revisão de código |
| Entrar como administrador com uma conta de cliente | A tela confere o perfil da conta autenticada e recusa o perfil errado | `AstraFrame` | Robô de interface |

**Risco residual:** as contas de demonstração (`admin@astra` e `cliente@email.com`) têm senha `123`. É
proposital para a demonstração e **precisa ser trocado** em qualquer uso real. O bloqueio é guardado em memória e
zera ao reiniciar o programa. PBKDF2 é aceitável, mas Argon2 ou bcrypt seriam preferíveis num sistema novo.

### T: Tampering (alterar o que não devia)

| Ameaça | Defesa implementada | Onde | Evidência |
|---|---|---|---|
| **Injeção de SQL** em login, nome, título de filme, nome de produto | **Todo SQL usa `PreparedStatement`**: o valor digitado nunca entra no texto do comando. A única concatenação em SQL do projeto é uma constante com nomes de colunas | Classes `...Jdbc` | Teste com `' OR '1'='1' --` e `'); DROP TABLE ...; --`: os textos são gravados como texto e as 11 tabelas continuam intactas |
| Gravar dados absurdos direto no banco | O banco recusa: estoque e preço negativos (`CHECK`), pedido de cliente inexistente (chave estrangeira), status inventado (`CHECK`) | `schema.sql` | Teste T |
| Vender o mesmo assento duas vezes | `UNIQUE (sessao_id, assento_id)` em `ingresso`, mais conferência antes de cobrar | `PedidoRepositorioJdbc` | `TesteVenda` |
| Vender estoque que não existe (duas janelas) | `UPDATE ... WHERE estoque >= ?` numa única instrução, dentro da transação da venda | `PedidoRepositorioJdbc` | `TesteVenda` |
| Cobrar um valor diferente do correto | O preço do ingresso vem da **sessão no banco**, e o do produto é fixado no item no momento da compra. A tela não envia valores | `Ingresso`, `ItemPedido` | `TesteVenda` |
| Pagamento aprovado e venda não gravada | Venda desfeita: pagamento estornado, pedido volta a aberto, e o fato é registrado na auditoria | `VendaService` | `TesteFalhaCompra`, `TesteSeguranca` |
| Mudar o preço ou o horário de uma sessão depois de vendida | O serviço recusa editar ou cancelar sessão com ingresso vendido | `SessaoService` | `TesteAdmin` |

**Risco residual:** quem tem **acesso de escrita ao arquivo `astra.db`** fora do sistema (um editor de SQLite)
pode alterar dados, inclusive a trilha de auditoria. As restrições do banco limitam o estrago, mas não impedem
tudo. A proteção real é a permissão do arquivo (seção I) e, num sistema maior, um servidor de banco com contas
separadas.

### R: Repudiation (negar que fez algo)

| Ameaça | Defesa implementada | Onde | Evidência |
|---|---|---|---|
| "Não fui eu que cancelei a sessão / mudei o preço / repus o estoque" | **Trilha de auditoria** (tabela `auditoria`): quem, quando, qual ação e o detalhe. Registra logins (certos, errados e bloqueados), criação de conta, toda alteração administrativa, compras concluídas e vendas desfeitas | `TrilhaDeAuditoria`, `AuditoriaRepositorioJdbc` | `TesteSeguranca`, seção R |
| Alguém forjar linhas no registro digitando uma quebra de linha | Caracteres de controle viram espaço e o texto é cortado em 500 caracteres antes de gravar | `TrilhaDeAuditoria` | Teste com `\n` |
| A trilha conter senhas | Nenhuma chamada passa senha, e o teste confere todos os registros | Serviços | Teste R |
| Alterar ou apagar registros pelo sistema | A interface do repositório só permite **acrescentar e consultar** | `AuditoriaRepositorio` | Teste por reflexão |
| Qualquer um ler a trilha | Só administradores consultam (aba **Auditoria**), conferido no banco | `TrilhaDeAuditoria` | Teste E |

**Risco residual:** gravar a auditoria é *best effort*: se falhar, a operação continua e o erro vai para o console
(prioriza disponibilidade; num sistema crítico a operação deveria ser cancelada). A trilha não tem assinatura
nem encadeamento, então quem edita o arquivo do banco consegue apagá-la. Tentativas de acesso negado (administrador
forjado) lançam exceção, mas não são registradas.

### I: Information disclosure (vazar o que é privado)

| Ameaça | Defesa implementada | Onde | Evidência |
|---|---|---|---|
| Senha em texto puro no banco ou na memória do objeto | Só o hash é gravado. Nenhuma classe de usuário tem método que devolva senha ou hash, e o `toString` não os mostra | `Usuario`, `CriptografiaSenha` | Teste I |
| Outro usuário da mesma máquina ler o arquivo do banco | Ao abrir, o arquivo fica **legível só pelo dono** (`rw-------`). Um banco que estava aberto (`rw-r--r--`) é fechado | `ConexaoBanco` | Teste I (em Linux e macOS) |
| Mensagens de erro revelarem o caminho do banco ou SQL | A mensagem que chega à tela não traz o caminho do arquivo, o texto do SQL nem o nome da restrição. O detalhe técnico fica na causa da exceção e vai para o console | `ConexaoBanco`, repositórios, `TelaCliente` | Teste I |
| Um cliente ver os pedidos de outros | O histórico é pedido pelo id do cliente logado, e a lista geral de pedidos exige administrador, conferido no banco | `PedidoService` | Teste E |
| A aba de auditoria expor dados a não administradores | Exige administrador | `TrilhaDeAuditoria` | Teste E |

**Risco residual:** o banco **não é criptografado** (CPFs e e-mails ficam legíveis para quem copiar o arquivo e
tiver permissão). No Windows a proteção do arquivo depende das permissões da pasta do usuário, porque o sistema
não aplica as permissões POSIX. O cadastro informa se um e-mail ou CPF já existe, o que permite descobrir contas
(escolha de usabilidade). A senha digitada passa pela memória como `String`.

### D: Denial of service (deixar o sistema lento ou parado)

| Ameaça | Defesa implementada | Onde | Evidência |
|---|---|---|---|
| Textos gigantes que enchem o banco ou travam a tela | **Limite de tamanho em cada campo**: nome 100, e-mail 254, senha 128, título 150, gênero 60, diretor 100, sinopse 2.000, produto 100, descrição 300 | Serviços (`Validacao`) | Teste D, no limite e um acima |
| Senha enorme para tornar o login lento (o hash é caro de propósito) | Login com e-mail ou senha acima do limite é recusado **sem consultar o banco nem calcular o hash** | `ContaService` | Teste D conta as chamadas ao banco |
| Força bruta sobre o login | Bloqueio de 30 s depois de 5 falhas (ver S) | `ContaService` | Teste S |
| Esvaziar o estoque com quantidades absurdas | Estoque inicial e reposição limitados a 100.000; o carrinho só aceita o que há em estoque | `ProdutoService`, `VendaService` | Teste D, `TesteCarrinho` |
| "Segurar" poltronas e estoque sem pagar | Um pedido em aberto **não reserva nada**: poltrona e estoque só mudam quando o pagamento é gravado | `VendaService` | `TesteVenda` |
| Consulta de auditoria devolvendo a tabela inteira | O limite da consulta é de 1 a 1.000 registros | `AuditoriaRepositorio` | Teste D |
| Operações demoradas congelando a janela | Consultas e gravações rodam fora da thread da interface | Telas (`SwingWorker`) | Robôs de interface |

**Risco residual:** o bloqueio de login pode ser **usado contra a própria vítima**: quem digita senhas erradas para o
e-mail dela a impede de entrar por 30 segundos. É uma troca consciente (o tempo curto limita o dano). O SQLite
aceita um escritor por vez, então uma operação muito longa pode atrasar outras.

### E: Elevation of privilege (ganhar poderes que não se tem)

| Ameaça | Defesa implementada | Onde | Evidência |
|---|---|---|---|
| Fazer uma operação de administrador sem ser administrador | Antes, os serviços só conferiam que o objeto `Administrador` **não era nulo**. Agora cada operação administrativa **consulta o banco** e exige que a conta exista e seja de administrador naquele momento | `ControleDeAcesso` | `TesteSeguranca`, seção E: administradores forjados (id de cliente, id inexistente, id 0, nulo) são recusados em **12 operações** e **nada muda no banco** |
| Um cliente usar funções de administrador pela interface | A tela do cliente conhece só a interface `CatalogoCliente`, que **não tem** nenhuma operação administrativa | `CatalogoCliente`, `AtendimentoCliente` | `TesteContaPedido` (por reflexão) |
| Comprar em nome de um administrador, ou de um id qualquer | O banco confere que o dono do pedido é um **cliente** existente | `PedidoRepositorioJdbc` | Teste E |
| Escalar privilégio por injeção de SQL | SQL parametrizado (ver T) | `...Jdbc` | Teste T |

**Risco residual:** o sistema **não tem sessões nem tokens**: a identidade de quem fez login é a que a tela
guarda em memória. O `ControleDeAcesso` barra identidades forjadas ou desatualizadas, mas um `Administrador` criado
em código com o **id de um administrador real** passa. Isso só importa para quem já controla o processo, que está
fora do escopo.

## 4. Resumo

| Categoria | Situação | Principal defesa | Principal risco que sobra |
|---|---|---|---|
| **S** Spoofing | Mitigada | PBKDF2 com sal e bloqueio de tentativas | Senha de demonstração `123` |
| **T** Tampering | Mitigada | SQL parametrizado e restrições do banco | Escrita direta no arquivo do banco |
| **R** Repudiation | Mitigada | Trilha de auditoria somente de acréscimo | Trilha editável por quem altera o arquivo |
| **I** Information disclosure | Parcial | Hash, permissão do arquivo, erros sem detalhes | Banco sem criptografia |
| **D** Denial of service | Mitigada | Limites de tamanho e bloqueio de login | Bloqueio usado contra a vítima |
| **E** Elevation of privilege | Mitigada | Administrador conferido no banco | Sem sessões ou tokens |

## 5. Trabalho futuro, por ordem de valor

1. Trocar as senhas de demonstração e exigir troca no primeiro acesso.
2. Criptografar o arquivo do banco (por exemplo, SQLCipher).
3. Introduzir sessões com identificador aleatório, em vez de confiar no objeto guardado pela tela.
4. Tornar a trilha à prova de adulteração (encadear cada registro ao anterior por hash) e registrar os acessos negados.
5. Migrar para Argon2 ou bcrypt e permitir recuperação de senha.
6. Validar os dígitos verificadores do CPF.

## 6. Como verificar

```bash
# compila e roda o teste de segurança, a partir da raiz do projeto
mkdir -p out
javac -encoding UTF-8 -cp lib/sqlite-jdbc-3.46.1.3.jar -d out $(find src -name '*.java')
cp -r src/astra/persistencia/sql out/astra/persistencia/
java -cp "out:lib/sqlite-jdbc-3.46.1.3.jar" teste.TesteSeguranca     # no Windows, troque ":" por ";"
```

Saída esperada: `TODOS OS 92 TESTES PASSARAM`. O teste está dividido em seis seções, uma para cada letra do STRIDE.

Para ver a trilha de auditoria, entre como administrador e abra a aba **Auditoria**, ou consulte o banco:

```sql
SELECT quando, usuario_id, acao, detalhe FROM auditoria ORDER BY id DESC LIMIT 50;
```

## 7. Compatibilidade

- A tabela `auditoria` é criada automaticamente em bancos existentes (`CREATE TABLE IF NOT EXISTS`); não é preciso apagar o `astra.db`.
- Contas com o hash antigo continuam funcionando e são atualizadas no primeiro login.
- A senha de uma conta não pode ser recuperada do hash, por isso a atualização só acontece no login.