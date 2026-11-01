#!/usr/bin/env bash
# 生产业务 SQL 发布脚本：按版本号顺序执行 member/mall/trade 建表脚本与系统菜单脚本。
# 默认只打印执行计划；确认后加 --apply 才会写库。执行前必须备份目标库。
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
APPLY=false
for arg in "$@"; do
  case "$arg" in
    --apply) APPLY=true ;;
    -h|--help)
      echo "用法: DB_HOST=127.0.0.1 DB_USER=root DB_PASSWORD=*** $0 [--apply]"
      exit 0
      ;;
    *) echo "未知参数: $arg" >&2; exit 2 ;;
  esac
done

DB_HOST="${DB_HOST:?请设置 DB_HOST}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:?请设置 DB_USER}"
DB_PASSWORD="${DB_PASSWORD:?请设置 DB_PASSWORD}"
MEMBER_DB="${MEMBER_DB:-lemnt_member}"
MALL_DB="${MALL_DB:-lemnt_mall}"
TRADE_DB="${TRADE_DB:-lemnt_trade}"
SYSTEM_DB="${SYSTEM_DB:-lemnt_system}"

MYSQL=(mysql --protocol=TCP -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" --default-character-set=utf8mb4)
if [[ -n "$DB_PASSWORD" ]]; then
  MYSQL+=("-p$DB_PASSWORD")
fi

run_sql() {
  local db="$1" file="$2"
  echo "[$db] $file"
  if $APPLY; then
    "${MYSQL[@]}" "$db" < "$file"
  fi
}

run_ordered() {
  local db="$1" dir="$2"
  while IFS= read -r file; do
    run_sql "$db" "$file"
  done < <(find "$dir" -maxdepth 1 -type f -name 'V*.sql' | sort -V)
}

echo "目标: $DB_HOST:$DB_PORT user=$DB_USER apply=$APPLY"
echo "库: member=$MEMBER_DB mall=$MALL_DB trade=$TRADE_DB system=$SYSTEM_DB"

if $APPLY; then
  for db in "$MEMBER_DB" "$MALL_DB" "$TRADE_DB"; do
    "${MYSQL[@]}" -e "CREATE DATABASE IF NOT EXISTS \`$db\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci"
  done
fi

run_ordered "$MEMBER_DB" "$ROOT/lemnt-member/sql"
run_ordered "$MALL_DB" "$ROOT/lemnt-mall/sql"
run_ordered "$TRADE_DB" "$ROOT/lemnt-trade/sql"
# 菜单脚本依赖已存在的 system 基础表，不在此脚本内重建 system 库。
run_ordered "$SYSTEM_DB" "$ROOT/sql"

if ! $APPLY; then
  echo
  echo "以上为执行计划；确认备份后重新运行并加 --apply。"
fi
