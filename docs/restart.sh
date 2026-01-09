#!/bin/bash

# 定义文件路径
DIR="/app/java/ai-task"
JAR_NAME="ai-biz-1.0.0.jar"
NEW_JAR="ai-biz-1.0.0-SNAPSHOT.jar"
BACKUP_JAR="ai-biz-1.0.0.jar.bak"
LOG_FILE="console.log"
# 获取当前日期和时间，格式为 yyyyMMdd_HHmmss
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 获取当前运行的进程ID
PID=$(ps aux | grep "java -jar $JAR_NAME" | grep -v grep | awk '{print $2}')

# 进入目录下
cd $DIR

# 1. 停止当前正在运行的 jar 包
if [ -n "$PID" ]; then
  echo "停止当前进程 (PID: $PID)..."
  kill -9 $PID
  echo "进程已停止."
else
  echo "未找到正在运行的进程 $JAR_NAME."
fi

# 2. 检查是否有新的 jar 包
if [ -f "$NEW_JAR" ]; then
  echo "发现新包: $NEW_JAR"

  # 备份当前 jar 包
  if [ -f "$JAR_NAME" ]; then
    BACKUP_FILE="$JAR_NAME.bak.$TIMESTAMP"
    echo "备份JAR: $BACKUP_FILE..."
    mv "$JAR_NAME" "$BACKUP_FILE"
  fi

  # 将新 JAR 重命名为 ai-biz-1.0.0.jar
  echo "启用新包 $NEW_JAR -> $JAR_NAME..."
  mv "$NEW_JAR" "$JAR_NAME"
fi

# 3. 重新启动 jar 包
if [ -f "$JAR_NAME" ]; then
  echo "启动程序 $JAR_NAME..."
  nohup java -jar "$JAR_NAME" > "$LOG_FILE" 2>&1 &
  echo "  $JAR_NAME 启动成功..."
else
  echo "未找到可启动的 JAR 包 $JAR_NAME."
fi

# 4. 删除多余备份
find $DIR -ctime +1 -type f -name "$JAR_NAME.bak.*" | xargs rm -rf {}

# 查看日志
tail -f "$LOG_FILE"