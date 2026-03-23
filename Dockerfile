# Multi-stage Dockerfile for Java 17 Maven multi-module project
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . /workspace

# Build the project (skip tests in container builds for speed)
RUN mvn -B -DskipTests package

# Collect produced JAR (attempt common names) into /workspace/dist/app.jar
RUN mkdir -p /workspace/dist && bash -lc "\
  ART=$(find . -type f \( -name '*-jar-with-dependencies.jar' -o -name '*-shaded.jar' -o -name '*.jar' \) | grep -v '/original-' | head -n 1) ; \
  if [ -z \"$ART\" ]; then echo 'No jar found after build' >&2; exit 1; fi ; \
  cp \"$ART\" /workspace/dist/app.jar"

FROM eclipse-temurin:17-jre-jammy
RUN groupadd --system app && useradd --system --gid app --create-home app
WORKDIR /opt/app
COPY --from=build /workspace/dist/app.jar /opt/app/app.jar
RUN chown -R app:app /opt/app
USER app

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s CMD java -jar /opt/app/app.jar --healthcheck || exit 1

ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
