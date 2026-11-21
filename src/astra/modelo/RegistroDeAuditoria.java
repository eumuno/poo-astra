package astra.modelo;

import java.time.LocalDateTime;

/**
 * Uma linha da trilha de auditoria: quem fez o quê, e quando. Serve para que ninguém consiga negar uma ação
 * depois de feita (não repúdio).
 *
 * @param id        identificador do registro (cresce a cada ação)
 * @param quando    momento em que a ação foi registrada
 * @param usuarioId id de quem fez a ação, ou {@code null} quando não há usuário identificado
 *                  (por exemplo, uma tentativa de login com e-mail inexistente)
 * @param acao      o tipo da ação, por exemplo {@code LOGIN_OK} ou {@code FILME_CRIADO}
 * @param detalhe   texto curto com o que foi feito; nunca contém senhas
 */
public record RegistroDeAuditoria(int id, LocalDateTime quando, Integer usuarioId, String acao, String detalhe) {
}