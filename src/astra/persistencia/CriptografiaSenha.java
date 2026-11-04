package astra.persistencia;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Gera o hash das senhas antes de gravar no banco, para nunca guardar a
 * senha em texto puro. Usa SHA-256 da própria API Java (sem biblioteca extra).
 *
 * Simplificação didática: um sistema real usaria um algoritmo com "sal" e
 * custo ajustável (bcrypt, Argon2).
 */
public final class CriptografiaSenha {

    private CriptografiaSenha() {
        // só métodos estáticos
    }

    /** Devolve o SHA-256 da senha em hexadecimal (64 caracteres). */
    public static String gerarHash(String senha) {
        if (senha == null) {
            throw new IllegalArgumentException("A senha não pode ser nula!");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // todo Java tem SHA-256, então isso não deve acontecer
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
        }
    }

    /** Confere se a senha digitada corresponde ao hash gravado no banco. */
    public static boolean confere(String senhaDigitada, String hashGravado) {
        return hashGravado != null && hashGravado.equals(gerarHash(senhaDigitada));
    }
}
