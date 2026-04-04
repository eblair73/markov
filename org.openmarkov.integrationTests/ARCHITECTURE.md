# org.openmarkov.integrationTests — Architecture

**Role in the project:** End-to-end tests that verify the entire system works together correctly.
**Source files:** ~25 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Types of Tests](#types-of-tests)
3. [Test Infrastructure](#test-infrastructure)
4. [Special JVM Settings](#special-jvm-settings)
5. [Running the Tests](#running-the-tests)

---

## What This Module Does

Individual modules have their own unit tests that verify one component in isolation. This module goes further: it tests complete workflows that cross module boundaries, catching bugs that only appear when modules interact.

**Why integration tests matter:** A unit test might verify that `ProbNetXMLReader` correctly parses a file, and a separate unit test might verify that `HuginPropagation` correctly computes a posterior. But only an integration test can catch a bug where the reader produces a `ProbNet` that inference handles incorrectly.

---

## Types of Tests

### Full Workflow Tests

These tests exercise a complete pipeline end-to-end:

1. **I/O → Inference**: Read a `.pgmx` network file, run inference with evidence, and verify that posterior probabilities match expected values computed analytically.
2. **I/O → Learning → I/O**: Read a case database, run a learning algorithm, write the learned network to disk, read it back, and verify the structure is preserved.
3. **I/O → Inference → Learning → Evaluation**: Read a network, generate a synthetic dataset, learn from that dataset, evaluate with cross-validation, verify that accuracy metrics fall within expected ranges.

### GUI Integration Tests

Using **AssertJ-Swing**, these tests simulate a real user interacting with the GUI:

- Open a network file using the File → Open menu.
- Click on a node to select it.
- Edit its CPT using the properties dialog.
- Run inference and verify that probability bars update.
- Use File → Save to write the modified network.

AssertJ-Swing drives the actual Swing components programmatically (it simulates mouse clicks, keyboard input, and dialog interactions) in a **headless** mode — no display is needed, so these tests can run on a server.

### Localization Tests

These tests verify that every string key referenced in Java source code has a corresponding entry in both `core_en.xml` and `core_es.xml`, and that there are no orphaned keys in the XML files.

### Code Quality Tests

Several tests enforce project-wide conventions:

- **Exception usage**: verify that all thrown exceptions are subclasses of `OpenMarkovException` or `OpenMarkovRuntimeException` (the project's exception hierarchy).
- **Annotation completeness**: verify that every class annotated with a plugin annotation (`@ProbNetFormatType`, `@LearningAlgorithmType`, etc.) is registered correctly.

---

## Test Infrastructure

| Tool | Version | Purpose |
|------|---------|---------|
| JUnit 5 | 5.13.0 | Test framework |
| AssertJ | 4.0.0-M1 | Fluent assertions (`assertThat(x).isEqualTo(y)`) |
| AssertJ-Swing | 3.17.1 | GUI test automation (simulates mouse/keyboard) |

Test fixture network files (`.pgmx`, `.xml`, `.elv`) are in `src/test/resources/nets/`.

---

## Special JVM Settings

AssertJ-Swing uses Java reflection to access private fields of Swing components. Starting with Java 9, the module system (JPMS) restricts cross-module reflection by default. This test module's `pom.xml` passes the following JVM argument when running tests:

```
--add-opens java.base/java.util=ALL-UNNAMED
```

This grants the test code the reflection access it needs. This setting applies **only when running tests** and does not affect the production application.

---

## Running the Tests

```bash
cd org.openmarkov.integrationTests
mvn test
```

Because GUI tests start Swing windows (even in headless mode), these tests are slower than unit tests. Run them after making significant changes that span multiple modules.

To run only the integration tests as part of a full project build:

```bash
cd org.openmarkov
mvn verify
```

The `verify` phase runs all tests including integration tests; `test` skips the integration test module by default in some project configurations.
