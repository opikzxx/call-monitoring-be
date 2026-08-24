#!/bin/sh
set -e

# Recompile on save in the background; DevTools (running inside the bootRun
# JVM below) watches the compiled output and restarts the app in-place.
./gradlew classes --continuous --console=plain &

exec ./gradlew bootRun --console=plain
