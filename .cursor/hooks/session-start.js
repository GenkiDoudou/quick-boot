#!/usr/bin/env node
// session-start.js

const fs = require('fs');
const path = require('path');

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
    const user_email = input.user_email || 'unknown';
    const composer_mode = input.composer_mode || 'unknown';
    const is_background = input.is_background_agent || false;

    // 创建状态目录
    const STATE_DIR = '.cache/hooks/state';
    if (!fs.existsSync(STATE_DIR)) {
      fs.mkdirSync(STATE_DIR, { recursive: true });
    }

    // 创建会话统计文件
    const STATS_FILE = path.join(STATE_DIR, `conversation-${session_id}.json`);
    const stats = {
      session_id: session_id,
      user_email: user_email,
      composer_mode: composer_mode,
      is_background_agent: is_background,
      modified_files: [],
      new_files: [],
      total_lines_changed: 0,
      start_time: new Date().toISOString()
    };

    fs.writeFileSync(STATS_FILE, JSON.stringify(stats, null, 2));

    // 记录会话启动日志
    const logMessage = `🚀 会话启动 | 用户: ${user_email} | 会话ID: ${session_id} | 模式: ${composer_mode}\n`;
    fs.appendFileSync('/tmp/cursor-hooks.log', logMessage);

    // 输出响应
    console.log(JSON.stringify({ continue: true }));
    process.exit(0);

  } catch (error) {
    console.error('Hook failed:', error);
    console.log(JSON.stringify({ continue: true }));
    process.exit(0);
  }
}

main();