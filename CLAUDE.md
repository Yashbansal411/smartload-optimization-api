# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Compile
mvn compile

# Run
mvn exec:java -Dexec.mainClass="com.example.Main"

# Package as JAR
mvn package

# Clean build artifacts
mvn clean
```

## Project Overview

Maven Java 21 project (`com.example / TeleportAssignment`). The single entry point is `src/main/java/com/example/Main.java`. No dependencies are declared yet beyond the standard Maven defaults — all application logic will live under `src/main/java/com/example/`.

## Key Facts

- Java 21, Maven build system
- No test framework is configured yet (`pom.xml` has no dependencies); add JUnit 5 or similar before writing tests
- IDE: IntelliJ IDEA (`.idea/` present)
