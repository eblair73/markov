# org.openmarkov.io — Architecture

**Role in the project:** Reads and writes probabilistic network files in several formats.
**Source files:** ~42 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Supported File Formats](#supported-file-formats)
3. [How the Format Registry Works](#how-the-format-registry-works)
4. [Key Classes](#key-classes)
5. [Reading a Network File](#reading-a-network-file)
6. [Writing a Network File](#writing-a-network-file)
7. [Database Readers (Case Data)](#database-readers-case-data)
8. [Tests](#tests)

---

## What This Module Does

Probabilistic networks need to be saved to disk and reloaded later. Different tools (OpenMarkov, Elvira, Netica, AMUA, Weka) use different file formats. This module provides the code that translates between OpenMarkov's internal `ProbNet` object (defined in [`org.openmarkov.core`](../org.openmarkov.core/ARCHITECTURE.md)) and files on disk.

---

## Supported File Formats

| Format | Extension | Description |
|--------|-----------|-------------|
| **ProbModel XML** | `.pgmx` | OpenMarkov's native XML format — the primary format for saving and loading networks |
| **XMLBIF** | `.xml` | XML Bayesian Interchange Format, a community standard for exchanging Bayesian networks |
| **XDSL / DSL** | `.xdsl` | GeNIe/SMILE format used by the GeNIe Modeler tool |
| **AMUA** | `.amua` | Influence diagram format used by the AMUA decision analysis tool |
| **Elvira** | `.elv` | Legacy format from the Elvira Bayesian network tool |

For **case data** (datasets used for learning), three additional formats are handled by sub-modules:
- Excel (`.xlsx`) — see [`org.openmarkov.io.database.excel`](../org.openmarkov.io.database.excel/ARCHITECTURE.md)
- Weka ARFF — see [`org.openmarkov.io.database.weka`](../org.openmarkov.io.database.weka/ARCHITECTURE.md)
- Elvira database — see [`org.openmarkov.io.database.elvira`](../org.openmarkov.io.database.elvira/ARCHITECTURE.md)

---

## How the Format Registry Works

OpenMarkov uses a **plugin registry** for file formats so that new formats can be added without modifying existing code. Here is how it works:

1. Each reader/writer class is annotated with `@ProbNetFormatType`, which declares the format name, file extension(s), and whether the class reads, writes, or both.

2. When the application starts, the `classgraph` library scans the classpath and finds all classes with `@ProbNetFormatType`.

3. The `FormatManager` singleton stores a map from file extension to reader/writer class.

4. When the user opens or saves a file, `FormatManager` looks up the right reader/writer by extension and instantiates it.

This means that adding support for a new format requires only writing a new class with the annotation — no configuration files, no code changes elsewhere.

---

## Key Classes

| Class | Role |
|-------|------|
| `ProbNetReader` | Interface: `read(String filePath) → ProbNet` |
| `ProbNetWriter` | Interface: `write(ProbNet net, String filePath)` |
| `FormatManager` | Singleton registry; maps file extensions to reader/writer instances |
| `ProbNetXMLReader` | Reads `.pgmx` and `.xml` formats |
| `ProbNetXMLWriter` | Writes `.pgmx` format |
| `XMLBIFReader` | Reads XMLBIF format |
| `AmuaReader` | Reads AMUA influence diagrams |
| `ElviraReader` | Reads Elvira `.elv` format |

---

## Reading a Network File

When you open a `.pgmx` file:

1. `FormatManager.getReader(".pgmx")` returns a `ProbNetXMLReader`.
2. `reader.read("path/to/network.pgmx")` is called.
3. `ProbNetXMLReader` uses **JDOM2** (a Java XML library) to parse the XML file into a tree of elements.
4. The reader walks the XML tree, constructing `Variable`, `Node`, `Link`, and `Potential` objects.
5. Once all objects are constructed, the reader assembles them into a `ProbNet` and returns it.
6. The GUI receives the `ProbNet` and renders it on screen.

A simplified view of the `.pgmx` XML structure:

```xml
<ProbModelXML>
  <ProbNet type="BayesianNetwork">
    <Variables>
      <Variable name="Rain" type="finiteStates">
        <States>
          <State name="yes"/>
          <State name="no"/>
        </States>
      </Variable>
      ...
    </Variables>
    <Links>
      <Link var1="Rain" var2="WetGrass" directed="true"/>
    </Links>
    <Potentials>
      <Potential type="Table">
        <Variables><Variable name="WetGrass"/><Variable name="Rain"/></Variables>
        <Values>0.99 0.01 0.4 0.6</Values>
      </Potential>
    </Potentials>
  </ProbNet>
</ProbModelXML>
```

---

## Writing a Network File

Writing is the reverse process:

1. `FormatManager.getWriter(".pgmx")` returns a `ProbNetXMLWriter`.
2. `writer.write(probNet, "path/to/output.pgmx")` is called.
3. The writer walks the `ProbNet` object tree, building an XML document using JDOM2.
4. The XML document is serialized to disk.

---

## Database Readers (Case Data)

For learning algorithms, OpenMarkov needs to read **case databases** — datasets where each row is one observation (a full assignment of values to all variables in the network). The three sub-modules implement this:

- [`org.openmarkov.io.database.excel`](../org.openmarkov.io.database.excel/ARCHITECTURE.md): reads Excel spreadsheets.
- [`org.openmarkov.io.database.weka`](../org.openmarkov.io.database.weka/ARCHITECTURE.md): reads Weka ARFF files.
- [`org.openmarkov.io.database.elvira`](../org.openmarkov.io.database.elvira/ARCHITECTURE.md): reads Elvira database files.

All three implement the `CaseDatabaseReader` interface (defined in [`org.openmarkov.learning.core`](../org.openmarkov.learning.core/ARCHITECTURE.md)) and return a `CaseDatabase` object.

---

## Tests

About 23 test classes verify that networks are read and written correctly:

- Round-trip tests: read a `.pgmx` file, write it back, compare the result.
- Cross-format tests: read an Elvira file, compare the resulting `ProbNet` to a known-good reference.
- Edge-case tests: empty networks, networks with missing data, unusual characters in variable names.

Test fixture network files are in `src/test/resources/`.

Run with:

```bash
cd org.openmarkov.io
mvn test
```
