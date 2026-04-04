# org.openmarkov.full — Architecture

**Role in the project:** Entry point and packaging — assembles the runnable application JAR.
**Source files:** ~7 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [The Entry Point Class](#the-entry-point-class)
3. [Command-Line Arguments](#command-line-arguments)
4. [Application Startup Sequence](#application-startup-sequence)
5. [How the Executable JAR Is Built](#how-the-executable-jar-is-built)
6. [Running the Application](#running-the-application)

---

## What This Module Does

This is the **top-level module** that ties everything together. Its two responsibilities are:

1. **Entry point** — provides the `main()` method that Java's runtime calls when you launch the application.
2. **Packaging** — uses the Maven Assembly Plugin to bundle all 20 modules and their third-party dependencies into a single, self-contained JAR file that any computer with Java can run.

This module has no logic of its own beyond startup initialization; it delegates everything to [`org.openmarkov.gui`](../org.openmarkov.gui/ARCHITECTURE.md).

---

## The Entry Point Class

`org.openmarkov.full.OpenMarkov` contains the `public static void main(String[] args)` method. In Java, this is the method that the virtual machine calls when you run `java -jar openmarkov.jar`.

Startup steps in `main()`:

1. Parse command-line arguments (language, files to open).
2. Load application preferences from disk (theme, language, UI scale).
3. Initialize the **FlatLaf** look-and-feel (applies the modern visual theme to all Swing components).
4. Install the global exception handler (`OMExceptionHandler`).
5. Initialize `StringDatabase` with the chosen language.
6. Show a **splash screen** while the rest of the application loads.
7. Create and display the `MainGUI` window.
8. If any network file paths were passed on the command line, open them.

---

## Command-Line Arguments

| Argument | Example | Effect |
|----------|---------|--------|
| `-l <language>` | `-l es` | Set interface language (`en` = English, `es` = Spanish). Default: `en`. |
| `<file1> [file2 …]` | `mynetwork.pgmx` | Open one or more network files at startup. |

Examples:

```bash
# Start normally
java -jar openmarkov.jar

# Start in Spanish
java -jar openmarkov.jar -l es

# Start and immediately open two networks
java -jar openmarkov.jar network1.pgmx network2.pgmx

# Start in Spanish and open a network
java -jar openmarkov.jar -l es patient_risk.pgmx
```

---

## Application Startup Sequence

```
java -jar openmarkov.jar [-l lang] [file ...]
         │
         ▼
OpenMarkov.main()
  1. Parse args
  2. Load preferences (language, theme, scale)
  3. Initialize FlatLaf theme
  4. Install global exception handler
  5. Initialize StringDatabase (localization)
  6. Show splash screen
         │
         ▼
  7. Create MainGUI (the main window)         ← in org.openmarkov.gui
         │
         ▼
  8. Open command-line file arguments
         │
         ▼
  9. Hide splash; show main window
         │
         ▼
  [User interacts with the application]
```

---

## How the Executable JAR Is Built

A standard Java JAR file contains only one module's compiled classes. To distribute the application as a single file, the **maven-assembly-plugin** is configured in this module's `pom.xml` to produce a **fat JAR** (also called an uber-JAR or jar-with-dependencies):

1. Maven compiles all 20 modules.
2. The assembly plugin collects all compiled class files from all modules.
3. It also unpacks all third-party dependency JARs and includes their class files.
4. Everything is packed into one large JAR file.
5. The JAR's `MANIFEST.MF` file declares `Main-Class: org.openmarkov.full.OpenMarkov`, so Java knows which `main()` to call.

The output file is:

```
org.openmarkov.full/target/openmarkov-full-0.3.0-SNAPSHOT-jar-with-dependencies.jar
```

This file contains the compiled code for all 20 modules plus all third-party libraries — approximately 50–100 MB. Anyone with Java 21 installed can run it without installing any additional tools.

---

## Running the Application

**Full command (from the repository root):**

```bash
java -jar org.openmarkov.full/target/openmarkov-full-0.3.0-SNAPSHOT-jar-with-dependencies.jar
```

**Tip:** Rename the JAR for convenience:

```bash
cp org.openmarkov.full/target/openmarkov-full-0.3.0-SNAPSHOT-jar-with-dependencies.jar openmarkov.jar
java -jar openmarkov.jar
```

**On high-DPI (Retina/4K) displays**, you may want to set the UI scale:

```bash
java -Dflatlaf.uiScale=2.0 -jar openmarkov.jar
```

**On macOS**, you can double-click a JAR file if you have Java installed and `.jar` files are associated with the Java launcher.

For full build instructions, see the [Overall Architecture](../ARCHITECTURE.md#building-the-project).
