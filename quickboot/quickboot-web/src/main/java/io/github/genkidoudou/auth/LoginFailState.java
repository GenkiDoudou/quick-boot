package io.github.genkidoudou.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录失败计数与锁定截止时间，存入 Spring Cache（{@code CacheManager}）条目值。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginFailState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前锁定窗口内连续失败次数。 */
    private int failCount;

    /** 锁定结束时间戳（毫秒）；0 表示未处于锁定截止语义。 */
    private long lockedUntilMs;
}
