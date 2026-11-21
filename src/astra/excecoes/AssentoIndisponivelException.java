package astra.excecoes;

/**
 * A poltrona pedida não pode ser vendida (já foi vendida, não existe ou já está no pedido).
 *
 * Guarda a sessão e o número da poltrona como DADOS, e não só dentro do texto:
 * assim a tela consegue montar a própria mensagem ("A poltrona 7 acabou de ser vendida...")
 * e achar o ingresso certo no carrinho, sem precisar "ler" a frase da exceção.
 */
public class AssentoIndisponivelException extends Exception {

    private final int sessaoId;
    private final int numeroAssento;

    public AssentoIndisponivelException(String mensagem, int sessaoId, int numeroAssento) {
        super(mensagem);
        this.sessaoId = sessaoId;
        this.numeroAssento = numeroAssento;
    }

    /** @return id da sessão da poltrona */
    public int getSessaoId() { return sessaoId; }

    /** @return número da poltrona que não pôde ser vendida */
    public int getNumeroAssento() { return numeroAssento; }
}
