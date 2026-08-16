package io.github.genkidoudou.common.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import io.github.genkidoudou.common.exception.WarningException;

/**
 * {@link DefaultFileTemplate} 本地存储与分类校验（不启动 Spring）。
 */
class DefaultFileTemplateTest {

  @TempDir
  Path tempDir;

  @Test
  void upload_and_view_and_shortUrl_same() throws Exception {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    props.setDomain("https://cdn.example.com");
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, enabledRule("img"), List.of());

    byte[] bytes = {1, 2, 3};
    String path = tpl.upload(bytes, "a.PNG", "img");
    assertTrue(path.startsWith("img/"));
    assertTrue(path.endsWith(".png"));
    assertTrue(tpl.exists(path));

    assertEquals("https://cdn.example.com/" + path, tpl.view(path));
    assertEquals(tpl.view(path), tpl.getShortUrl(path));

    Resource res = tpl.download(path);
    assertEquals(3, res.getInputStream().readAllBytes().length);
    Path stored = tempDir.resolve(path);
    assertEquals(List.of((byte) 1, (byte) 2, (byte) 3),
        List.of(Files.readAllBytes(stored)[0], Files.readAllBytes(stored)[1], Files.readAllBytes(stored)[2]));

    tpl.delete(path);
    assertFalse(tpl.exists(path));
  }

  @Test
  void compress_enabled_keeps_non_image_bytes() throws Exception {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    FileClassifyRule rule = baseRule("img");
    rule.setLimitExt("png,jpg,bin");
    rule.setCompressEnabled("1");
    DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, fixed(rule), List.of());

    byte[] original = {10, 20, 30, 40};
    String path = tpl.upload(original, "a.bin", "img");
    assertTrue(java.util.Arrays.equals(original, Files.readAllBytes(tempDir.resolve(path))));
  }

  @Test
  void compress_enabled_shrinks_large_jpeg() throws Exception {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    props.getCompress().setEnabled(true);
    props.getCompress().setMinSizeKb(1);
    props.getCompress().setQuality(0.6f);
    props.getCompress().setMaxEdge(200);
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    FileClassifyRule rule = baseRule("img");
    rule.setCompressEnabled("1");
    DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, fixed(rule), List.of());

    byte[] original = createJpegBytes(800, 600);
    assertTrue(original.length > 5_000);
    String path = tpl.upload(original, "big.jpg", "img");
    byte[] stored = Files.readAllBytes(tempDir.resolve(path));
    assertTrue(stored.length < original.length, "stored=" + stored.length + " original=" + original.length);
  }

  @Test
  void server_compress_disabled_keeps_large_jpeg() throws Exception {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    props.getCompress().setEnabled(false);
    props.getCompress().setMaxEdge(200);
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    FileClassifyRule rule = baseRule("img");
    rule.setCompressEnabled("1");
    DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, fixed(rule), List.of());

    byte[] original = createJpegBytes(800, 600);
    String path = tpl.upload(original, "big.jpg", "img");
    assertTrue(java.util.Arrays.equals(original, Files.readAllBytes(tempDir.resolve(path))));
  }

  @Test
  void server_compress_skips_when_below_min_size() throws Exception {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    props.getCompress().setEnabled(true);
    props.getCompress().setMinSizeKb(10_000);
    props.getCompress().setMaxEdge(200);
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    FileClassifyRule rule = baseRule("img");
    rule.setCompressEnabled("1");
    DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, fixed(rule), List.of());

    byte[] original = createJpegBytes(800, 600);
    String path = tpl.upload(original, "big.jpg", "img");
    assertTrue(java.util.Arrays.equals(original, Files.readAllBytes(tempDir.resolve(path))));
  }

  @Test
  void compress_disabled_keeps_large_jpeg_bytes() throws Exception {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    props.getCompress().setMaxEdge(200);
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    FileClassifyRule rule = baseRule("img");
    rule.setCompressEnabled("0");
    DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, fixed(rule), List.of());

    byte[] original = createJpegBytes(800, 600);
    String path = tpl.upload(original, "big.jpg", "img");
    assertTrue(java.util.Arrays.equals(original, Files.readAllBytes(tempDir.resolve(path))));
  }

  private static byte[] createJpegBytes(int width, int height) throws Exception {
    java.awt.image.BufferedImage image =
        new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
    java.awt.Graphics2D g = image.createGraphics();
    g.setColor(java.awt.Color.BLUE);
    g.fillRect(0, 0, width, height);
    g.setColor(java.awt.Color.RED);
    g.fillOval(width / 4, height / 4, width / 2, height / 2);
    g.dispose();
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(image, "jpg", out);
    return out.toByteArray();
  }

  @Test
  void reject_bad_extension() {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, enabledRule("img"), List.of());

    assertThrows(WarningException.class, () -> tpl.upload(new byte[]{1}, "a.exe", "img"));
  }

  @Test
  void reject_missing_classify() {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    DefaultFileTemplate tpl =
        new DefaultFileTemplate(props, storage, new EmptyFileClassifyRuleResolver(), List.of());

    assertThrows(WarningException.class, () -> tpl.upload(new byte[]{1}, "a.png", "img"));
  }

  @Test
  void reject_disabled_classify() {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    FileClassifyRule rule = baseRule("img");
    rule.setStatus("1");
    DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, fixed(rule), List.of());

    assertThrows(WarningException.class, () -> tpl.upload(new byte[]{1}, "a.png", "img"));
  }

  @Test
  void hook_before_abort_triggers_onError() {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    boolean[] onError = {false};
    FileUploadHook hook = new FileUploadHook() {
      @Override
      public void beforeUpload(FileUploadBeforeContext ctx) {
        throw new IllegalStateException("deny");
      }

      @Override
      public void onError(FileUploadErrorContext ctx) {
        onError[0] = true;
      }
    };
    DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, enabledRule("img"), List.of(hook));
    assertThrows(IllegalStateException.class, () -> tpl.upload(new byte[]{1}, "a.png", "img"));
    assertTrue(onError[0]);
  }

  @Test
  void hook_after_abort_rollbacks_object_and_triggers_onError() {
    QcFileProperties props = new QcFileProperties();
    props.getLocal().setPath(tempDir.toString());
    LocalFileStorageBackend storage = new LocalFileStorageBackend(tempDir);
    boolean[] onError = {false};
    String[] relativePath = {null};
    FileUploadHook hook = new FileUploadHook() {
      @Override
      public void afterUpload(FileUploadAfterContext ctx) {
        relativePath[0] = ctx.getRelativePath();
        throw new IllegalStateException("after");
      }

      @Override
      public void onError(FileUploadErrorContext ctx) {
        onError[0] = true;
      }
    };
    DefaultFileTemplate tpl = new DefaultFileTemplate(props, storage, enabledRule("img"), List.of(hook));
    assertThrows(IllegalStateException.class, () -> tpl.upload(new byte[]{1}, "a.png", "img"));
    assertTrue(onError[0]);
    assertTrue(relativePath[0] != null && !relativePath[0].isBlank());
    assertFalse(tpl.exists(relativePath[0]));
  }

  private static FileClassifyRuleResolver enabledRule(String classify) {
    return fixed(baseRule(classify));
  }

  private static FileClassifyRule baseRule(String classify) {
    FileClassifyRule rule = new FileClassifyRule();
    rule.setClassify(classify);
    rule.setClassifyName(classify);
    rule.setLimitExt("png,jpg,jpeg");
    rule.setLimitSizeBytes(10L * 1024 * 1024);
    rule.setLimitCount(1);
    rule.setCompressEnabled("0");
    rule.setAnonymous(false);
    rule.setStatus("0");
    return rule;
  }

  private static FileClassifyRuleResolver fixed(FileClassifyRule rule) {
    return new FileClassifyRuleResolver() {
      @Override
      public Optional<FileClassifyRule> findByClassify(String classify) {
        return rule.getClassify().equals(classify) ? Optional.of(rule) : Optional.empty();
      }

      @Override
      public List<FileClassifyRule> listEnabled() {
        return rule.isEnabled() ? List.of(rule) : List.of();
      }
    };
  }
}
