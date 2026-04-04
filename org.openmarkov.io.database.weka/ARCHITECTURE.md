# org.openmarkov.io.database.weka — Architecture

**Role in the project:** Reads case databases from Weka ARFF format files for use in learning algorithms.
**Source files:** ~5 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## What This Module Does

**Weka** is a widely used machine-learning toolkit developed at the University of Waikato. Its native file format, **ARFF** (Attribute-Relation File Format), is a plain-text format for tabular datasets that is common in the machine-learning community. This module reads `.arff` files and produces a `CaseDatabase` for OpenMarkov's learning algorithms.

---

## ARFF File Format

An ARFF file has two sections:

**Header section** — declares the attribute (variable) names and their possible values:

```
@RELATION weather

@ATTRIBUTE outlook    {sunny, overcast, rainy}
@ATTRIBUTE temperature NUMERIC
@ATTRIBUTE humidity   NUMERIC
@ATTRIBUTE windy      {TRUE, FALSE}
@ATTRIBUTE play       {yes, no}
```

**Data section** — one observation per line, comma-separated:

```
@DATA
sunny,85,85,FALSE,no
sunny,80,90,TRUE,no
overcast,83,86,FALSE,yes
rainy,70,96,FALSE,yes
```

OpenMarkov's reader handles **nominal (categorical) attributes** — those declared with a list of values in `{}`. Numeric attributes can be discretized if the network uses finite-states variables.

---

## Key Class

| Class | Role |
|-------|------|
| `WekaCaseDatabaseReader` | Implements `CaseDatabaseReader`; reads `.arff` → `CaseDatabase` |

---

## How It Works

1. Parse the `@ATTRIBUTE` lines to build a list of variable names and their state lists.
2. Match each attribute to the corresponding `Variable` in the network (by name).
3. Parse the `@DATA` section line by line, converting each state name to its integer index.
4. Return a `CaseDatabase`.

---

## Relationship to Other Modules

| Module | Relationship |
|--------|-------------|
| [`org.openmarkov.learning.core`](../org.openmarkov.learning.core/ARCHITECTURE.md) | Provides the `CaseDatabaseReader` interface and `CaseDatabase` class |
| [`org.openmarkov.core`](../org.openmarkov.core/ARCHITECTURE.md) | Provides `Variable` and `State` |
