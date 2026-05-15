package io.github.genkidoudou.common.firewall.password;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Spring BCrypt密码编码器适配器
 * 适配Spring Security的BCryptPasswordEncoder
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
public class SpringBCryptPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();


    @Override
    public String encrypt(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public String decrypt(String encodedPassword) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return delegate.upgradeEncoding(encodedPassword);
    }
}
