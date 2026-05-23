package com.su60.quickboot.common.idempotent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 幂等缓存值包装类
 * <p>
 * 用于存储幂等标识和过期时间
 * </p>
 *
 * @author luyanan
 * @since 2026/01/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdempotentValue {

	/**
	 * 存储时间戳
	 */
	private long timestamp;

	/**
	 * 过期时间（秒）
	 */
	private int expireTime;

	/**
	 * 检查是否已过期
	 *
	 * @return 是否过期
	 */
	public boolean isExpired() {
		return System.currentTimeMillis() - timestamp > expireTime * 1000L;
	}
}
