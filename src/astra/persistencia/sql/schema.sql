-- =====================================================================
-- Astra: criação das tabelas (SQLite)
-- Executado por ConexaoBanco.inicializar() toda vez que o sistema abre.
-- "IF NOT EXISTS" faz com que rodar de novo não apague nada.
--
-- Convenções:
--   * ids são INTEGER PRIMARY KEY AUTOINCREMENT: quem gera o id é o banco.
--   * enums são gravados como TEXT com o name() do enum Java
--     (ex.: Sessao.statusSessao.AGENDADA -> 'AGENDADA'); o CHECK impede
--     valores que não existem no enum.
--   * datas são TEXT no formato ISO-8601 de LocalDateTime.toString()
--     (ex.: '2026-11-27T19:00'), lidas de volta com LocalDateTime.parse().
--   * booleanos são INTEGER 0/1.
--   * dinheiro é REAL, como o double usado nas classes.
-- =====================================================================

-- Usuario (abstrata) -> uma tabela só com a coluna "tipo"
-- (herança "single table"). cpf só é preenchido para CLIENTE.
CREATE TABLE IF NOT EXISTS usuario (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo       TEXT    NOT NULL CHECK (tipo IN ('CLIENTE', 'ADMINISTRADOR')),
    nome       TEXT    NOT NULL,
    email      TEXT    NOT NULL UNIQUE,
    senha_hash TEXT    NOT NULL,          -- SHA-256 em hexadecimal, nunca a senha pura
    cpf        TEXT,
    CHECK (tipo = 'ADMINISTRADOR' OR cpf IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS filme (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo          TEXT    NOT NULL,
    duracao_minutos INTEGER NOT NULL CHECK (duracao_minutos > 0),
    classificacao   TEXT    NOT NULL CHECK (classificacao IN
                        ('LIVRE', 'DEZ', 'DOZE', 'QUATORZE', 'DEZESSEIS', 'DEZOITO')),
    genero          TEXT,
    diretor         TEXT,
    ativo           INTEGER NOT NULL DEFAULT 1 CHECK (ativo IN (0, 1)),
    dublado         INTEGER NOT NULL DEFAULT 0 CHECK (dublado IN (0, 1)),
    sinopse         TEXT
);

CREATE TABLE IF NOT EXISTS sala (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    numero INTEGER NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS assento (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    sala_id INTEGER NOT NULL REFERENCES sala (id),
    numero  INTEGER NOT NULL CHECK (numero > 0),
    tipo    TEXT    NOT NULL CHECK (tipo IN ('COMUM', 'PREFERENCIAL')),
    UNIQUE (sala_id, numero)               -- não existem dois assentos 5 na mesma sala
);

CREATE TABLE IF NOT EXISTS sessao (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    filme_id   INTEGER NOT NULL REFERENCES filme (id),
    sala_id    INTEGER NOT NULL REFERENCES sala (id),
    data_hora  TEXT    NOT NULL,
    status     TEXT    NOT NULL CHECK (status IN ('AGENDADA', 'ENCERRADA', 'CANCELADA')),
    preco_base REAL    NOT NULL CHECK (preco_base >= 0),   -- substitui o valorReferencia fixo em 15
    UNIQUE (sala_id, data_hora)            -- uma sala não exibe duas sessões no mesmo horário
);

CREATE TABLE IF NOT EXISTS produto (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    nome      TEXT    NOT NULL,
    descricao TEXT,
    preco     REAL    NOT NULL CHECK (preco >= 0),
    estoque   INTEGER NOT NULL CHECK (estoque >= 0),       -- o banco recusa estoque negativo
    categoria TEXT    NOT NULL CHECK (categoria IN ('PIPOCA', 'BEBIDA', 'DOCE'))
);

CREATE TABLE IF NOT EXISTS pedido (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id INTEGER NOT NULL REFERENCES usuario (id),
    data       TEXT    NOT NULL,
    status     TEXT    NOT NULL CHECK (status IN ('ABERTO', 'PAGO', 'PRONTO', 'RETIRADO', 'CANCELADO'))
);

CREATE TABLE IF NOT EXISTS item_pedido (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    pedido_id      INTEGER NOT NULL REFERENCES pedido (id) ON DELETE CASCADE,
    produto_id     INTEGER NOT NULL REFERENCES produto (id),
    quantidade     INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario REAL    NOT NULL CHECK (preco_unitario >= 0)  -- preço no momento da compra
);

CREATE TABLE IF NOT EXISTS ingresso (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    pedido_id  INTEGER NOT NULL REFERENCES pedido (id) ON DELETE CASCADE,
    sessao_id  INTEGER NOT NULL REFERENCES sessao (id),
    assento_id INTEGER NOT NULL REFERENCES assento (id),
    tipo       TEXT    NOT NULL CHECK (tipo IN ('INTEIRA', 'MEIA', 'CORTESIA')),
    valor_pago REAL    NOT NULL CHECK (valor_pago >= 0),
    UNIQUE (sessao_id, assento_id)         -- o próprio banco impede vender o mesmo assento duas vezes
);

CREATE TABLE IF NOT EXISTS pagamento (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    pedido_id INTEGER NOT NULL REFERENCES pedido (id) ON DELETE CASCADE,
    forma     TEXT    NOT NULL CHECK (forma IN ('PIX', 'CARTAO')),
    valor     REAL    NOT NULL CHECK (valor >= 0),
    data      TEXT    NOT NULL,
    status    TEXT    NOT NULL CHECK (status IN ('PENDENTE', 'APROVADO', 'RECUSADO'))
);

-- Índices para as consultas mais comuns (assentos ocupados de uma sessão,
-- sessões de um filme, pedidos de um cliente).
CREATE INDEX IF NOT EXISTS idx_ingresso_sessao ON ingresso (sessao_id);
CREATE INDEX IF NOT EXISTS idx_sessao_filme    ON sessao (filme_id);
CREATE INDEX IF NOT EXISTS idx_pedido_cliente  ON pedido (cliente_id);
