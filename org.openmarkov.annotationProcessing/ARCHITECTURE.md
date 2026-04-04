# org.openmarkov.annotationProcessing — Architecture

**Role in the project:** Generates type-safe string-lookup code at compile time for the localization system.
**Source files:** ~5 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Background: Annotations in Java](#background-annotations-in-java)
3. [The Problem Being Solved](#the-problem-being-solved)
4. [How It Works](#how-it-works)
5. [Key Classes](#key-classes)
6. [When This Module Runs](#when-this-module-runs)

---

## What This Module Does

This module provides a **Java annotation processor** that runs automatically during compilation to generate boilerplate code that links Java class names to their internationalized (i18n) string keys. In plain English: it removes the need to write repetitive "string lookup" code by hand, and catches typos in string keys at compile time rather than at runtime.

---

## Background: Annotations in Java

An **annotation** in Java is a marker that you attach to a class, method, or field using the `@` symbol. For example, `@Override` is a built-in annotation that tells the compiler you intend to override a parent method.

You can define your own custom annotations. An **annotation processor** is a piece of code that the Java compiler calls during compilation whenever it finds a matching annotation. The processor can inspect the annotated code and generate new source files as a side effect.

This module defines two custom annotations and one processor:

| Name | Type | Purpose |
|------|------|---------|
| `@BindLocalizations` | Annotation | Mark a class whose string keys should be bound |
| `@ClassLocalizable` | Annotation | Mark a class as providing localized string resources |
| `BindLocalizationsProcessor` | Processor | Scans for `@BindLocalizations` classes; generates binding code |

---

## The Problem Being Solved

Throughout OpenMarkov, code needs to look up user-visible strings by key. The naive approach is:

```java
String message = StringDatabase.getUniqueInstance().getString("Exceptions.err.invalidNode");
```

This has two problems:

1. **Typos are silent**: if you mistype `"Exceptions.err.invalidNode"`, the error only appears at runtime (a missing string key), not at compile time.
2. **Refactoring is error-prone**: if you rename the key in the XML file, you must manually update every `.getString("...")` call.

The annotation processor solves both problems by generating a companion class with strongly-typed constants:

```java
// Generated code (you do not write this by hand)
public class ExceptionStrings {
    public static final String ERR_INVALID_NODE =
        StringDatabase.getUniqueInstance().getString("Exceptions.err.invalidNode");
}
```

Now call sites look like:

```java
String message = ExceptionStrings.ERR_INVALID_NODE;
```

If the key is removed from the XML, the generated constant disappears, and the compiler reports an error at every call site — catching the problem before the application even runs.

---

## How It Works

1. During `mvn compile`, the Java compiler invokes `BindLocalizationsProcessor`.
2. The processor scans the classpath for all classes annotated with `@BindLocalizations` or `@ClassLocalizable`.
3. For each annotated class, it reads the corresponding localization XML file (e.g., `core_en.xml`) to extract the list of string keys.
4. It generates a new Java source file containing one constant per key.
5. The generated source files are compiled in the same pass.

The processor is **registered automatically** via Google Auto Service: the `@AutoService(Processor.class)` annotation on `BindLocalizationsProcessor` causes the build tool to add it to the compiler's service-loader manifest. No manual registration is required.

---

## Key Classes

| Class | Role |
|-------|------|
| `BindLocalizationsProcessor` | The annotation processor; orchestrates generation |
| `@BindLocalizations` | Marks a class to have binding code generated for it |
| `@ClassLocalizable` | Marks a class as a source of localizable strings |

---

## When This Module Runs

This module is a **compile-time-only dependency** (`<scope>provided</scope>` in Maven terminology). It has no runtime code: once compilation is complete, the generated binding classes are part of the compiled output, and this module itself is not included in the final JAR.

The parent `pom.xml` configures the `maven-compiler-plugin` to include `BindLocalizationsProcessor` as an annotation processor for all 20 modules, so every module's strings are bound automatically.
