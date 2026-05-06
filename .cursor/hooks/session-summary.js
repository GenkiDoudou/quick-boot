#!/usr/bin/env node
// session-end.js

const fs = require('fs');
const path = require('path');
const os = require('os');

async function main() {
  try {
    // 读取 stdin
    const chunks = [];
    for await (const chunk of process.stdin) {
      chunks.push(chunk);
    }
    const input = JSON.parse(Buffer.concat(chunks).toString());

    // 提取字段
    const session_id = input.session_id;
    const reason = input.reason || 'unknown';
    const duration_ms = input.duration_ms || 0;
    const user_email = input.user_email || 'unknown';

    // 状态目录和文件
    const STATE_DIR = path.join('.cache', 'hooks', 'state');
    const STATS_FILE = path.join(STATE_DIR, `conversation-${session_id}.json`);

    // 检查统计文件是否存在
    if (fs.existsSync(STATS_FILE)) {
      // 读取统计数据
      const stats = JSON.parse(fs.readFileSync(STATS_FILE, 'utf8'));

      const saved_user_email = stats.user_email || 'unknown';
      const modified_count = stats.modified_files?.length || 0;
      const new_count = stats.new_files?.length || 0;
      const total_lines = stats.total_lines_changed || 0;
      const start_time = stats.start_time || 'unknown';

      // 计算会话时长（分钟）
      const duration_min = Math.floor(duration_ms / 60000);

      // 生成摘要报告
      const summary = `
=================================
📊 会话统计摘要
=================================
👤 用户邮箱: ${saved_user_email}
🆔 会话ID: ${session_id}
⏱️  开始时间: ${start_time}
⌛ 会话时长: ${duration_min} 分钟
🏁 结束原因: ${reason}
---------------------------------
✏️  修改文件数: ${modified_count}
➕ 新增文件数: ${new_count}
📝 总变更行数: ${total_lines}
=================================
`;

      // 输出到控制台
      console.log(summary);

      // 保存到会话摘要日志
      const summaryLogPath = path.join(STATE_DIR, 'session-summary.log');
      fs.appendFileSync(summaryLogPath, summary);

      // 记录到通用日志
      const logPath = path.join(os.tmpdir(), 'cursor-hooks.log');
      const logMessage = `📋 会话结束 | 用户: ${saved_user_email} | 会话: ${session_id} | 修改: ${modified_count}个文件 | 新增: ${new_count}个文件 | 变更: ${total_lines}行 | 时长: ${duration_min}分钟\n`;
      fs.appendFileSync(logPath, logMessage);

      // 可选：清理统计文件
      // fs.unlinkSync(STATS_FILE);
    }

    process.exit(0);

  } catch (error) {
    console.error('Hook failed:', error);
    process.exit(0);
  }
}

main();