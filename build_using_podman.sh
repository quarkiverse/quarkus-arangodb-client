#!/usr/bin/env sh
export DOCKER_HOST=unix:///run/user/${UID}/podman/podman.sock
export TESTCONTAINERS_RYUK_DISABLED=true
mvn clean install