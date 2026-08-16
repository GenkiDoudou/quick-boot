/*
 * 文件分类压缩参数落库：min-size / quality / max-edge。
 * 影响：sys_file_classify；YAML qc.file.compress 仍作默认值与服务端总开关。
 * 依赖：V20__sys_file_and_classify.sql
 */

ALTER TABLE sys_file_classify
  ADD COLUMN compress_min_size_kb INT NOT NULL DEFAULT 200 COMMENT '超过该 KB 才压缩' AFTER compress_enabled,
  ADD COLUMN compress_quality DECIMAL(3, 2) NOT NULL DEFAULT 0.85 COMMENT 'JPEG 质量 0.10-1.00' AFTER compress_min_size_kb,
  ADD COLUMN compress_max_edge INT NOT NULL DEFAULT 1920 COMMENT '最长边像素；0 表示不限制' AFTER compress_quality;
