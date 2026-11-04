-- =====================================================================
-- Astra: dados iniciais (SQLite)
-- Executado por ConexaoBanco.inicializar() só quando o banco é novo
-- (tabela usuario vazia). São os mesmos dados que antes ficavam fixos
-- no construtor do CinemaService, mais os usuários que as telas criavam.
--
-- Senhas: SHA-256 de '123' (ver CriptografiaSenha.gerarHash).
-- Atenção: o leitor de scripts separa comandos por ';', então não use
-- ponto e vírgula dentro de textos neste arquivo.
-- =====================================================================

INSERT INTO usuario (tipo, nome, email, senha_hash, cpf) VALUES
    ('ADMINISTRADOR', 'Admin', 'admin@astra',
     'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', NULL),
    ('CLIENTE', 'Kiko da Silva', 'cliente@email.com',
     'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', '000.000.000-00');

INSERT INTO filme (titulo, duracao_minutos, classificacao, genero, diretor, ativo, dublado, sinopse) VALUES
    ('O Auto da Compadecida', 104, 'DOZE', 'Comédia/Aventura', 'Guel Arraes', 1, 1,
     'As aventuras de João Grilo, um sertanejo pobre e mentiroso, e Chicó, o mais covarde dos homens.');

INSERT INTO produto (nome, descricao, preco, estoque, categoria) VALUES
    ('Pipoca Grande', 'Salgada', 25.0, 50, 'PIPOCA');

INSERT INTO sala (numero) VALUES (1);

-- 10 assentos comuns na sala 1 (igual ao laço que existia no CinemaService)
INSERT INTO assento (sala_id, numero, tipo)
SELECT s.id, n.numero, 'COMUM'
FROM sala s,
     (SELECT 1 AS numero UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) n
WHERE s.numero = 1;

-- Sessão de amanhã às 19:00 (data relativa para nunca nascer no passado).
-- preco_base 15.0 = o valorReferencia que estava fixo em Ingresso.
INSERT INTO sessao (filme_id, sala_id, data_hora, status, preco_base)
SELECT f.id, s.id, strftime('%Y-%m-%dT19:00', 'now', 'localtime', '+1 day'), 'AGENDADA', 15.0
FROM filme f, sala s
WHERE f.titulo = 'O Auto da Compadecida' AND s.numero = 1;

-- Filmes adicionais
INSERT INTO filme (titulo, duracao_minutos, classificacao, genero, diretor, ativo, dublado, sinopse) VALUES
    ('Central do Brasil', 113, 'DOZE', 'Drama', 'Walter Salles', 1, 0,
     'Dora, uma ex-professora que escreve cartas para analfabetos, ajuda um menino a procurar o pai no Nordeste.'),
    ('Cidade de Deus', 130, 'DEZESSEIS', 'Drama/Crime', 'Fernando Meirelles', 1, 0,
     'Buscapé, um jovem fotógrafo, acompanha o crescimento do crime organizado na Cidade de Deus, no Rio de Janeiro.');
