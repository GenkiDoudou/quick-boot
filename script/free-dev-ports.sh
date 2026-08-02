#!/usr/bin/env bash
# 释放本地开发常用端口，解决 Spring Boot / 嵌入式 Redis 启动 BindException。
# 默认：9993 (HTTP)、6379 (embedded Redis)
#
# 用法（仓库根目录）:
#   bash script/free-dev-ports.sh
#   bash script/free-dev-ports.sh 9993 6379

set -euo pipefail

PORTS=("$@")
if [[ ${#PORTS[@]} -eq 0 ]]; then
  PORTS=(9993 6379)
fi

kill_port() {
  local port="$1"
  local pids=""
  if command -v lsof >/dev/null 2>&1; then
    pids=$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)
  elif command -v fuser >/dev/null 2>&1; then
    fuser -k "${port}/tcp" 2>/dev/null || true
    echo "  :${port}  fuser attempted"
    return 0
  else
    echo "  :${port}  need lsof or fuser" >&2
    return 1
  fi

  if [[ -z "${pids}" ]]; then
    echo "  :${port}  idle"
    return 0
  fi

  for pid in $pids; do
    kill -9 "$pid" 2>/dev/null || true
    echo "  :${port}  stopped PID ${pid}"
  done
}

echo "Freeing ports: ${PORTS[*]}"
for port in "${PORTS[@]}"; do
  kill_port "$port"
done

sleep 0.5
echo
echo "Recheck:"
busy=0
for port in "${PORTS[@]}"; do
  left=""
  if command -v lsof >/dev/null 2>&1; then
    left=$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)
  fi
  if [[ -z "${left}" ]]; then
    echo "  :${port}  free"
  else
    echo "  :${port}  still held by PID ${left}"
    busy=1
  fi
done

if [[ "$busy" -ne 0 ]]; then
  echo
  echo "Some ports remain busy." >&2
  exit 1
fi

echo
echo "Done. You can start the app:"
echo "  cd quickboot"
echo "  mvn -pl quickboot-web spring-boot:run"
