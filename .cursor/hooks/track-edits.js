#!/usr/bin/env node
// after-file-edit.js

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const os = require('os');

async function main() {
  try {
    // 读取 stdin
    const chunks = [];
    for await (const chunk of process.stdin) {
      chunks.push(chunk);
    }
    const input = JSON.parse(Buffer.concat(chunks).toString());

    // 解析数据
    const conversation_id = input.conversation_id;
    const file_path = input.file_path;
    const edits = input.edits || [];
    const user_email = input.user_email || 'unknown';

    // 状态文件目录
    const STATE_DIR = path.join('.cache', 'hooks', 'state');
    if (!fs.existsSync(STATE_DIR)) {
      fs.mkdirSync(STATE_DIR, { recursive: true });
    }

    const STATS_FILE = path.join(STATE_DIR, `conversation-${conversation_id}.json`);

    // 初始化或读取现有统计
    let current_stats;
    if (!fs.existsSync(STATS_FILE)) {
      current_stats = {
        user_email: user_email,
        modified_files: [],
        new_files: [],
        total_lines_changed: 0
      };
    } else {
      current_stats = JSON.parse(fs.readFileSync(STATS_FILE, 'utf8'));
      // 确保 user_email 被记录（首次或更新）
      current_stats.user_email = user_email;
    }

    // 检查文件是否为新文件
    let is_new_file = false;
    if (!fs.existsSync(file_path)) {
      is_new_file = true;
    } else {
      // 检查是否在 git 中
      try {
        const output = execSync(`git ls-files --error-unmask "${file_path}"`, {
          encoding: 'utf8',
          stdio: ['pipe', 'pipe', 'pipe']
        }).trim();
        is_new_file = output.length === 0;
      } catch (error) {
        // Git 命令失败，假设不是新文件
        is_new_file = false;
      }
    }

    // 计算修改的行数
    let lines_changed = 0;
    for (const edit of edits) {
      const old_str = edit.old_string || '';
      const new_str = edit.new_string || '';

      const old_lines = old_str.split('\n').length;
      const new_lines = new_str.split('\n').length;
      lines_changed += old_lines + new_lines;
    }

    // 更新统计数据
    if (is_new_file) {
      if (!current_stats.new_files.includes(file_path)) {
        current_stats.new_files.push(file_path);
      }
    } else {
      if (!current_stats.modified_files.includes(file_path)) {
        current_stats.modified_files.push(file_path);
      }
    }

    current_stats.total_lines_changed += lines_changed;

    // 保存更新后的统计
    fs.writeFileSync(STATS_FILE, JSON.stringify(current_stats, null, 2));

    // 输出当前统计
    const modified_count = current_stats.modified_files.length;
    const new_count = current_stats.new_files.length;
    const total_lines = current_stats.total_lines_changed;

    const logPath = path.join(os.tmpdir(), 'cursor-hooks.log');
    const logMessage = `📊 用户: ${user_email} | 会话统计: 修改 ${modified_count} 个文件, 新增 ${new_count} 个文件, 总计 ${total_lines} 行变更\n`;
    fs.appendFileSync(logPath, logMessage);

    process.exit(0);

  } catch (error) {
    console.error('Hook failed:', error);
    process.exit(0);
  }
}

main();