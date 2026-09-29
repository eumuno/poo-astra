package astra.excecoes;

public class AssentoIndisponivelException extends Exception {
    public AssentoIndisponivelException(String s) {
        super(s);
    }
    // REGRA DE NEGÓCIO SE O ASSENTO JÁ ESTIVER OCUPADO
}
