LOCAL_VERSION = $(shell git describe --tags --always)
VERSION ?= "0.1.0-$(LOCAL_VERSION)"
export MAVEN_OPTS=-Xmx2048m
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:6543/wherewasi
export JDBC_DATABASE_USERNAME=wherewasi
export JDBC_DATABASE_PASSWORD=wherewasi

build:
	mvnd verify -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

test:
	mvnd test -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

run:
	mvn spring-boot:run -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

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
native:
	mvn -Pproduction -Pnative native:compile -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

nrun:
	./target/where-was-i \
		-Dspring.profiles.active=production \
		-Djdbc.database.url=jdbc:postgresql://localhost:6543/wherewasi \
		-Djdbc.database.username=wherewasi \
		-Djdbc.database.password=wherewasi
# Spring Boot Native --> cannot work with Vaadin?
#natives:
#	mvn -Pproduction -Pnative spring-boot:build-image -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

docker:
	docker buildx build . --platform linux/amd64 --tag ghcr.io/joostvdg/where-was-i:${VERSION} --provenance=false --sbom=false --push

drun:
	docker run -i --rm \
      --name where-was-i \
      -p 8080:8080 \
      -e SPRING_PROFILES_ACTIVE=production \
      -e JDBC_DATABASE_URL=jdbc:postgresql://where-was-i-postgres-1:5432/wherewasi \
      --network where-was-i_default \
      ghcr.io/joostvdg/where-was-i:${VERSION}