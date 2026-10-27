# syntax=docker/dockerfile:1

FROM node:20-bookworm-slim AS ui-build

ARG VITE_API_URL=/
WORKDIR /workspace/sqlparserx-ui

COPY sqlparserx-ui/package*.json ./
RUN npm ci

COPY sqlparserx-ui/ ./
RUN VITE_API_URL="${VITE_API_URL}" npm run build:prod


FROM node:20-bookworm-slim AS website-build

WORKDIR /workspace/website

COPY website/package*.json ./
RUN npm ci

COPY website/ ./
RUN DOCUSAURUS_BASE_URL=/website/ npm run build


FROM maven:3.9.9-eclipse-temurin-17 AS backend-build

WORKDIR /workspace/sqlparserx
COPY . .

# The runtime image uses the directory-style package produced by assembly.xml.
RUN mvn -B clean package -DskipTests -Dspotless.check.skip=true


FROM eclipse-temurin:8-jre

ENV APP_HOME=/opt/sqlparserx
WORKDIR ${APP_HOME}

COPY --from=backend-build /workspace/sqlparserx/sqlparserx-web/target/dtstack-platform/dtstack-platform/dtstack/ ./dtstack/
COPY --from=backend-build /workspace/sqlparserx/sqlparserx-web/target/dtstack-platform/dtstack-platform/lib/ ./lib/
COPY conf/application.properties conf/logback.xml ./conf/
COPY --from=ui-build /workspace/sqlparserx-ui/dist/ ./conf/static/
COPY --from=website-build /workspace/website/build/ ./website/

EXPOSE 16758

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -Duser.dir.conf=${APP_HOME}/conf -Duser.dir.logs=${APP_HOME}/logs -cp \"${APP_HOME}/dtstack/*:${APP_HOME}/lib/*\" com.dtstack.sqlparserx.SqlParserXWebApplication"]
