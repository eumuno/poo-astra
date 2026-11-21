package astra.persistencia;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Gera e confere o hash das senhas, para o banco nunca guardar a senha em texto puro.
 *
 * <p>O formato novo é {@code pbkdf2$iterações$sal$hash}: PBKDF2 com HMAC-SHA256 (já vem no JDK, sem biblioteca
 * extra), com um <b>sal aleatório de 16 bytes por senha</b> e 600.000 iterações. O sal faz duas contas com a mesma
 * senha terem hashes diferentes e inutiliza tabelas pré-calculadas; as iterações deixam cada tentativa de adivinhar
 * a senha cara para quem roubar o arquivo do banco.</p>
 *
 * <p>As contas criadas antes desta versão têm o formato antigo (SHA-256 sem sal, 64 caracteres hexadecimais).
 * Elas continuam entrando, e o repositório troca o hash pelo formato novo no primeiro login bem-sucedido
 * (veja {@link #precisaAtualizar(String)}).</p>
 */
public final class CriptografiaSenha {

    private static final String PREFIXO = "pbkdf2";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 600_000;
    private static final int BYTES_DO_SAL = 16;
    private static final int BITS_DO_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private CriptografiaSenha() {
        // só métodos estáticos
    }

    /**
     * Gera o hash de uma senha, com um sal novo.
     *
     * @param senha a senha em texto puro
     * @return o hash no formato {@code pbkdf2$iterações$sal$hash}
     * @throws IllegalArgumentException se a senha for nula
     */
    public static String gerarHash(String senha) {
        if (senha == null) {
            throw new IllegalArgumentException("A senha não pode ser nula!");
        }
        byte[] sal = new byte[BYTES_DO_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(senha, sal, ITERACOES);
        return PREFIXO + "$" + ITERACOES + "$" + codificar(sal) + "$" + codificar(hash);
    }

    /**
     * Confere se a senha digitada corresponde ao hash gravado, nos dois formatos (novo e antigo).
     * A comparação leva o mesmo tempo quer o hash difira no primeiro ou no último byte.
     *
     * @param senhaDigitada a senha informada no login
     * @param hashGravado   o hash que está no banco
     * @return {@code true} se a senha confere
     */
    public static boolean confere(String senhaDigitada, String hashGravado) {
        if (senhaDigitada == null || hashGravado == null) {
            return false;
        }
        if (hashGravado.startsWith(PREFIXO + "$")) {
            return confereFormatoNovo(senhaDigitada, hashGravado);
        }
        return MessageDigest.isEqual(hashGravado.getBytes(StandardCharsets.UTF_8),
                sha256EmHexadecimal(senhaDigitada).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Informa se o hash gravado deve ser trocado por um novo: está no formato antigo (sem sal) ou foi gerado
     * com menos iterações do que o padrão atual.
     *
     * @param hashGravado o hash que está no banco
     * @return {@code true} se vale gerar o hash de novo (o que só pode ser feito com a senha em mãos, no login)
     */
    public static boolean precisaAtualizar(String hashGravado) {
        if (hashGravado == null || !hashGravado.startsWith(PREFIXO + "$")) {
            return true;
        }
        String[] partes = hashGravado.split("\\$");
        try {
            return partes.length != 4 || Integer.parseInt(partes[1]) < ITERACOES;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static boolean confereFormatoNovo(String senhaDigitada, String hashGravado) {
        String[] partes = hashGravado.split("\\$");
        if (partes.length != 4) {
            return false;
        }
        try {
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return MessageDigest.isEqual(esperado, derivar(senhaDigitada, sal, iteracoes));
        } catch (IllegalArgumentException e) {
            return false; // hash corrompido: nunca confere
        }
    }

    private static byte[] derivar(String senha, byte[] sal, int iteracoes) {
        PBEKeySpec especificacao = new PBEKeySpec(senha.toCharArray(), sal, iteracoes, BITS_DO_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacao).getEncoded();
        } catch (GeneralSecurityException e) {
            // todo JDK moderno tem PBKDF2 com HMAC-SHA256, então isso não deve acontecer
            throw new IllegalStateException(ALGORITMO + " indisponível nesta JVM", e);
        } finally {
            especificacao.clearPassword();
        }
    }

    private static String codificar(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    // Só para conferir contas antigas: o formato SHA-256 sem sal não é mais usado para gravar
    private static String sha256EmHexadecimal(String senha) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
        }
    }
}