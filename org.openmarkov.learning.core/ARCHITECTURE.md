# org.openmarkov.learning.core — Architecture

**Role in the project:** Defines the abstract framework that all learning algorithms build upon.
**Source files:** ~16 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Statistical Background](#statistical-background)
3. [Key Classes](#key-classes)
4. [The Case Database](#the-case-database)
5. [Learning Algorithm Types](#learning-algorithm-types)
6. [The Plugin System for Algorithms](#the-plugin-system-for-algorithms)
7. [Relationship to Other Modules](#relationship-to-other-modules)
8. [Tests](#tests)

---

## What This Module Does

This module provides the **abstract skeleton** for Bayesian network learning. It defines the interfaces and base classes that concrete learning algorithms (implemented in [`org.openmarkov.learning.algorithm`](../org.openmarkov.learning.algorithm/ARCHITECTURE.md)) must follow.

Think of this module as the textbook chapter on "what all learning algorithms have in common" — the data structures for holding a dataset, the interface a learning algorithm must implement, and the hook by which the GUI can discover and configure any algorithm without knowing its specifics.

---

## Statistical Background

**Structure learning** means estimating the graph of a Bayesian network from data: which variables are parents of which others. **Parameter learning** means estimating the CPTs (conditional probability tables) given a known graph structure. OpenMarkov supports both.

The two main families of structure learning algorithms are:

- **Score-and-search**: Define a scoring function (BIC, AIC, K2, …) that measures how well a given graph fits the data. Search for the graph with the highest score. OpenMarkov's Hill Climbing algorithm uses this approach.
- **Constraint-based**: Test conditional independence relationships in the data (using statistical tests), and reconstruct the graph skeleton from those relationships. OpenMarkov's PC algorithm uses this approach.

Both families require the same input: a **case database** — a dataset where each row is one observed sample (one complete assignment of values to all variables).

---

## Key Classes

| Class | Role |
|-------|------|
| `LearningAlgorithm` | Abstract base class for all learning algorithms |
| `ScoreAndSearchAlgorithm` | Extends `LearningAlgorithm` for metric-based methods |
| `IndependenceRelationsAlgorithm` | Extends `LearningAlgorithm` for constraint-based methods |
| `CaseDatabase` | Holds a dataset in memory as a 2D array of state indices |
| `IDiscriminativeBayes` | Interface for classifier variants (Naive Bayes, TAN, …) |
| `AlgorithmManager` | Registry; maps algorithm names to classes |

---

## The Case Database

`CaseDatabase` is the in-memory representation of your dataset. It stores:

- A list of `Variable` objects (the columns of your dataset, matching the network's variables).
- A 2D integer array `cases[sample][variable]` where each entry is the **index** of the state observed for that variable in that sample. For example, if Rain has states {yes=0, no=1}, then `cases[5][2] == 0` means that in sample 5, the third variable had value "yes".
- A weight array — by default all weights are 1 (unweighted data).

Using integer indices rather than string state names makes the arithmetic in scoring metrics very fast.

---

## Learning Algorithm Types

Three abstract base classes cover the main algorithm families:

### LearningAlgorithm

The root abstract class. Every learning algorithm:

1. Receives a `ProbNet` (the initial or empty network), a `CaseDatabase`, and configuration parameters.
2. Provides a `run()` method that modifies (or replaces) the network's structure and/or parameters.
3. Can report progress to listeners (used by the GUI progress bar).

### ScoreAndSearchAlgorithm

Adds the concept of a **scoring metric** (an instance of `Metric` from [`org.openmarkov.learning.metric`](../org.openmarkov.learning.metric/ARCHITECTURE.md)). The algorithm's inner loop repeatedly evaluates candidate edits (add arc, remove arc, reverse arc) using the metric and applies the best-scoring one.

### IndependenceRelationsAlgorithm

Adds an **independence tester** — an object that, given two variables and a conditioning set, returns whether they are statistically independent in the data. The algorithm uses these tests to reconstruct the graph's skeleton and then orient edges.

### IDiscriminativeBayes

A marker interface for classifier learning algorithms (Naive Bayes, TAN, KDB, …). These algorithms learn a network intended for **classification** — predicting the value of one designated class variable — rather than modelling the full joint distribution.

---

## The Plugin System for Algorithms

Concrete algorithm classes are annotated with `@LearningAlgorithmType`:

```java
@LearningAlgorithmType(
    name        = "Hill Climbing",
    supportsNets = {BayesianNetworkType.class}
)
public class HillClimbingAlgorithm extends ScoreAndSearchAlgorithm { ... }
```

At application startup, `AlgorithmManager` scans the classpath for all classes with `@LearningAlgorithmType` and registers them. The GUI then queries `AlgorithmManager` for the list of available algorithms, populates a dropdown menu, and instantiates the selected algorithm when the user clicks "Run."

This means adding a new learning algorithm requires only:
1. Writing the class, annotating it with `@LearningAlgorithmType`.
2. Adding the module to the Maven build.

No code changes in the GUI or the core framework are needed.

---

## Relationship to Other Modules

```
org.openmarkov.learning.core
          ▲
          │  depends on
          │
org.openmarkov.learning.metric   ← scoring functions (BIC, AIC, K2, …)
          ▲
          │
org.openmarkov.learning.algorithm ← concrete algorithms (Hill Climbing, PC, …)
          ▲
          │
org.openmarkov.learning.gui      ← GUI dialogs for running learning
```

Case databases can be loaded from three file formats:
- [Excel](../org.openmarkov.io.database.excel/ARCHITECTURE.md)
- [Weka ARFF](../org.openmarkov.io.database.weka/ARCHITECTURE.md)
- [Elvira database](../org.openmarkov.io.database.elvira/ARCHITECTURE.md)

---

## Tests

About 4 test classes cover:

- Creating a `CaseDatabase` from arrays and verifying index lookups.
- Verifying that `AlgorithmManager` discovers annotated algorithm classes.
- Checking that `LearningAlgorithm` subclasses receive correct constructor arguments.

Run with:

```bash
cd org.openmarkov.learning.core
mvn test
```
