# 多阶段构建 - 优化依赖缓存
FROM maven:3.8-openjdk-11 AS dependencies

# 设置工作目录
WORKDIR /app

# 先复制POM文件（利用Docker层缓存）
COPY pom.xml .

# 复制各服务的POM文件
COPY eureka-server/pom.xml ./eureka-server/
COPY user-service/pom.xml ./user-service/
COPY relationship-service/pom.xml ./relationship-service/
COPY message-service/pom.xml ./message-service/
COPY notification-service/pom.xml ./notification-service/
COPY api-gateway/pom.xml ./api-gateway/
COPY frontend/pom.xml ./frontend/

# 下载依赖（这一层会被缓存，除非POM文件改变）
RUN mvn dependency:go-offline -B

# 构建阶段
FROM dependencies AS build

# 复制源代码
COPY . .

# 构建应用（跳过测试，使用并行构建）
RUN mvn clean package -DskipTests -T 4 -B

# 运行时阶段
FROM openjdk:11-jre-slim AS runtime

# 创建非root用户
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 appuser

WORKDIR /app

# 设置应用参数
ARG JAR_FILE
ARG SERVICE_NAME

# 从构建阶段复制JAR文件
COPY --from=build --chown=appuser:appgroup /app/${SERVICE_NAME}/target/*.jar app.jar

# 切换到非root用户
USER appuser

# 简化健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD test -f app.jar || exit 1

# JVM优化参数
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 启动应用
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar 