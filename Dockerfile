# syntax=docker/dockerfile:1
# ---- Stage Build ---- #
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
#-- Copy file build sebelum source code & Kasih permission execute --#
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# copy source code
COPY src/ src/
# Folder .m2 disimpan di cache BuildKit, di luar layer image, Batch mode (-B) lalu skip test waktu build
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B package -DskipTests

# Pecah fat JAR jadi folder per layer
RUN cp target/*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---- Run Time ---- #
FROM eclipse-temurin:21-jre
WORKDIR /application

# Jalankan app sebagai user non-root
RUN useradd --system --uid 1001 spring
user spring

COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

# maximal heap 75%
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

#port
EXPOSE 8080
# exec app
ENTRYPOINT ["java", "-jar", "application.jar"]