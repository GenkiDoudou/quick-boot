// auto-retry.js
const fs = require('fs');

async function main() {
    // 读取 stdin
    const chunks = [];
    for await (const chunk of process.stdin) {
        chunks.push(chunk);
    }
    const input = JSON.parse(Buffer.concat(chunks).toString());

    const { status, loop_count } = input;
    const response = {};

    // 如果状态是错误且循环次数小于限制
    if (status === 'error' && loop_count < 5) {
        response.followup_message = '继续执行，请尝试解决上述错误。';
    }

    process.stdout.write(JSON.stringify(response) + '\n');
}

main().catch(error => {
    console.error('Hook failed:', error);
    process.stdout.write('{}\n');
});