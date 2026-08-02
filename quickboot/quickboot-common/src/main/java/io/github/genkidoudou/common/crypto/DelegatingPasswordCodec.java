package io.github.genkidoudou.common.crypto;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 密码工厂
 *
 * @author luyanan
 * @since 2026/7/27
 */

public class DelegatingPasswordCodec extends AbstractValidatingPasswordCodec {
  private static final String DEFAULT_ID_PREFIX = "{";

  private static final String DEFAULT_ID_SUFFIX = "}";


  private final String idPrefix;

  private final String idSuffix;

  private final String idForEncode;

  private final PasswordCodec passwordEncoderForEncode;

  private final Map<@Nullable String, PasswordCodec> idToPasswordEncoder;
  // 默认的密码加密
  private PasswordCodec defaultPasswordEncoderForMatches;


  public DelegatingPasswordCodec(String idForEncode, Map<String, PasswordCodec> idToPasswordEncoder) {
    this(idForEncode, idToPasswordEncoder, DEFAULT_ID_PREFIX, DEFAULT_ID_SUFFIX);
  }

  public DelegatingPasswordCodec(String idForEncode, Map<String, PasswordCodec> idToPasswordEncoder,
                                 String idPrefix, String idSuffix) {

    if (idForEncode == null) {
      throw new IllegalArgumentException("idForEncode cannot be null");
    }
    if (idPrefix == null) {
      throw new IllegalArgumentException("prefix cannot be null");
    }
    if (idSuffix == null || idSuffix.isEmpty()) {
      throw new IllegalArgumentException("suffix cannot be empty");
    }
    if (idPrefix.contains(idSuffix)) {
      throw new IllegalArgumentException("idPrefix " + idPrefix + " cannot contain idSuffix " + idSuffix);
    }

    if (!idToPasswordEncoder.containsKey(idForEncode)) {
      throw new IllegalArgumentException(
        "idForEncode " + idForEncode + "is not found in idToPasswordEncoder " + idToPasswordEncoder);
    }
    for (String id : idToPasswordEncoder.keySet()) {
      if (id == null) {
        continue;
      }
      if (!idPrefix.isEmpty() && id.contains(idPrefix)) {
        throw new IllegalArgumentException("id " + id + " cannot contain " + idPrefix);
      }
      if (id.contains(idSuffix)) {
        throw new IllegalArgumentException("id " + id + " cannot contain " + idSuffix);
      }
    }
    this.idForEncode = idForEncode;
    this.passwordEncoderForEncode = idToPasswordEncoder.get(idForEncode);
    this.idToPasswordEncoder = new HashMap<>(idToPasswordEncoder);
    this.idPrefix = idPrefix;
    this.idSuffix = idSuffix;
  }


  public void setDefaultPasswordEncoderForMatches(PasswordCodec defaultPasswordEncoderForMatches) {
    if (defaultPasswordEncoderForMatches == null) {
      throw new IllegalArgumentException("defaultPasswordEncoderForMatches cannot be null");
    }
    this.defaultPasswordEncoderForMatches = defaultPasswordEncoderForMatches;
  }


  private String extractEncodedPassword(String prefixEncodedPassword) {
    int start = prefixEncodedPassword.indexOf(this.idSuffix);
    return prefixEncodedPassword.substring(start + this.idSuffix.length());
  }


  private @Nullable String extractId(@Nullable String prefixEncodedPassword) {
    if (prefixEncodedPassword == null) {
      return null;
    }
    int start = prefixEncodedPassword.indexOf(this.idPrefix);
    if (start != 0) {
      return null;
    }
    int end = prefixEncodedPassword.indexOf(this.idSuffix, start);
    if (end < 0) {
      return null;
    }
    return prefixEncodedPassword.substring(start + this.idPrefix.length(), end);
  }

  @Override
  protected String encryptNonNullPassword(String rawPassword) {
    return this.idPrefix + this.idForEncode + this.idSuffix + this.passwordEncoderForEncode.encrypt(rawPassword);

  }

  @Override
  protected boolean matchesNonNull(String rawPassword, String prefixEncodedPassword) {
    String id = extractId(prefixEncodedPassword);
    PasswordCodec delegate = this.idToPasswordEncoder.get(id);
    if (delegate == null) {
      return this.defaultPasswordEncoderForMatches.matches(rawPassword, prefixEncodedPassword);
    }
    String encodedPassword = extractEncodedPassword(prefixEncodedPassword);
    return delegate.matches(rawPassword, encodedPassword);
  }

  @Override
  protected String decryptNonNullPassword(String encodedPassword) {
    return this.idPrefix + this.idForEncode + this.idSuffix + this.passwordEncoderForEncode.decrypt(encodedPassword);
  }


  public void register(String idForEncode, PasswordCodec passwordCodec) {
    this.idToPasswordEncoder.put(idForEncode, passwordCodec);
  }

  public PasswordCodec get(String idForEncode) {
    return this.idToPasswordEncoder.get(idForEncode);
  }
}
