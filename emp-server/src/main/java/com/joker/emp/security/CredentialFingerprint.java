package com.joker.emp.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 凭证指纹：把用户当前口令散列摘要写进访问 / 刷新令牌。
 * 修改或重置密码后指纹变化，旧令牌会在下一次请求时立即失效。
 */
public final class CredentialFingerprint {

  private CredentialFingerprint() {
  }

  public static String of(Long userId, String passwordHash) {
    String source = userId + ":" + (passwordHash == null ? "" : passwordHash);
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(source.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
