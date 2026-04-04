# OpenMarkov — Overall Architecture

OpenMarkov is an open-source Java application for building, visualizing, and analyzing **probabilistic graphical models** — mathematical structures that represent the joint probability distribution of a set of random variables. The kinds of models supported include Bayesian networks, Markov networks, influence diagrams, dynamic Bayesian networks, MDPs, and POMDPs.

This document gives a high-level map of how the project is organized. Each module has its own architecture document linked below.

---

## Table of Contents

1. [What the Software Does](#what-the-software-does)
2. [How the Code Is Organized](#how-the-code-is-organized)
3. [Module Dependency Map](#module-dependency-map)
4. [Architecture Document Index](#architecture-document-index)
5. [Module Summaries](#module-summaries)
6. [Key Concepts and Vocabulary](#key-concepts-and-vocabulary)
7. [Building the Project](#building-the-project)
8. [Running the Application](#running-the-application)
9. [Running the Tests](#running-the-tests)
10. [Project Statistics](#project-statistics)

---

## What the Software Does

From a statistician's perspective, OpenMarkov lets you:

- **Draw and edit** a Bayesian network or other probabilistic graphical model using a graphical user interface.
- **Perform inference** — given observations (evidence), compute posterior probabilities for unobserved variables.
- **Learn network structure and parameters** from data, using algorithms such as Hill Climbing, PC, and Naive Bayes.
- **Conduct decision analysis** using influence diagrams and Markov decision processes.
- **Evaluate learned models** with cross-validation and performance metrics.
- **Generate synthetic datasets** by forward-sampling from a network.
- **Import and export networks** in several standard file formats.

---

## How the Code Is Organized

Java projects are commonly broken into **modules** — independent packages with their own source files and dependencies. OpenMarkov uses **Maven**, the standard Java build tool, to manage 20 such modules. Think of each module as a self-contained chapter in a textbook: it builds on prior chapters but can be read (and compiled) on its own.

The modules are organized into layers. Higher layers depend on lower ones; lower layers never depend on higher ones.

```
┌──────────────────────────────────────┐
│  org.openmarkov.full  (Entry point)  │
└──────────────────┬───────────────────┘
                   │
┌──────────────────▼───────────────────┐
│        org.openmarkov.gui            │  ← graphical user interface
│     + org.openmarkov.learning.gui    │
└────┬─────────┬────────┬──────────────┘
     │         │        │
Plugins:    Learning  I/O + databases
cost-eff,   (algo,    (io, excel,
bnEval,     metric,   weka, elvira)
dbGen,      core)
stochOut
     │         │        │
┌────▼─────────▼────────▼──────────────┐
│       org.openmarkov.inference       │  ← probability computation
└──────────────────┬───────────────────┘
                   │
┌──────────────────▼───────────────────┐
│        org.openmarkov.core           │  ← domain model (the foundation)
└──────────────────────────────────────┘
              ▲ built with
┌─────────────┴────────────────────────┐
│  org.openmarkov.annotationProcessing │  ← compile-time code generation
└──────────────────────────────────────┘
```

---

## Module Dependency Map

| Module | Depends on |
|--------|-----------|
| `org.openmarkov.full` | `gui`, all plugins |
| `org.openmarkov.gui` | `inference`, `io`, `learning.gui`, `core` |
| `org.openmarkov.learning.gui` | `learning.algorithm`, `learning.metric`, `learning.core` |
| `org.openmarkov.learning.algorithm` | `learning.metric`, `learning.core`, `core` |
| `org.openmarkov.learning.metric` | `learning.core`, `core` |
| `org.openmarkov.learning.core` | `core` |
| `org.openmarkov.inference` | `core` |
| `org.openmarkov.io` | `core` |
| `org.openmarkov.io.database.*` | `core` |
| `org.openmarkov.bnEvaluation` | `gui`, `learning.algorithm` |
| `org.openmarkov.costEffectiveness` | `gui` |
| `org.openmarkov.dbGenerator` | `gui` |
| `org.openmarkov.stochasticPropagationOutput` | `gui` |
| `org.openmarkov.core` | *(no project dependencies)* |
| `org.openmarkov.annotationProcessing` | *(compile-time only)* |

---

## Architecture Document Index

A single table of all 19 architecture documents in this project.

| Document | Module | Coverage |
|----------|--------|----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | *(this file)* | Overall structure, dependency map, build and run instructions |
| [org.openmarkov.core](org.openmarkov.core/ARCHITECTURE.md) | Foundation | Domain model: networks, nodes, variables, potentials, constraints, edit system |
| [org.openmarkov.annotationProcessing](org.openmarkov.annotationProcessing/ARCHITECTURE.md) | Foundation | Compile-time code generation for internationalized strings |
| [org.openmarkov.inference](org.openmarkov.inference/ARCHITECTURE.md) | Computation | Hugin propagation, variable elimination, decision analysis, temporal models |
| [org.openmarkov.io](org.openmarkov.io/ARCHITECTURE.md) | File I/O | Network file format readers and writers (PGMX, XMLBIF, XDSL, AMUA, Elvira) |
| [org.openmarkov.io.database.excel](org.openmarkov.io.database.excel/ARCHITECTURE.md) | File I/O | Import case data from Excel spreadsheets (.xlsx) |
| [org.openmarkov.io.database.weka](org.openmarkov.io.database.weka/ARCHITECTURE.md) | File I/O | Import case data in Weka ARFF format |
| [org.openmarkov.io.database.elvira](org.openmarkov.io.database.elvira/ARCHITECTURE.md) | File I/O | Import case data in legacy Elvira format |
| [org.openmarkov.learning.core](org.openmarkov.learning.core/ARCHITECTURE.md) | Learning | Abstract framework: CaseDatabase, LearningAlgorithm base classes, plugin system |
| [org.openmarkov.learning.metric](org.openmarkov.learning.metric/ARCHITECTURE.md) | Learning | Scoring metrics: BIC, AIC, K2, MDL, log-likelihood |
| [org.openmarkov.learning.algorithm](org.openmarkov.learning.algorithm/ARCHITECTURE.md) | Learning | Concrete algorithms: Hill Climbing, PC, Naive Bayes, TAN, KDB, FANB, SNB, SPNB, EM |
| [org.openmarkov.learning.gui](org.openmarkov.learning.gui/ARCHITECTURE.md) | Learning | GUI dialogs for configuring and running learning algorithms |
| [org.openmarkov.gui](org.openmarkov.gui/ARCHITECTURE.md) | User Interface | Main Swing GUI: windows, menus, network editor, dialogs, theme system |
| [org.openmarkov.full](org.openmarkov.full/ARCHITECTURE.md) | User Interface | Application entry point; assembles the executable JAR |
| [org.openmarkov.bnEvaluation](org.openmarkov.bnEvaluation/ARCHITECTURE.md) | Analysis Plugin | Cross-validation, confusion matrix, accuracy/precision/recall/F1 |
| [org.openmarkov.costEffectiveness](org.openmarkov.costEffectiveness/ARCHITECTURE.md) | Analysis Plugin | Cost-effectiveness plane, ICER, tornado diagram, sensitivity analysis |
| [org.openmarkov.dbGenerator](org.openmarkov.dbGenerator/ARCHITECTURE.md) | Analysis Plugin | Ancestral sampling to generate synthetic datasets |
| [org.openmarkov.stochasticPropagationOutput](org.openmarkov.stochasticPropagationOutput/ARCHITECTURE.md) | Analysis Plugin | Parametric bootstrap for CPT uncertainty propagation |
| [org.openmarkov.integrationTests](org.openmarkov.integrationTests/ARCHITECTURE.md) | Testing | End-to-end tests spanning I/O, inference, learning, and the GUI |

---

## Module Summaries

### Foundation

| Module | Purpose | Architecture Doc |
|--------|---------|-----------------|
| [org.openmarkov.core](org.openmarkov.core/ARCHITECTURE.md) | Domain model: networks, nodes, variables, potentials, constraints, edit system | [→](org.openmarkov.core/ARCHITECTURE.md) |
| [org.openmarkov.annotationProcessing](org.openmarkov.annotationProcessing/ARCHITECTURE.md) | Compile-time code generation for internationalized strings | [→](org.openmarkov.annotationProcessing/ARCHITECTURE.md) |

### Computation

| Module | Purpose | Architecture Doc |
|--------|---------|-----------------|
| [org.openmarkov.inference](org.openmarkov.inference/ARCHITECTURE.md) | Inference algorithms: Hugin propagation, variable elimination, decision analysis | [→](org.openmarkov.inference/ARCHITECTURE.md) |

### File I/O

| Module | Purpose | Architecture Doc |
|--------|---------|-----------------|
| [org.openmarkov.io](org.openmarkov.io/ARCHITECTURE.md) | Network file format readers and writers (XML, PGMX, XDSL, AMUA) | [→](org.openmarkov.io/ARCHITECTURE.md) |
| [org.openmarkov.io.database.excel](org.openmarkov.io.database.excel/ARCHITECTURE.md) | Import case data from Excel spreadsheets (.xlsx) | [→](org.openmarkov.io.database.excel/ARCHITECTURE.md) |
| [org.openmarkov.io.database.weka](org.openmarkov.io.database.weka/ARCHITECTURE.md) | Import case data in Weka ARFF format | [→](org.openmarkov.io.database.weka/ARCHITECTURE.md) |
| [org.openmarkov.io.database.elvira](org.openmarkov.io.database.elvira/ARCHITECTURE.md) | Import case data in legacy Elvira format | [→](org.openmarkov.io.database.elvira/ARCHITECTURE.md) |

### Learning

| Module | Purpose | Architecture Doc |
|--------|---------|-----------------|
| [org.openmarkov.learning.core](org.openmarkov.learning.core/ARCHITECTURE.md) | Abstract base classes for learning algorithms and case databases | [→](org.openmarkov.learning.core/ARCHITECTURE.md) |
| [org.openmarkov.learning.metric](org.openmarkov.learning.metric/ARCHITECTURE.md) | Scoring metrics: BIC, AIC, K2, MDL, log-likelihood | [→](org.openmarkov.learning.metric/ARCHITECTURE.md) |
| [org.openmarkov.learning.algorithm](org.openmarkov.learning.algorithm/ARCHITECTURE.md) | Concrete learning algorithms: Hill Climbing, PC, Naive Bayes, TAN, EM | [→](org.openmarkov.learning.algorithm/ARCHITECTURE.md) |
| [org.openmarkov.learning.gui](org.openmarkov.learning.gui/ARCHITECTURE.md) | GUI dialogs for configuring and running learning | [→](org.openmarkov.learning.gui/ARCHITECTURE.md) |

### User Interface

| Module | Purpose | Architecture Doc |
|--------|---------|-----------------|
| [org.openmarkov.gui](org.openmarkov.gui/ARCHITECTURE.md) | Main Swing GUI: windows, menus, network editor, dialogs | [→](org.openmarkov.gui/ARCHITECTURE.md) |
| [org.openmarkov.full](org.openmarkov.full/ARCHITECTURE.md) | Application entry point; assembles the executable JAR | [→](org.openmarkov.full/ARCHITECTURE.md) |

### Analysis Plugins

| Module | Purpose | Architecture Doc |
|--------|---------|-----------------|
| [org.openmarkov.bnEvaluation](org.openmarkov.bnEvaluation/ARCHITECTURE.md) | Cross-validation and performance metrics for learned networks | [→](org.openmarkov.bnEvaluation/ARCHITECTURE.md) |
| [org.openmarkov.costEffectiveness](org.openmarkov.costEffectiveness/ARCHITECTURE.md) | Cost-effectiveness analysis and tornado diagrams | [→](org.openmarkov.costEffectiveness/ARCHITECTURE.md) |
| [org.openmarkov.dbGenerator](org.openmarkov.dbGenerator/ARCHITECTURE.md) | Generate synthetic datasets by sampling from a network | [→](org.openmarkov.dbGenerator/ARCHITECTURE.md) |
| [org.openmarkov.stochasticPropagationOutput](org.openmarkov.stochasticPropagationOutput/ARCHITECTURE.md) | Visualization of uncertainty in inference results | [→](org.openmarkov.stochasticPropagationOutput/ARCHITECTURE.md) |

### Testing

| Module | Purpose | Architecture Doc |
|--------|---------|-----------------|
| [org.openmarkov.integrationTests](org.openmarkov.integrationTests/ARCHITECTURE.md) | End-to-end tests spanning I/O, inference, learning, and the GUI | [→](org.openmarkov.integrationTests/ARCHITECTURE.md) |

---

## Key Concepts and Vocabulary

| Term | Meaning |
|------|---------|
| **ProbNet** | The central Java object representing a probabilistic network (graph + probability tables) |
| **Node** | A vertex in the graph, corresponding to a random variable (or a decision/utility variable) |
| **Variable** | A random variable with a finite set of states (for discrete models) or a continuous range |
| **Potential** | A function over a set of variables — corresponds to a conditional probability table (CPT) or a utility function |
| **TablePotential** | The most common potential type: a full conditional probability table stored as an array |
| **Finding / EvidenceCase** | An observation: a variable assigned a specific value (evidence for inference) |
| **InferenceAlgorithm** | Code that computes posterior probabilities given a network and evidence |
| **Learning algorithm** | Code that estimates network structure and/or parameters from a dataset |
| **Metric (scoring function)** | A number that scores how well a given network structure fits the data (BIC, AIC, K2, …) |
| **Module** | A self-contained Maven sub-project with its own source files and declared dependencies |
| **Maven** | The Java build tool used to compile, test, and package all modules |
| **JAR** | A Java ARchive file — the compiled, packaged form of a Java module or application |
| **Plugin** | A module that extends the application without modifying its core code |

---

## Building the Project

### Prerequisites

You need these tools installed before building. Each link goes to the official installer.

| Tool | Minimum Version | Purpose |
|------|----------------|---------|
| [Java Development Kit (JDK)](https://adoptium.net/) | 21 | Compiles and runs Java code |
| [Apache Maven](https://maven.apache.org/download.cgi) | 3.8 | Builds all modules and manages dependencies |
| Git | any | Clones the repository |

**Verify your installation** — open a terminal (on Mac: Terminal app; on Windows: Command Prompt) and run:

```bash
java -version      # should say "21" or higher
mvn -version       # should say "3.8" or higher
```

### Step-by-step build

1. **Clone (or download) the repository.**

   If you received the source as a ZIP file, unzip it. If using Git:

   ```bash
   git clone <repository-url>
   cd markov
   ```

2. **Navigate to the parent module.** The parent module `org.openmarkov` is the top-level coordinator.

   ```bash
   cd org.openmarkov
   ```

3. **Build everything.** This command compiles all 20 modules, runs all tests, and creates JAR files.

   ```bash
   mvn clean install
   ```

   What each word means:
   - `mvn` — invokes Maven
   - `clean` — deletes any previous build output
   - `install` — compiles, tests, and stores each module's JAR in your local Maven cache

   The first run will download dependencies (~150 MB) from the internet. Subsequent runs are faster.

4. **Locate the runnable JAR.** After a successful build, the executable application is at:

   ```
   ../org.openmarkov.full/target/openmarkov-full-0.3.0-SNAPSHOT-jar-with-dependencies.jar
   ```

### Skipping tests (faster build)

If you want to compile quickly without running the test suite:

```bash
mvn clean install -DskipTests
```

### Generating Javadoc

```bash
mvn javadoc:aggregate
```

The HTML documentation is written to `target/site/apidocs/index.html`.

---

## Running the Application

### Graphical User Interface

From the repository root, run:

```bash
java -jar org.openmarkov.full/target/openmarkov-full-0.3.0-SNAPSHOT-jar-with-dependencies.jar
```

A splash screen appears, followed by the main OpenMarkov window.

**Opening a network file at startup:**

```bash
java -jar org.openmarkov.full/target/openmarkov-full-0.3.0-SNAPSHOT-jar-with-dependencies.jar mynetwork.pgmx
```

**Changing the interface language** (English or Spanish):

```bash
java -jar org.openmarkov.full/target/...jar -l es    # Spanish
java -jar org.openmarkov.full/target/...jar -l en    # English (default)
```

### Using OpenMarkov as a Java Library

If you want to work with networks programmatically (without the GUI), add the `core` and `inference` modules as dependencies in your own Maven project and write Java code such as:

```java
// Build a two-node Bayesian network: Rain → WetGrass
ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());

Variable rain     = new Variable("Rain",     new State[]{ new State("yes"), new State("no") });
Variable wetGrass = new Variable("WetGrass", new State[]{ new State("yes"), new State("no") });

net.addNode(rain,     NodeType.CHANCE, new Point2D(0, 0));
net.addNode(wetGrass, NodeType.CHANCE, new Point2D(100, 0));
net.addLink(rain, wetGrass, true);

// Run inference given evidence Rain=yes
EvidenceCase evidence = new EvidenceCase();
evidence.addFinding(new Finding(rain, 0));   // 0 = index of "yes"

InferenceAlgorithm algo = InferenceManager.getDefaultAlgorithm(net);
Propagation result = (Propagation) algo.query(new PropagationTask(), evidence);
```

---

## Running the Tests

From inside `org.openmarkov/`:

```bash
mvn test
```

To run tests for a single module only (for example, the inference module):

```bash
cd ../org.openmarkov.inference
mvn test
```

Test reports are written to `target/surefire-reports/` inside each module directory.

---

## Project Statistics

| Item | Count |
|------|-------|
| Maven modules | 20 |
| Java source files | ~1,000 |
| Test classes | ~200 |
| Potential (CPT) types | 30+ |
| Constraint types | 35+ |
| Network types supported | 12+ |
| File formats supported | 4 |
| Learning algorithms | 9 |
| Interface languages | 2 (English, Spanish) |
