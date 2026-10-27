# syntax=docker/dockerfile:1

FROM node:20-bookworm-slim AS ui-build

ARG VITE_API_URL=/
ARG DTSTACK_ATTRIBUTION_HTML
WORKDIR /workspace/sqlparserx-ui

COPY sqlparserx-ui/package*.json ./
RUN npm ci

COPY sqlparserx-ui/ ./
COPY bin/quick/site/inject-attribution.mjs /workspace/inject-attribution.mjs
RUN VITE_API_URL="${VITE_API_URL}" npm run build:prod && \
    DTSTACK_ATTRIBUTION_HTML="${DTSTACK_ATTRIBUTION_HTML}" node /workspace/inject-attribution.mjs /workspace/sqlparserx-ui/dist


FROM node:20-bookworm-slim AS website-build

ARG DTSTACK_ATTRIBUTION_HTML
WORKDIR /workspace/website

COPY website/package*.json ./
RUN npm ci

COPY website/ ./
COPY conf/application.properties /workspace/application.properties
COPY sqlparserx-web/src/main/resources/static/sitemap.xml /workspace/app-sitemap.xml
COPY bin/quick/site/inject-attribution.mjs /workspace/inject-attribution.mjs
COPY bin/quick/site/merge-sitemaps.mjs /workspace/merge-sitemaps.mjs
RUN SEO_SITE_URL="$(sed -n 's/^[[:space:]]*seo\.site-url[[:space:]]*=[[:space:]]*//p' /workspace/application.properties | tail -n 1)" && \
    DOCUSAURUS_URL="${SEO_SITE_URL}" DOCUSAURUS_BASE_URL=/website/ npm run build && \
    DTSTACK_ATTRIBUTION_HTML="${DTSTACK_ATTRIBUTION_HTML}" node /workspace/inject-attribution.mjs /workspace/website/build && \
    node /workspace/merge-sitemaps.mjs /workspace/app-sitemap.xml /workspace/website/build/sitemap.xml /workspace/sitemap.xml "${SEO_SITE_URL}"


FROM maven:3.9.9-eclipse-temurin-17 AS backend-build

WORKDIR /workspace/sqlparserx
COPY . .

# The runtime image uses the directory-style package produced by assembly.xml.
RUN mvn -B clean package -DskipTests -Dspotless.check.skip=true
RUN sh bin/quick/site/render-seo-files.sh sqlparserx-web/src/main/resources/static conf/application.properties


FROM eclipse-temurin:8-jre

ENV APP_HOME=/opt/sqlparserx
WORKDIR ${APP_HOME}

COPY --from=backend-build /workspace/sqlparserx/sqlparserx-web/target/dtstack-platform/dtstack-platform/dtstack/ ./dtstack/
COPY --from=backend-build /workspace/sqlparserx/sqlparserx-web/target/dtstack-platform/dtstack-platform/lib/ ./lib/
COPY conf/application.properties conf/logback.xml ./conf/
COPY --from=ui-build /workspace/sqlparserx-ui/dist/ ./conf/static/
COPY --from=backend-build /workspace/sqlparserx/sqlparserx-web/src/main/resources/static/ ./conf/static/
COPY --from=website-build /workspace/sitemap.xml ./conf/static/sitemap.xml
COPY --from=website-build /workspace/website/build/ ./website/

EXPOSE 16758

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -Duser.dir.conf=${APP_HOME}/conf -Duser.dir.logs=${APP_HOME}/logs -cp \"${APP_HOME}/dtstack/*:${APP_HOME}/lib/*\" com.dtstack.sqlparserx.SqlParserXWebApplication"]
