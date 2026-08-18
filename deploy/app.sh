#!/bin/sh
# 目标机旁路启停：nohup 跑当前目录下唯一 jar。
# 用法见文末 usage()。不覆盖外部 yml；备份目录为 ./back 。
#
# 可选参数（不传则用默认值）：
#   --xms 256M          堆初始大小
#   --xmx 256M          堆上限
#   --profile prod      spring.profiles.active
#   --keep 5            back/ 下按时间戳备份最多保留份数，超出删最旧

. /etc/profile

cd "$(dirname "$0")" || exit 1

XMS="256M"
XMX="256M"
PROFILE="prod"
KEEP="5"
ROLLBACK_TARGET=""

usage() {
  echo "Usage: $0 {start|stop|restart|rollback} [options] [backup-file]"
  echo "  --xms 256M         堆 -Xms（默认 256M）"
  echo "  --xmx 256M         堆 -Xmx（默认 256M）"
  echo "  --profile prod     --spring.profiles.active（默认 prod）"
  echo "  --keep 5           备份保留条数（默认 5）"
  echo "  rollback           恢复 back/ 中最新一份并启动"
  echo "  rollback <file>    恢复指定备份（路径或 back/ 下文件名）并启动"
}

# 当前目录有且仅有一个 jar，否则无法确定发布物
resolve_jar() {
  # shellcheck disable=SC2012
  FILE=$(ls -1 ./*.jar 2>/dev/null | awk 'END { if (NR==1) print; }')
  if [ -z "$FILE" ]; then
    echo "当前目录没有 jar 文件" >&2
    exit 1
  fi
  jar_count=$(ls -1 ./*.jar 2>/dev/null | wc -l)
  jar_count=$(echo "$jar_count" | tr -d ' ')
  if [ "$jar_count" -ne 1 ]; then
    echo "当前目录只能有一个 jar（现有 ${jar_count} 个）" >&2
    exit 1
  fi
  FILE=$(basename "$FILE")
}

# 解析 start/restart/rollback 后的 --key value；rollback 还可跟备份文件
parse_opts() {
  while [ $# -gt 0 ]; do
    case "$1" in
      --xms)
        XMS="$2"
        shift 2
        ;;
      --xmx)
        XMX="$2"
        shift 2
        ;;
      --profile)
        PROFILE="$2"
        shift 2
        ;;
      --keep)
        KEEP="$2"
        shift 2
        ;;
      --xms=*)
        XMS="${1#*=}"
        shift
        ;;
      --xmx=*)
        XMX="${1#*=}"
        shift
        ;;
      --profile=*)
        PROFILE="${1#*=}"
        shift
        ;;
      --keep=*)
        KEEP="${1#*=}"
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      --*)
        echo "未知参数: $1" >&2
        usage
        exit 1
        ;;
      *)
        if [ -n "$ROLLBACK_TARGET" ]; then
          echo "多余参数: $1" >&2
          exit 1
        fi
        ROLLBACK_TARGET="$1"
        shift
        ;;
    esac
  done
  case "$KEEP" in
    ''|*[!0-9]*)
      echo "--keep 必须是正整数，当前: ${KEEP}" >&2
      exit 1
      ;;
  esac
  if [ "$KEEP" -lt 1 ]; then
    echo "--keep 至少为 1" >&2
    exit 1
  fi
}

# 备份当前 jar，并删除超出 KEEP 的最旧文件（文件名含 YYYYMMDDHHMMSS，按名字排序即可）
backup_and_prune() {
  [ -d back ] || mkdir back
  DATA=$(date +%Y%m%d%H%M%S)
  cp "${FILE}" "back/${FILE}.${DATA}"
  echo "已备份 ${FILE} -> back/${FILE}.${DATA}"
  prune_backups
}

prune_backups() {
  [ -d back ] || return 0
  ls -1 "back/${FILE}."* 2>/dev/null | sort | awk -v keep="$KEEP" '
    { files[++n] = $0 }
    END {
      for (i = 1; i <= n - keep; i++) print files[i]
    }
  ' | while IFS= read -r old; do
    [ -n "$old" ] || continue
    rm -f "$old"
    echo "已删除过期备份 ${old}"
  done
}

start() {
  if [ -f ./run.pid ]; then
    pid=$(cat ./run.pid)
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      echo "已在运行 PID=${pid}，请先 stop 或使用 restart" >&2
      exit 1
    fi
    rm -f ./run.pid
  fi
  [ -d dump ] || mkdir dump
  nohup java -Xms"${XMS}" -Xmx"${XMX}" \
    -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./dump \
    -jar "./${FILE}" --spring.profiles.active="${PROFILE}" \
    > ./console.log 2>&1 &
  echo $! > ./run.pid
}

stop() {
  if [ ! -f ./run.pid ]; then
    echo "未找到 run.pid，视为未运行"
    return 0
  fi
  processId=$(cat ./run.pid)
  echo "$processId"
  if [ -n "$processId" ]; then
    kill -9 "$processId" 2>/dev/null || true
  fi
  rm -f ./run.pid
}

# 按修改时间取 back/ 中最新备份；默认跳过与当前 jar 内容相同的文件（start 会先备份当前包）。
latest_backup() {
  mode="$1"
  listf="./back/.ls.$$"
  [ -d back ] || return 0
  ls -1t "back/${FILE}."* 2>/dev/null > "$listf" || true
  found=""
  while IFS= read -r f; do
    [ -f "$f" ] || continue
    if [ "$mode" = "any" ] || ! cmp -s "$f" "./${FILE}"; then
      found="$f"
      break
    fi
  done < "$listf"
  rm -f "$listf"
  [ -n "$found" ] && echo "$found"
}

# 用备份覆盖当前 jar。ROLLBACK_TARGET 为空时默认 back/ 中最新一条（与当前 jar 不同）。
rollback() {
  [ -d back ] || mkdir back
  if [ -z "$ROLLBACK_TARGET" ]; then
    ROLLBACK_TARGET=$(latest_backup)
    if [ -z "$ROLLBACK_TARGET" ]; then
      ROLLBACK_TARGET=$(latest_backup any)
    fi
    if [ -z "$ROLLBACK_TARGET" ]; then
      echo "ROLLBACK_TARGET 为空，且 back/ 中没有 ${FILE}.* 备份可回滚" >&2
      exit 1
    fi
    echo "ROLLBACK_TARGET 为空，默认使用最新备份: ${ROLLBACK_TARGET}"
  fi
  if [ -f "$ROLLBACK_TARGET" ]; then
    src="$ROLLBACK_TARGET"
  elif [ -f "back/$ROLLBACK_TARGET" ]; then
    src="back/$ROLLBACK_TARGET"
  else
    echo "找不到备份: ${ROLLBACK_TARGET}" >&2
    exit 1
  fi
  echo "回滚使用 ${src}"
  cp "$src" "./${FILE}"
}

CMD="$1"
[ -n "$CMD" ] || {
  usage
  exit 1
}
shift
parse_opts "$@"
resolve_jar

case "$CMD" in
  start)
    backup_and_prune
    start
    echo "启动完成 PID=$(cat ./run.pid) Xms=${XMS} Xmx=${XMX} profile=${PROFILE}"
    ;;
  stop)
    stop
    echo "关闭完成"
    ;;
  restart)
    stop
    echo "####################"
    sleep 3
    backup_and_prune
    start
    echo "启动完成 PID=$(cat ./run.pid) Xms=${XMS} Xmx=${XMX} profile=${PROFILE}"
    ;;
  rollback)
    stop
    sleep 1
    rollback
    start
    echo "回滚并启动完成 PID=$(cat ./run.pid) 当前 jar=${FILE}"
    ;;
  *)
    usage
    exit 1
    ;;
esac
