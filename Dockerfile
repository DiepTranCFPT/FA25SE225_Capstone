FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Cài đặt font nếu cần cho báo cáo/pdf (tùy chọn)
RUN apk add --no-cache fontconfig ttf-dejavu

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]