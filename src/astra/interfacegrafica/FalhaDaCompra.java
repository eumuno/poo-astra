package astra.interfacegrafica;

import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.PagamentoException;
import astra.excecoes.PersistenciaException;

import java.util.concurrent.ExecutionException;

/**
 * Traduz uma falha ao finalizar a compra em algo que o cliente entende:
 * o que aconteceu, se foi cobrado e o que fazer agora.
 *
 * <p>Antes, toda falha caía em "Não foi possível concluir o pagamento", com o mesmo
 * título e o mesmo ícone, mesmo quando nada tinha sido cobrado. Aqui cada tipo de
 * exceção vira uma mensagem própria, com título, gravidade e a aba para onde levar o cliente.</p>
 *
 * <p>Não usa Swing de propósito: só decide O QUE mostrar. Quem mostra é a TelaCliente.
 * Assim a regra pode ser testada sem abrir janela.</p>
 *
 * @param titulo     título curto da janela de aviso
 * @param texto      o que aconteceu e o que fazer
 * @param gravidade  AVISO (o cliente resolve) ou ERRO (falha do sistema)
 * @param destino    aba para onde levar o cliente depois do aviso
 */
public record FalhaDaCompra(String titulo, String texto, Gravidade gravidade, Destino destino) {

    /** AVISO: algo que o próprio cliente resolve. ERRO: falha do sistema. */
    public enum Gravidade { AVISO, ERRO }

    /** Para onde a tela leva o cliente depois de mostrar a mensagem. */
    public enum Destino { FICAR, INGRESSOS, CARRINHO }

    /**
     * Classifica a exceção que interrompeu a compra.
     *
     * @param erro a exceção (pode vir embrulhada numa ExecutionException do SwingWorker)
     * @return o que mostrar ao cliente
     */
    public static FalhaDaCompra classificar(Throwable erro) {
        Throwable causa = desembrulhar(erro);

        if (causa instanceof AssentoIndisponivelException e) {
            String poltrona = e.getNumeroAssento() > 0 ? "A poltrona " + e.getNumeroAssento() : "Uma das poltronas";
            return new FalhaDaCompra("Poltrona indisponível",
                    poltrona + " acabou de ser vendida. Nada foi cobrado. Escolha outra.",
                    Gravidade.AVISO, Destino.INGRESSOS);
        }
        if (causa instanceof EstoqueInsuficienteException e) {
            String texto = e.getDisponivel() > 0
                    ? "Restam " + e.getDisponivel() + " de " + e.getNomeProduto() + ". Nada foi cobrado. Reduza a quantidade."
                    : e.getNomeProduto() + " esgotou. Nada foi cobrado. Remova o item do carrinho.";
            return new FalhaDaCompra("Estoque insuficiente", texto, Gravidade.AVISO, Destino.CARRINHO);
        }
        if (causa instanceof PagamentoException e) {
            return new FalhaDaCompra("Pagamento recusado",
                    "Pagamento recusado: " + semPontoFinal(e.getMessage()) + ". Tente outra forma.",
                    Gravidade.AVISO, Destino.FICAR);
        }
        if (causa instanceof PersistenciaException) {
            return new FalhaDaCompra("Compra não registrada",
                    "Não conseguimos registrar a compra. Nada foi cobrado. Tente de novo.",
                    Gravidade.ERRO, Destino.FICAR);
        }
        if (causa instanceof IllegalStateException || causa instanceof IllegalArgumentException) {
            // Regra de negócio violada (ex.: carrinho vazio): as mensagens do modelo já são para o cliente
            return new FalhaDaCompra("Revise seu pedido", causa.getMessage(), Gravidade.AVISO, Destino.CARRINHO);
        }
        return new FalhaDaCompra("Erro inesperado",
                "Algo inesperado impediu a compra. Tente de novo em instantes.",
                Gravidade.ERRO, Destino.FICAR);
    }

    /** O motivo entra no meio da frase; tira o ponto final para não ficar "vencido.. Tente". */
    private static String semPontoFinal(String motivo) {
        if (motivo == null || motivo.isBlank()) return "motivo não informado";
        String limpo = motivo.strip();
        return limpo.endsWith(".") ? limpo.substring(0, limpo.length() - 1) : limpo;
    }

    /** O SwingWorker devolve a exceção original dentro de uma ExecutionException: tira a casca. */
    public static Throwable desembrulhar(Throwable erro) {
        Throwable atual = erro;
        while (atual instanceof ExecutionException && atual.getCause() != null) {
            atual = atual.getCause();
        }
        return atual;
    }
}
