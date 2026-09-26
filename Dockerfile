# ==============================================================================
# Multi-stage Dockerfile para Despliegue en Render (Java 17 + Spring Boot 3)
# ==============================================================================

# ------------------------------------------------------------------------------
# Etapa 1: Construcción (Builder)
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copiar descriptores para aprovechar la caché de capas de Docker en dependencias
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar empaquetando el JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Etapa 2: Imagen de Ejecución (Runtime Ligera)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Crear usuario y grupo no privilegiados por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar únicamente el archivo JAR generado desde la etapa de construcción
COPY --from=builder /build/target/users-management-*.jar app.jar

# Cambiar permisos al usuario no root
RUN chown -R appuser:appgroup /app
USER appuser

# Parámetros optimizados para el plan gratuito de Render (512 MB RAM limit):
# - UseContainerSupport: Detecta los límites del contenedor
# - Xmx384m: Limita el Heap a 384 MB para dejar margen al Metaspace y evitar OOM killer
# - TieredStopAtLevel=1: Reduce drásticamente el uso de memoria en compilación JIT
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -Xmx384m -Xms128m -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8"

# Puerto por defecto (Render inyectará la variable $PORT automáticamente)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
