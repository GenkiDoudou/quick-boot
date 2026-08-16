package io.github.genkidoudou.common.file;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.util.StringUtils;

/**
 * 分类开启压缩时，对常见位图做等比缩小与重编码；非图片或失败时返回 empty（调用方保留原字节）。
 */
public final class ImageCompressSupport {

  private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "bmp");

  private ImageCompressSupport() {
  }

  /**
   * @param content  原始字节
   * @param filename 用于推断扩展名
   * @param rule     分类规则
   * @param compress 压缩配置
   * @return 压缩后的字节与建议 contentType；无需压缩或失败时 empty
   */
  public static Optional<CompressedImage> maybeCompress(
      byte[] content,
      String filename,
      FileClassifyRule rule,
      QcFileProperties.CompressProperties compress) {
    if (content == null || content.length == 0 || rule == null || !rule.isCompressEnabledFlag()) {
      return Optional.empty();
    }
    QcFileProperties.CompressProperties cfg =
        compress != null ? compress : new QcFileProperties.CompressProperties();
    // 服务端压缩总开关（前端压缩为主时默认关闭）
    if (!cfg.isEnabled()) {
      return Optional.empty();
    }
    int minKb = rule.resolveCompressMinSizeKb(cfg);
    if (content.length < minKb * 1024L) {
      return Optional.empty();
    }
    String ext = FilePathSupport.normalizeExtension(filename);
    if (!IMAGE_EXT.contains(ext)) {
      return Optional.empty();
    }
    try {
      BufferedImage source = ImageIO.read(new ByteArrayInputStream(content));
      if (source == null) {
        return Optional.empty();
      }
      BufferedImage scaled = scaleIfNeeded(source, rule.resolveCompressMaxEdge(cfg));
      String format = resolveFormat(ext);
      byte[] out = writeImage(scaled, format, rule.resolveCompressQuality(cfg));
      if (out == null || out.length == 0) {
        return Optional.empty();
      }
      // 压缩结果更大则保留原图
      if (out.length >= content.length) {
        return Optional.empty();
      }
      return Optional.of(new CompressedImage(out, contentTypeFor(format)));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * 判断扩展名是否属于可压缩位图类型。
   *
   * @param ext 小写扩展名（不含点）
   * @return 是否支持服务端压缩
   */
  public static boolean isCompressibleImageExt(String ext) {
    return StringUtils.hasText(ext) && IMAGE_EXT.contains(ext.toLowerCase(Locale.ROOT));
  }

  private static BufferedImage scaleIfNeeded(BufferedImage source, int maxEdge) {
    if (maxEdge <= 0) {
      return source;
    }
    int w = source.getWidth();
    int h = source.getHeight();
    int edge = Math.max(w, h);
    if (edge <= maxEdge) {
      return source;
    }
    double ratio = (double) maxEdge / (double) edge;
    int nw = Math.max(1, (int) Math.round(w * ratio));
    int nh = Math.max(1, (int) Math.round(h * ratio));
    Image tmp = source.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
    int type = source.getColorModel().hasAlpha()
        ? BufferedImage.TYPE_INT_ARGB
        : BufferedImage.TYPE_INT_RGB;
    BufferedImage dest = new BufferedImage(nw, nh, type);
    Graphics2D g = dest.createGraphics();
    g.drawImage(tmp, 0, 0, null);
    g.dispose();
    return dest;
  }

  private static String resolveFormat(String ext) {
    if ("png".equals(ext) || "bmp".equals(ext)) {
      return "png".equals(ext) ? "png" : "jpg";
    }
    return "jpg";
  }

  private static String contentTypeFor(String format) {
    if ("png".equals(format)) {
      return "image/png";
    }
    return "image/jpeg";
  }

  private static byte[] writeImage(BufferedImage image, String format, float quality) throws IOException {
    if ("jpg".equals(format) || "jpeg".equals(format)) {
      BufferedImage rgb = ensureRgb(image);
      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
      if (!writers.hasNext()) {
        ByteArrayOutputStream fallback = new ByteArrayOutputStream();
        if (!ImageIO.write(rgb, "jpg", fallback)) {
          return null;
        }
        return fallback.toByteArray();
      }
      ImageWriter writer = writers.next();
      try {
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
          param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
          param.setCompressionQuality(quality);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(bos)) {
          writer.setOutput(ios);
          writer.write(null, new IIOImage(rgb, null, null), param);
        }
        return bos.toByteArray();
      } finally {
        writer.dispose();
      }
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    if (!ImageIO.write(image, format, bos)) {
      return null;
    }
    return bos.toByteArray();
  }

  private static BufferedImage ensureRgb(BufferedImage source) {
    if (source.getType() == BufferedImage.TYPE_INT_RGB) {
      return source;
    }
    BufferedImage rgb =
        new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = rgb.createGraphics();
    g.drawImage(source, 0, 0, java.awt.Color.WHITE, null);
    g.dispose();
    return rgb;
  }

  /**
   * 压缩结果。
   *
   * @param bytes       压缩后字节
   * @param contentType MIME
   */
  public record CompressedImage(byte[] bytes, String contentType) {
  }
}
