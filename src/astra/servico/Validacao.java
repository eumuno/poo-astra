package astra.servico;

/**
 * Verificações de entrada compartilhadas pelos serviços. Limitar o tamanho de cada campo é a defesa simples
 * contra textos enormes que encheriam o banco, travariam a tela ou deixariam o login caro demais (negação de
 * serviço).
 */
final class Validacao {

    private Validacao() {
        // só métodos estáticos
    }

    /**
     * Recusa um texto maior que o limite.
     *
     * @param valor  o texto digitado (pode ser nulo, e então não há o que conferir)
     * @param limite o tamanho máximo permitido, em caracteres
     * @param campo  o nome do campo, como aparece na mensagem ("O título do filme")
     * @throws IllegalArgumentException se o texto passar do limite
     */
    static void exigirNoMaximo(String valor, int limite, String campo) {
        if (valor != null && valor.length() > limite) {
            throw new IllegalArgumentException(campo + " deve ter no máximo " + limite + " caracteres.");
        }
    }
}