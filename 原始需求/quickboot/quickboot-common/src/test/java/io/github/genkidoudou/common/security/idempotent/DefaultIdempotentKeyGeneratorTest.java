package io.github.genkidoudou.common.security.idempotent;

import io.github.genkidoudou.common.firewall.idempotent.DefaultIdempotentKeyGenerator;
import io.github.genkidoudou.common.firewall.idempotent.Idempotent;
import io.github.genkidoudou.common.firewall.idempotent.IdempotentProperties;
import io.github.genkidoudou.common.firewall.idempotent.KeyGenerateStrategy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DefaultIdempotentKeyGenerator 默认幂等键生成器测试类
 *
 * @author genkidoudou
 */
class DefaultIdempotentKeyGeneratorTest {

    @Mock
    private IdempotentProperties properties;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @Mock
    private Idempotent idempotent;

    private DefaultIdempotentKeyGenerator keyGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(properties.getKeyPrefix()).thenReturn("idempotent");
        when(properties.getTokenHeader()).thenReturn("X-Idempotent-Token");
        keyGenerator = new DefaultIdempotentKeyGenerator(properties);
    }

    @Test
    void testGenerateDefaultKey() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        when(idempotent.strategy()).thenReturn(KeyGenerateStrategy.DEFAULT);
        when(idempotent.prefix()).thenReturn("");

        String key = keyGenerator.generateKey(joinPoint, idempotent);

        assertNotNull(key);
        assertTrue(key.startsWith("idempotent:"));
        assertTrue(key.contains("DefaultIdempotentKeyGeneratorTest.testMethod"));
    }

    @Test
    void testGenerateKeyWithCustomPrefix() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(idempotent.strategy()).thenReturn(KeyGenerateStrategy.DEFAULT);
        when(idempotent.prefix()).thenReturn("custom");

        String key = keyGenerator.generateKey(joinPoint, idempotent);

        assertNotNull(key);
        assertTrue(key.startsWith("idempotent:custom:"));
    }

    @Test
    void testGenerateKeyWithNoArgs() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(idempotent.strategy()).thenReturn(KeyGenerateStrategy.DEFAULT);
        when(idempotent.prefix()).thenReturn("");

        String key = keyGenerator.generateKey(joinPoint, idempotent);

        assertNotNull(key);
        assertTrue(key.endsWith(":noargs"));
    }

    @Test
    void testGenerateKeyWithNullArgs() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(null);
        when(idempotent.strategy()).thenReturn(KeyGenerateStrategy.DEFAULT);
        when(idempotent.prefix()).thenReturn("");

        String key = keyGenerator.generateKey(joinPoint, idempotent);

        assertNotNull(key);
        assertTrue(key.endsWith(":noargs"));
    }

    @Test
    void testGenerateKeySameArgsProduceSameHash() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(idempotent.strategy()).thenReturn(KeyGenerateStrategy.DEFAULT);
        when(idempotent.prefix()).thenReturn("");

        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        String key1 = keyGenerator.generateKey(joinPoint, idempotent);

        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        String key2 = keyGenerator.generateKey(joinPoint, idempotent);

        assertEquals(key1, key2);
    }

    @Test
    void testGenerateKeyDifferentArgsProduceDifferentHash() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(idempotent.strategy()).thenReturn(KeyGenerateStrategy.DEFAULT);
        when(idempotent.prefix()).thenReturn("");

        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        String key1 = keyGenerator.generateKey(joinPoint, idempotent);

        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg3"});
        String key2 = keyGenerator.generateKey(joinPoint, idempotent);

        assertNotEquals(key1, key2);
    }

    // 测试方法，用于反射
    public void testMethod() {
    }
}
