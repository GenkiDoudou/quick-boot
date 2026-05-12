package com.su60.quickboot.data.file.utils;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析文件类型
 *
 * @author luyanan
 * @since 2025/1/7
 */
@UtilityClass
public class FileTypeDetector {
	// 文件扩展名到MIME类型的映射
	private static final Map<String, String> EXTENSION_MAP = new HashMap<>();
	// 文件头签名到MIME类型的映射
	private static final Map<byte[], String> SIGNATURE_MAP = new HashMap<>();

	static {
		// 文档文件
		EXTENSION_MAP.put("pdf", "application/pdf");
		EXTENSION_MAP.put("doc", "application/msword");
		EXTENSION_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		EXTENSION_MAP.put("xls", "application/vnd.ms-excel");
		EXTENSION_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		EXTENSION_MAP.put("ppt", "application/vnd.ms-powerpoint");
		EXTENSION_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		EXTENSION_MAP.put("txt", "text/plain");

		// 图片文件
		EXTENSION_MAP.put("jpg", "image/jpeg");
		EXTENSION_MAP.put("jpeg", "image/jpeg");
		EXTENSION_MAP.put("png", "image/png");
		EXTENSION_MAP.put("gif", "image/gif");
		EXTENSION_MAP.put("bmp", "image/bmp");
		EXTENSION_MAP.put("svg", "image/svg+xml");
		EXTENSION_MAP.put("tiff", "image/tiff");
		EXTENSION_MAP.put("webp", "image/webp");

		// 音频文件
		EXTENSION_MAP.put("mp3", "audio/mpeg");
		EXTENSION_MAP.put("wav", "audio/wav");
		EXTENSION_MAP.put("aac", "audio/aac");
		EXTENSION_MAP.put("flac", "audio/flac");
		EXTENSION_MAP.put("ogg", "audio/ogg");

		// 视频文件
		EXTENSION_MAP.put("mp4", "video/mp4");
		EXTENSION_MAP.put("avi", "video/x-msvideo");
		EXTENSION_MAP.put("mov", "video/quicktime");
		EXTENSION_MAP.put("mkv", "video/x-matroska");
		EXTENSION_MAP.put("webm", "video/webm");

		// 压缩文件
		EXTENSION_MAP.put("zip", "application/zip");
		EXTENSION_MAP.put("rar", "application/vnd.rar");
		EXTENSION_MAP.put("7z", "application/x-7z-compressed");
		EXTENSION_MAP.put("tar", "application/x-tar");
		EXTENSION_MAP.put("gz", "application/gzip");

		// 其他
		EXTENSION_MAP.put("json", "application/json");
		EXTENSION_MAP.put("css", "text/css");
		EXTENSION_MAP.put("js", "application/javascript");
		EXTENSION_MAP.put("html", "text/html");

		// 文档文件
		SIGNATURE_MAP.put(new byte[]{(byte) 0x25, 'P', 'D', 'F'}, "application/pdf"); // PDF
		SIGNATURE_MAP.put(new byte[]{(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0}, "application/msword"); // .doc
		SIGNATURE_MAP.put(new byte[]{(byte) 0xEC, (byte) 0xA5, (byte) 0xC3, (byte) 0x5D}, "application/vnd.openxmlformats-officedocument.wordprocessingml.document"); // .docx
		SIGNATURE_MAP.put(new byte[]{(byte) 0x09, 'E', 'C', 'L'}, "application/vnd.ms-excel"); // .xls
		SIGNATURE_MAP.put(new byte[]{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04}, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // .xlsx

		// 图片文件
		SIGNATURE_MAP.put(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}, "image/jpeg"); // JPEG
		SIGNATURE_MAP.put(new byte[]{(byte) 0x89, 'P', 'N', 'G'}, "image/png"); // PNG
		SIGNATURE_MAP.put(new byte[]{'G', 'I', 'F'}, "image/gif"); // GIF
		SIGNATURE_MAP.put(new byte[]{(byte) 0x42, (byte) 0x4D}, "image/bmp"); // BMP
		SIGNATURE_MAP.put(new byte[]{(byte) 0x49, (byte) 0x49, (byte) 0x2A, (byte) 0x00}, "image/tiff"); // TIFF (Intel)
		SIGNATURE_MAP.put(new byte[]{(byte) 0x4D, (byte) 0x4D, (byte) 0x00, (byte) 0x2A}, "image/tiff"); // TIFF (Motorola)

		// 音频文件
		SIGNATURE_MAP.put(new byte[]{(byte) 0x49, (byte) 0x44, (byte) 0x33}, "audio/mpeg"); // MP3
		SIGNATURE_MAP.put(new byte[]{(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46}, "audio/wav"); // WAV
		SIGNATURE_MAP.put(new byte[]{(byte) 0x46, (byte) 0x4C, (byte) 0x41, (byte) 0x43}, "audio/flac"); // FLAC
		SIGNATURE_MAP.put(new byte[]{(byte) 0x4F, (byte) 0x67, (byte) 0x67, (byte) 0x53}, "audio/ogg"); // OGG

		// 视频文件
		SIGNATURE_MAP.put(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x66,
				(byte) 0x74, (byte) 0x79, (byte) 0x70, (byte) 0x6D, (byte) 0x70, (byte) 0x34, (byte) 0x32}, "video/mp4"); // MP4
		SIGNATURE_MAP.put(new byte[]{(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46}, "video/x-msvideo"); // AVI
		SIGNATURE_MAP.put(new byte[]{(byte) 0x6D, (byte) 0x6F, (byte) 0x6F, (byte) 0x76}, "video/quicktime"); // MOV
		SIGNATURE_MAP.put(new byte[]{(byte) 0x1A, (byte) 0x45, (byte) 0xDF, (byte) 0xA3}, "video/x-matroska"); // MKV
		SIGNATURE_MAP.put(new byte[]{(byte) 0x1A, (byte) 0x45, (byte) 0xDF, (byte) 0xA3}, "video/webm"); // WebM

		// 压缩文件
		SIGNATURE_MAP.put(new byte[]{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04}, "application/zip"); // ZIP
		SIGNATURE_MAP.put(new byte[]{(byte) 0x52, (byte) 0x61, (byte) 0x72, (byte) 0x21, (byte) 0x1A, (byte) 0x07, (byte) 0x00}, "application/vnd.rar"); // RAR
		SIGNATURE_MAP.put(new byte[]{(byte) 0x37, (byte) 0x7A, (byte) 0xBC, (byte) 0xAF, (byte) 0x27, (byte) 0x1C}, "application/x-7z-compressed"); // 7Z

	}

	/**
	 * 解析文件contentType
	 *
	 * @param fileName  文件名
	 * @param fileBytes 文件字节数组
	 * @return 文件类型
	 * @since 2025/1/7
	 */
	public static String detectFileType(String fileName, byte[] fileBytes) {
		String mimeType = getMimeTypeByExtension(fileName);
		if ("application/octet-stream".equals(mimeType)) {
			mimeType = getMimeTypeBySignature(fileBytes);
		}
		return mimeType;
	}

	private static String getMimeTypeByExtension(String fileName) {
		String extension = getFileExtension(fileName);
		if (extension == null || !EXTENSION_MAP.containsKey(extension.toLowerCase())) {
			return "application/octet-stream"; // 默认为二进制流
		}
		return EXTENSION_MAP.get(extension.toLowerCase());
	}

	private static String getMimeTypeBySignature(byte[] fileBytes) {
		for (Map.Entry<byte[], String> entry : SIGNATURE_MAP.entrySet()) {
			byte[] signature = entry.getKey();
			if (fileBytes.length >= signature.length && startsWith(fileBytes, signature)) {
				return entry.getValue();
			}
		}
		return "application/octet-stream"; // 默认为二进制流
	}

	private static boolean startsWith(byte[] source, byte[] prefix) {
		for (int i = 0; i < prefix.length; i++) {
			if (source[i] != prefix[i]) {
				return false;
			}
		}
		return true;
	}

	private static String getFileExtension(String fileName) {
		int lastIndexOfDot = fileName.lastIndexOf(".");
		if (lastIndexOfDot == -1) {
			return null;
		}
		return fileName.substring(lastIndexOfDot + 1).toLowerCase();
	}
}
