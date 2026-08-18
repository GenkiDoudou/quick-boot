#!/usr/bin/env bash
# 在目标机幂等发布 Spring Boot jar：建目录、备份旧包、安装 systemd（若不存在）、重启。
# 由 Jenkinsfile.quickboot 经 SSH 调用；不覆盖已有 application-prod.yml 与已有 unit 文件。
#
# 必填环境变量：
#   NEW_JAR       已上传的新 jar 绝对路径
#   DEPLOY_DIR    安装目录，如 /opt/quickboot/app
#   JAR_NAME      落盘文件名，如 quickboot-app.jar
#   SERVICE_NAME  systemd 单元名（不含 .service）
#   RUN_USER      运行用户（同时作为文件属主）
# 可选：
#   CONFIG_DIR    Spring 外部配置目录，默认 DEPLOY_DIR 的同级 config（/opt/quickboot/app → /opt/quickboot/config）
#   BACKUP_KEEP   备份保留份数，默认 5
#   JAVA_BIN      java 可执行文件，默认 /usr/bin/java

set -euo pipefail

need() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    echo "缺少环境变量: ${name}" >&2
    exit 1
  fi
}

need NEW_JAR
need DEPLOY_DIR
need JAR_NAME
need SERVICE_NAME
need RUN_USER

CONFIG_DIR="${CONFIG_DIR:-$(dirname "${DEPLOY_DIR}")/config}"
BACKUP_KEEP="${BACKUP_KEEP:-5}"
JAVA_BIN="${JAVA_BIN:-/usr/bin/java}"
BACKUP_DIR="${DEPLOY_DIR}/backup"
TARGET_JAR="${DEPLOY_DIR}/${JAR_NAME}"
UNIT_PATH="/etc/systemd/system/${SERVICE_NAME}.service"

if [[ ! -f "${NEW_JAR}" ]]; then
  echo "新 jar 不存在: ${NEW_JAR}" >&2
  exit 1
fi

if [[ ! -x "${JAVA_BIN}" ]] && ! command -v "${JAVA_BIN}" >/dev/null 2>&1; then
  echo "未找到 Java: ${JAVA_BIN}（请先在目标机安装 JDK 17）" >&2
  exit 1
fi

if ! id "${RUN_USER}" >/dev/null 2>&1; then
  echo "创建系统用户 ${RUN_USER}"
  useradd --system --home "${DEPLOY_DIR}" --shell /usr/sbin/nologin "${RUN_USER}"
fi

mkdir -p "${DEPLOY_DIR}" "${BACKUP_DIR}" "${CONFIG_DIR}"

# 已有包则备份，并只保留最近 N 份
if [[ -f "${TARGET_JAR}" ]]; then
  stamp="$(date +%Y%m%d%H%M%S)"
  cp -a "${TARGET_JAR}" "${BACKUP_DIR}/${JAR_NAME}.${stamp}"
  echo "已备份 ${TARGET_JAR} -> ${BACKUP_DIR}/${JAR_NAME}.${stamp}"
  # 按文件名时间戳排序后删除超额备份
  shopt -s nullglob
  old_backups=("${BACKUP_DIR}/${JAR_NAME}."*)
  extra=$(( ${#old_backups[@]} - BACKUP_KEEP ))
  if (( extra > 0 )); then
    # 按文件名排序（含时间戳）后删最旧的
    IFS=$'\n' old_backups=($(printf '%s\n' "${old_backups[@]}" | sort))
    unset IFS
    for (( i = 0; i < extra; i++ )); do
      rm -f "${old_backups[$i]}"
    done
  fi
fi

install -m 0644 "${NEW_JAR}" "${TARGET_JAR}"
chown -R "${RUN_USER}:${RUN_USER}" "${DEPLOY_DIR}" "${CONFIG_DIR}"

# 仅在缺少 unit 时写入，避免覆盖运维手工修改
if [[ ! -f "${UNIT_PATH}" ]]; then
  echo "写入 systemd 单元 ${UNIT_PATH}"
  cat > "${UNIT_PATH}" <<EOF
[Unit]
Description=QuickBoot Spring Boot application
After=network.target

[Service]
Type=simple
User=${RUN_USER}
Group=${RUN_USER}
WorkingDirectory=${DEPLOY_DIR}
Environment=SPRING_CONFIG_ADDITIONAL_LOCATION=file:${CONFIG_DIR}/
Environment=JAVA_OPTS=-Xms256m -Xmx512m
ExecStart=${JAVA_BIN} \$JAVA_OPTS -jar ${TARGET_JAR} --spring.profiles.active=prod
SuccessExitStatus=143
Restart=on-failure
RestartSec=5
StandardOutput=journal
StandardError=journal
NoNewPrivileges=true

[Install]
WantedBy=multi-user.target
EOF
  systemctl daemon-reload
  systemctl enable "${SERVICE_NAME}"
else
  echo "已存在 ${UNIT_PATH}，不覆盖"
fi

systemctl restart "${SERVICE_NAME}"
echo "已重启 ${SERVICE_NAME}"
