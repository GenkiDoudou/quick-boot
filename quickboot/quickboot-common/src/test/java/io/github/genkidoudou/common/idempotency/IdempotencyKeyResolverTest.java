package io.github.genkidoudou.common.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdempotencyKeyResolverTest {

  @Test
  void buildStorageKey_userScopeWithUri() {
    IdempotencyProperties props = new IdempotencyProperties();
    props.setKeyPrefix("p:");
    Idempotent ann = ann("", IdempotencyScope.USER, true, false);

    String key = IdempotencyKeyResolver.buildStorageKey("key-1", ann, props, "POST", "/sys/user/add");
    assertEquals("p:anon:POST:/sys/user/add:key-1", key);
  }

  @Test
  void buildStorageKey_globalWithoutUri() {
    IdempotencyProperties props = new IdempotencyProperties();
    props.setKeyPrefix("p:");
    Idempotent ann = ann("", IdempotencyScope.GLOBAL, false, false);

    String key = IdempotencyKeyResolver.buildStorageKey("key-2", ann, props, "POST", "/sys/user/add");
    assertEquals("p:key-2", key);
  }

  @Test
  void resolveClientKey_spelPreferredOverHeader() throws Exception {
    IdempotencyProperties props = new IdempotencyProperties();
    HttpServletRequest request = headerRequest(Map.of(IdempotencyKeys.HEADER_NAME, "header-key"));
    Method method = IdempotencySpelEvaluatorTest.SpelFixtures.class.getMethod("byParam", Long.class);
    Idempotent ann = ann("#orderId", IdempotencyScope.USER, true, false);

    String clientKey = IdempotencyKeyResolver.resolveClientKey(
        request, ann, props, method, new Object[]{99L});
    assertEquals("99", clientKey);
  }

  @Test
  void resolveClientKey_headerWhenNoSpel() {
    IdempotencyProperties props = new IdempotencyProperties();
    HttpServletRequest request = headerRequest(Map.of(IdempotencyKeys.HEADER_NAME, "header-only"));
    Idempotent ann = ann("", IdempotencyScope.USER, true, false);

    String clientKey = IdempotencyKeyResolver.resolveClientKey(
        request, ann, props, null, null);
    assertEquals("header-only", clientKey);
  }

  @Test
  void resolveClientKey_skipWhenNeither() {
    IdempotencyProperties props = new IdempotencyProperties();
    HttpServletRequest request = headerRequest(Map.of());
    Idempotent ann = ann("", IdempotencyScope.USER, true, false);

    assertNull(IdempotencyKeyResolver.resolveClientKey(request, ann, props, null, null));
  }

  private static HttpServletRequest headerRequest(Map<String, String> headers) {
    Map<String, String> h = new HashMap<>(headers);
    return new HttpServletRequest() {
      @Override
      public String getHeader(String name) {
        return h.get(name);
      }

      @Override
      public String getMethod() {
        return "POST";
      }

      @Override
      public String getRequestURI() {
        return "/test";
      }

      @Override
      public Object getAttribute(String name) {
        return null;
      }

      @Override
      public java.util.Enumeration<String> getAttributeNames() {
        return java.util.Collections.emptyEnumeration();
      }

      @Override
      public String getCharacterEncoding() {
        return null;
      }

      @Override
      public void setCharacterEncoding(String env) {
      }

      @Override
      public int getContentLength() {
        return 0;
      }

      @Override
      public long getContentLengthLong() {
        return 0;
      }

      @Override
      public String getContentType() {
        return null;
      }

      @Override
      public jakarta.servlet.ServletInputStream getInputStream() {
        return null;
      }

      @Override
      public String getParameter(String name) {
        return null;
      }

      @Override
      public java.util.Enumeration<String> getParameterNames() {
        return java.util.Collections.emptyEnumeration();
      }

      @Override
      public String[] getParameterValues(String name) {
        return new String[0];
      }

      @Override
      public Map<String, String[]> getParameterMap() {
        return Map.of();
      }

      @Override
      public String getProtocol() {
        return null;
      }

      @Override
      public String getScheme() {
        return null;
      }

      @Override
      public String getServerName() {
        return null;
      }

      @Override
      public int getServerPort() {
        return 0;
      }

      @Override
      public java.io.BufferedReader getReader() {
        return null;
      }

      @Override
      public String getRemoteAddr() {
        return null;
      }

      @Override
      public String getRemoteHost() {
        return null;
      }

      @Override
      public void setAttribute(String name, Object o) {
      }

      @Override
      public void removeAttribute(String name) {
      }

      @Override
      public java.util.Locale getLocale() {
        return null;
      }

      @Override
      public java.util.Enumeration<java.util.Locale> getLocales() {
        return java.util.Collections.emptyEnumeration();
      }

      @Override
      public boolean isSecure() {
        return false;
      }

      @Override
      public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
        return null;
      }

      @Override
      public int getRemotePort() {
        return 0;
      }

      @Override
      public String getLocalName() {
        return null;
      }

      @Override
      public String getLocalAddr() {
        return null;
      }

      @Override
      public int getLocalPort() {
        return 0;
      }

      @Override
      public jakarta.servlet.ServletContext getServletContext() {
        return null;
      }

      @Override
      public jakarta.servlet.AsyncContext startAsync() {
        return null;
      }

      @Override
      public jakarta.servlet.AsyncContext startAsync(
          jakarta.servlet.ServletRequest servletRequest,
          jakarta.servlet.ServletResponse servletResponse) {
        return null;
      }

      @Override
      public boolean isAsyncStarted() {
        return false;
      }

      @Override
      public boolean isAsyncSupported() {
        return false;
      }

      @Override
      public jakarta.servlet.AsyncContext getAsyncContext() {
        return null;
      }

      @Override
      public jakarta.servlet.DispatcherType getDispatcherType() {
        return null;
      }

      @Override
      public String getRequestId() {
        return null;
      }

      @Override
      public String getProtocolRequestId() {
        return null;
      }

      @Override
      public jakarta.servlet.ServletConnection getServletConnection() {
        return null;
      }

      @Override
      public java.util.Enumeration<String> getHeaderNames() {
        return java.util.Collections.enumeration(h.keySet());
      }

      @Override
      public java.util.Enumeration<String> getHeaders(String name) {
        String v = h.get(name);
        return v == null
            ? java.util.Collections.emptyEnumeration()
            : java.util.Collections.enumeration(java.util.List.of(v));
      }

      @Override
      public int getIntHeader(String name) {
        return -1;
      }

      @Override
      public long getDateHeader(String name) {
        return -1;
      }

      @Override
      public String getAuthType() {
        return null;
      }

      @Override
      public jakarta.servlet.http.Cookie[] getCookies() {
        return new jakarta.servlet.http.Cookie[0];
      }

      @Override
      public String getPathInfo() {
        return null;
      }

      @Override
      public String getPathTranslated() {
        return null;
      }

      @Override
      public String getContextPath() {
        return null;
      }

      @Override
      public String getQueryString() {
        return null;
      }

      @Override
      public String getRemoteUser() {
        return null;
      }

      @Override
      public boolean isUserInRole(String role) {
        return false;
      }

      @Override
      public java.security.Principal getUserPrincipal() {
        return null;
      }

      @Override
      public String getRequestedSessionId() {
        return null;
      }

      @Override
      public StringBuffer getRequestURL() {
        return new StringBuffer("/test");
      }

      @Override
      public String getServletPath() {
        return null;
      }

      @Override
      public jakarta.servlet.http.HttpSession getSession(boolean create) {
        return null;
      }

      @Override
      public jakarta.servlet.http.HttpSession getSession() {
        return null;
      }

      @Override
      public String changeSessionId() {
        return null;
      }

      @Override
      public boolean isRequestedSessionIdValid() {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdFromCookie() {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdFromURL() {
        return false;
      }

      @Override
      public boolean authenticate(jakarta.servlet.http.HttpServletResponse response) {
        return false;
      }

      @Override
      public void login(String username, String password) {
      }

      @Override
      public void logout() {
      }

      @Override
      public java.util.Collection<jakarta.servlet.http.Part> getParts() {
        return java.util.Collections.emptyList();
      }

      @Override
      public jakarta.servlet.http.Part getPart(String name) {
        return null;
      }

      @Override
      public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
        return null;
      }
    };
  }

  private static Idempotent ann(String key, IdempotencyScope scope, boolean includeUri, boolean required) {
    return new Idempotent() {
      @Override
      public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return Idempotent.class;
      }

      @Override
      public String key() {
        return key;
      }

      @Override
      public int ttlSeconds() {
        return -1;
      }

      @Override
      public boolean required() {
        return required;
      }

      @Override
      public IdempotencyScope scope() {
        return scope;
      }

      @Override
      public boolean includeUri() {
        return includeUri;
      }

      @Override
      public String message() {
        return "";
      }
    };
  }
}
