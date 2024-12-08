LOCAL_VERSION = $(shell git describe --tags --always)
VERSION ?= "0.1.0-$(LOCAL_VERSION)"
export MAVEN_OPTS=-Xmx2048m
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:6543/wherewasi
export JDBC_DATABASE_USERNAME=wherewasi
export JDBC_DATABASE_PASSWORD=wherewasi

build:
	mvnd verify -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

run:
	mvn spring-boot:run -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

prod:
	mvnd clean package -Pproduction -Dvaadin.force.production.build=true -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

native:
	mvn -Pproduction -Pnative native:compile -Dparallel=all -DperCoreThreadCount=false -DthreadCount=16 -T 1C -e

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