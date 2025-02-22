LOCAL_VERSION = $(shell git describe --tags --always)
VERSION ?= "0.1.4-$(LOCAL_VERSION)"
export MAVEN_OPTS=-Xmx2048m

## Local Postgres Config
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:6543/wherewasi
export JDBC_DATABASE_USERNAME=wherewasi
export JDBC_DATABASE_PASSWORD=wherewasi

## Local LDAP Config
export LDAP_ENABLED=true
export LDAP_URL=ldap://localhost:389/dc=example,dc=org
export LDAP_USER_DN_PATTERN=uid={0},ou=People
export LDAP_USER_SEARCH_FILTER=uid={0}
export LDAP_GROUP_SEARCH_BASE=ou=Groups
export LDAP_GROUP_ROLE_ATTRIBUTE=cn
export LDAP_MANAGER_DN=cn=admin,dc=example,dc=org
export LDAP_MANAGER_PASSWORD=admin


build:
	mvnd verify -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

test:
	mvnd test -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

spotless:
	mvnd spotless:apply

run:
	mvnd spring-boot:run -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

prod:
	mvnd clean package -Pproduction -Dvaadin.force.production.build=true -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

############################################
# Native Image
# Recommended options:
# G1GC: Use the G1 GC ('--gc=G1') for improved latency and throughput.
# PGO:  Use Profile-Guided Optimizations ('--pgo') for improved throughput.
# HEAP: Set max heap for improved and more predictable memory usage.
# CPU:  Enable more CPU features with '-march=native' for improved performance.
# QBM:  Use the quick build mode ('-Ob') to speed up builds during development.
# have to clean first, otherwise it will not work (There can only be one @PWA annotation and it must be set on the AppShellConfigurator implementor)
native:
	mvnd clean -Pproduction  -Dvaadin.force.production.build=true -Pnative native:compile -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

nrun:
	./target/where-was-i \
		-Dspring.profiles.active=production \
		-Djdbc.database.url=jdbc:postgresql://localhost:6543/wherewasi \
		-Djdbc.database.username=wherewasi \
		-Djdbc.database.password=wherewasi

ndocker:
	docker buildx build \
		-f Dockerfile.native \
		--platform linux/amd64 \
		--build-arg APP_FILE=where-was-i \
		--tag ghcr.io/joostvdg/where-was-i:${VERSION} \
		--provenance=false \
		--sbom=false \
		--push \
		.

# Spring Boot Native --> cannot work with Vaadin?
#natives:
#	mvn -Pproduction -Pnative spring-boot:build-image -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

#   docker build -f Dockerfiles/Dockerfile.native --build-arg APP_FILE=benchmark-jibber -t jibber-benchmark:native.0.0.1-SNAPSHOT .
docker:
	docker buildx build --platform linux/amd64 --tag ghcr.io/joostvdg/where-was-i:${VERSION} --provenance=false --sbom=false --push

drun:
	docker run -i --rm \
      --name where-was-i \
      -p 8080:8080 \
      -e SPRING_PROFILES_ACTIVE=production \
      -e JDBC_DATABASE_URL=jdbc:postgresql://where-was-i-postgres-1:5432/wherewasi \
      --network where-was-i_default \
      ghcr.io/joostvdg/where-was-i:${VERSION}

## jdbc:postgresql://young-dawn-2346.internal:5432/postgres