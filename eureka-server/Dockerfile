FROM maven:3.8.4-openjdk-11 AS build
WORKDIR /app

# 先安装父POM
COPY pom.xml /app/parent-pom.xml
RUN mvn -f /app/parent-pom.xml install -N

# 然后处理Eureka服务模块
COPY eureka-server/pom.xml /app/pom.xml
RUN mvn dependency:go-offline

COPY eureka-server/src/ /app/src/
RUN mvn package -DskipTests

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","app.jar"] 