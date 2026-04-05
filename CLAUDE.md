# OpenMarkov — Agent Instructions

## Documentation maintenance

Whenever you make a change to code, configuration, or project structure, update every relevant documentation file before closing the task:

- **README.md** — update any build, run, or setup instructions that your change affects.
- **ARCHITECTURE.md** — update the system overview, module map, or layer diagram if you add, remove, or significantly restructure a module.
- **`<module>/ARCHITECTURE.md`** — update the per-module architecture doc for every module you touch. Each module has its own `ARCHITECTURE.md` alongside its `pom.xml`.

If a doc file does not yet exist for a new module you create, write one that matches the style of the existing module architecture docs.

## Project structure

This is a flat Maven multi-module project. All modules are sibling directories inside `markov/`. The parent POM lives in `org.openmarkov/`. There is no root-level aggregator POM.

## Build

```bash
# Install the parent POM (once, or after parent changes)
cd org.openmarkov && mvn install -DskipTests && cd ..

# Build the executable JAR
cd org.openmarkov.full && mvn clean package -DskipTests
```

## Run

```bash
# From inside org.openmarkov.full/
java -jar target/openmarkov-full-0.3.0-SNAPSHOT-jar-with-dependencies.jar
```
