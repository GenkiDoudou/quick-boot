package io.github.genkidoudou.common.idempotency;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdempotencySpelEvaluatorTest {

  @Test
  void evaluateRequestParam() throws Exception {
    Method method = SpelFixtures.class.getMethod("byParam", Long.class);
    String key = IdempotencySpelEvaluator.evaluate("#orderId", method, new Object[]{100L}, null);
    assertEquals("100", key);
  }

  @Test
  void evaluateBodyField() throws Exception {
    Method method = SpelFixtures.class.getMethod("byBody", OrderBody.class);
    OrderBody body = new OrderBody();
    body.setOrderId("ord-1");
    String key = IdempotencySpelEvaluator.evaluate("#body.orderId", method, new Object[]{body}, null);
    assertEquals("ord-1", key);
  }

  @Test
  void evaluateCombined() throws Exception {
    Method method = SpelFixtures.class.getMethod("byBody", OrderBody.class);
    OrderBody body = new OrderBody();
    body.setOrderId("ord-2");
    String key = IdempotencySpelEvaluator.evaluate("#userId + ':' + #body.orderId", method, new Object[]{body}, null);
    assertEquals("anon:ord-2", key);
  }

  @Test
  void evaluateEmptyResultThrows() throws Exception {
    Method method = SpelFixtures.class.getMethod("byBody", OrderBody.class);
    assertThrows(Exception.class,
        () -> IdempotencySpelEvaluator.evaluate("#body.orderId", method, new Object[]{new OrderBody()}, null));
  }

  public static class SpelFixtures {
    public void byParam(@RequestParam Long orderId) {
    }

    public void byBody(@RequestBody OrderBody body) {
    }
  }

  static class OrderBody {
    private String orderId;

    public String getOrderId() {
      return orderId;
    }

    public void setOrderId(String orderId) {
      this.orderId = orderId;
    }
  }
}
