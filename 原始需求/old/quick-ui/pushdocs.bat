@echo off
chcp 65001
:: 执行pnpm构建命令
echo 正在执行 pnpm 构建...
pnpm -C docs build && cd docs/.vitepress/dist && echo  build ... && git init && git add . && git commit -m "commit docs" && git push -f https://gitee.com/rainsoil/quickboot.git master:gh-pages && echo "success"
:: 切换到dist目录

