FROM maven:3.8-openjdk-11 AS build

WORKDIR /app

# 复制所有必要文件
COPY pom.xml .
COPY user-service/ ./user-service/

# 直接构建应用（Maven会自动下载依赖）
RUN mvn -f user-service/pom.xml clean package -DskipTests -B -Dmaven.repo.local=/app/.m2

# 运行时阶段
FROM openjdk:11-jre-slim

# 创建用户
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 appuser

WORKDIR /app

# 复制JAR文件
COPY --from=build --chown=appuser:appgroup /app/user-service/target/*.jar app.jar

USER appuser

# 简化健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD test -f app.jar || exit 1

# JVM优化
ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -XX:+UseContainerSupport"

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"] 