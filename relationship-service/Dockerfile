FROM maven:3.8.4-openjdk-11 AS build
WORKDIR /app

# 先安装父POM
COPY pom.xml /app/parent-pom.xml
RUN mvn -f /app/parent-pom.xml install -N

# 然后处理关系服务模块
COPY relationship-service/pom.xml /app/pom.xml
RUN mvn dependency:go-offline

COPY relationship-service/src/ /app/src/
RUN mvn package -DskipTests

FROM adoptopenjdk:11-jre-hotspot
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]