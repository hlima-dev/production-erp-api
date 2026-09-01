# Build em duas etapas: a imagem final não carrega o Maven nem o cache de
# dependências, só o JAR e um JRE — bem mais leve pra rodar em produção.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia só o pom.xml primeiro pra cachear as dependências numa layer
# separada — um rebuild que só mudou código Java não baixa tudo de novo.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/erp-producao-*.jar app.jar

# Render injeta a variável PORT — application.yml já lê server.port dela
# (com default 8080 pra rodar local).
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
