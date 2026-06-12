#!/usr/bin/env bash
# PostgreSQL + pgvector 一键部署（Linux / macOS / Git Bash）
# 用法：cd docker/pgvector && chmod +x deploy.sh && ./deploy.sh
# 可选：./deploy.sh down   停止容器（保留数据卷）
#       ./deploy.sh logs   跟踪日志

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

ENV_FILE="${SCRIPT_DIR}/.env"
ENV_EXAMPLE="${SCRIPT_DIR}/.env.example"

if [[ ! -f "$ENV_FILE" ]]; then
  if [[ -f "$ENV_EXAMPLE" ]]; then
    cp "$ENV_EXAMPLE" "$ENV_FILE"
    echo "已生成 .env（来自 .env.example），可按需修改密码与端口。"
  fi
fi

compose() {
  docker compose --env-file "$ENV_FILE" "$@"
}

case "${1:-up}" in
  down)
    echo "停止并移除容器（数据卷 pgvector_data 保留）..."
    compose down
    ;;
  logs)
    compose logs -f pgvector
    ;;
  up|*)
    echo "拉取镜像并启动 PostgreSQL + pgvector ..."
    compose pull
    compose up -d

    echo ""
    echo "等待健康检查 ..."
    for _ in $(seq 1 30); do
      status="$(docker inspect --format='{{.State.Health.Status}}' quickboot-pgvector 2>/dev/null || echo starting)"
      if [[ "$status" == "healthy" ]]; then
        echo "容器已就绪 (healthy)"
        break
      fi
      sleep 2
    done

    echo ""
    echo "连接信息："
    grep -E '^(POSTGRES_|PGVECTOR_)' "$ENV_FILE" 2>/dev/null || true
    echo "  JDBC: jdbc:postgresql://127.0.0.1:5433/quickboot_vector"
    echo ""
    echo "常用命令："
    echo "  查看日志: ./deploy.sh logs"
    echo "  停止服务: ./deploy.sh down"
    echo "  进入 psql: docker exec -it quickboot-pgvector psql -U vector -d quickboot_vector"
    ;;
esac
