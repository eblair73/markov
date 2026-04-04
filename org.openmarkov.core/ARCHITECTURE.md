# org.openmarkov.core — Architecture

**Role in the project:** The foundation. Every other module depends on this one.
**Source files:** ~386 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Package Structure](#package-structure)
3. [The Domain Model: Networks, Nodes, and Variables](#the-domain-model)
4. [Potentials: Conditional Probability Tables and More](#potentials)
5. [Constraints: Rules That Networks Must Obey](#constraints)
6. [Network Types](#network-types)
7. [The Edit System: Undo and Redo](#the-edit-system)
8. [Inference Framework](#inference-framework)
9. [File I/O Interfaces](#file-io-interfaces)
10. [Localization](#localization)
11. [Exception Hierarchy](#exception-hierarchy)
12. [Key External Libraries](#key-external-libraries)
13. [Tests](#tests)

---

## What This Module Does

`org.openmarkov.core` defines all the fundamental data structures that the rest of the application uses to represent, store, and manipulate probabilistic networks. Think of it as the vocabulary and grammar of the entire system: other modules speak in terms of objects defined here.

Concretely, this module provides:

- Java classes that represent networks (graphs with probability distributions), nodes (random variables), and their conditional probability tables.
- A constraint system that enforces the structural rules of each network type (e.g., "no cycles in a Bayesian network").
- An edit system that records every change to a network, enabling unlimited undo and redo.
- Abstract interfaces for file I/O and inference, which concrete implementations in other modules fulfill.
- Internationalization support so that the GUI can display messages in multiple languages.

---

## Package Structure

The source code lives under `src/main/java/org/openmarkov/core/` and is divided into these sub-packages:

| Package | Contents |
|---------|----------|
| `model.network` | Core domain objects: `ProbNet`, `Node`, `Variable`, `Link`, `Graph` |
| `model.network.potential` | 30+ potential implementations (CPTs, Gaussian, utility functions, …) |
| `model.network.constraint` | 35+ structural constraints |
| `model.network.type` | Network type registry (Bayesian, Markov, Influence Diagram, …) |
| `action.base` | Edit system: `PNEdit`, `CompoundPNEdit`, `PNESupport` |
| `inference` | Inference task definitions and algorithm interface |
| `io` | Reader/writer interfaces and format registration |
| `expression` | Arithmetic expression parser (built on ANTLR4) |
| `localize` | Internationalization support |
| `exception` | ~30 exception classes |

---

## The Domain Model

### ProbNet — the network object

`ProbNet` is the central class. It represents one probabilistic network as a **directed or undirected graph** whose nodes carry probability distributions. Think of it as a container that holds:

- A list of `Node` objects (the vertices of the graph).
- A list of `Link` objects (the edges).
- Metadata: network type, title, author, comments.
- A `NetworkType` object that determines which constraints apply.

```
ProbNet
  ├── nodes:  List<Node>
  ├── links:  List<Link>
  ├── networkType: NetworkType
  └── potentials: List<Potential>
```

### Node — a random variable in the graph

Each `Node` wraps a `Variable` and additionally stores:

- **NodeType**: `CHANCE` (random variable), `DECISION` (choice), or `UTILITY` (objective function).
- A position on the screen (for GUI rendering).
- A list of `Potential` objects (its probability table or utility function).

### Variable — the random variable itself

A `Variable` has:
- A name (e.g., "Rain").
- For **discrete variables**: an ordered list of `State` objects (e.g., "yes", "no").
- For **continuous variables**: a numeric range.
- A `VariableType` enum value: `FINITE_STATES`, `NUMERIC`, or `DISCRETIZED`.

### Link — a directed or undirected edge

A `Link` connects two `Node` objects. It may be directed (arrow from parent to child, as in a Bayesian network) or undirected (as in a Markov network).

### Finding and EvidenceCase — observations

When you observe that variable *Rain* has value *yes*, that is represented as a `Finding`. An `EvidenceCase` is a collection of `Finding` objects — it is the evidence you supply to an inference algorithm.

---

## Potentials

A **potential** is a function that maps a combination of variable states to a number. In Bayesian networks, the most common potential is the **conditional probability table (CPT)**: for each combination of parent states, it stores the probability of each state of the child variable.

OpenMarkov provides more than 30 potential types. The most important ones:

| Class | What it represents |
|-------|--------------------|
| `TablePotential` | A full, explicit CPT stored as a flat array |
| `UniformPotential` | A uniform distribution (all states equally likely) |
| `DeltaPotential` | A deterministic function (one state has probability 1) |
| `ConditionalGaussianPotential` | Gaussian distribution conditioned on discrete parents |
| `ICIPotential` | Independence of Causal Influence (noisy-OR, noisy-MAX) |
| `LinearCombinationPotential` | Linear function of parent values (for continuous variables) |
| `SumPotential` / `ProductPotential` / `MaxPotential` | Aggregations for utility nodes in influence diagrams |
| `StrategyTree` | Optimal decision strategy (output of decision analysis) |
| `BinomialPotential` | Binomial distribution parameterized by a parent variable |
| `GLMPotential` | Generalized linear model potential |

`TablePotential` is the workhorse for discrete Bayesian networks. It stores values in a one-dimensional array using a column-major layout over variable states. The order of variables in the array is fixed when the potential is constructed, and index arithmetic handles multi-dimensional lookups efficiently.

### PotentialOperations

A utility class `PotentialOperations` provides algorithms over potentials — marginalization, multiplication, restriction (conditioning on evidence), and combination — all without modifying the original potential objects.

---

## Constraints

Constraints enforce the structural rules of a given network type. For example:

- A **Bayesian network** must be a directed acyclic graph (DAG), so the constraint `NoCycle` is active.
- An **influence diagram** may have utility nodes, but `NoUtilityParent` prevents utility nodes from being parents of other nodes.

Constraints are implemented as subclasses of `PNConstraint`. Each constraint provides a `checkProbNet()` method that returns `true` if the network satisfies the constraint.

The `ConstraintChecker` class is called before every edit (add node, add link, etc.) and rejects edits that would violate active constraints.

Selected constraint classes:

| Class | Rule |
|-------|------|
| `NoCycle` | No directed cycles |
| `NoSelfLoop` | No node can link to itself |
| `NoMultipleLinks` | At most one link between any pair of nodes |
| `OnlyChanceNodes` | Only random (chance) nodes allowed |
| `OnlyDirectedLinks` | All links must be directed |
| `MaxNumParents` | A node can have at most *k* parents |
| `DistinctVariableNames` | All variable names must be unique |
| `NoUtilityParent` | Utility nodes cannot be parents |

---

## Network Types

Each network type is a singleton object that specifies which constraints apply and what the network semantics are. Types are registered via the `@NetworkType` annotation and discovered at startup by classgraph.

| Class | Network type |
|-------|--------------|
| `BayesianNetworkType` | Classic Bayesian network (DAG, CPTs) |
| `MarkovNetworkType` | Markov random field (undirected) |
| `InfluenceDiagramType` | Influence diagram (decisions + utility) |
| `LIMIDType` | Limited-memory influence diagram |
| `DynamicBayesianNetworkType` | Two-slice temporal Bayesian network |
| `MDPType` | Markov decision process |
| `POMDPType` | Partially observable MDP |
| `MIDType` | Multi-agent influence diagram |
| `TuningNetworkType` | For parameter tuning workflows |

---

## The Edit System

Every change to a `ProbNet` — adding a node, removing a link, editing a CPT — is represented as an **edit object** (a subclass of `PNEdit`). This is the [Command pattern](https://en.wikipedia.org/wiki/Command_pattern): an action is reified as a data object.

Benefits:
- **Undo / redo**: each edit knows how to reverse itself.
- **Validation**: before executing an edit, `ConstraintChecker` verifies it will not violate any constraint.
- **Listeners**: `PNESupport` notifies the GUI whenever the network changes.

Key classes:

| Class | Role |
|-------|------|
| `PNEdit` | Abstract base class for all edits |
| `CompoundPNEdit` | A group of edits that execute and undo atomically |
| `PNESupport` | Maintains edit history; notifies registered listeners |
| `ConstraintChecker` | Validates an edit against all active constraints |

Examples of concrete edits: `AddNodeEdit`, `RemoveNodeEdit`, `AddLinkEdit`, `RemoveLinkEdit`, `SetPotentialEdit`, `RenameVariableEdit`.

---

## Inference Framework

The `core` module defines the **contract** for inference — the abstract classes and task types — without providing any concrete algorithms (those live in [`org.openmarkov.inference`](../org.openmarkov.inference/ARCHITECTURE.md)).

| Class | Role |
|-------|------|
| `InferenceAlgorithm` | Abstract base; subclasses implement `query()` |
| `InferenceOptions` | Configuration knobs (elimination heuristic, temporal slices, …) |
| `Propagation` | Task type: compute posterior distributions |
| `Evaluation` | Task type: compute expected utilities |
| `OptimalPolicies` | Task type: find best decision strategy |
| `CEAnalysis` | Task type: cost-effectiveness analysis |
| `SensitivityAnalysis*` | Several sensitivity analysis task types |

---

## File I/O Interfaces

The `core.io` package defines reader and writer interfaces. Each concrete file format is implemented in a separate module (primarily [`org.openmarkov.io`](../org.openmarkov.io/ARCHITECTURE.md)).

- `ProbNetReader` — reads a file and returns a `ProbNet`.
- `ProbNetWriter` — serializes a `ProbNet` to a file.
- `@ProbNetFormatType` annotation — registers a reader/writer with the format registry.

The registry is populated automatically at startup via classgraph reflection, so adding a new file format requires only implementing the interface and adding the annotation.

---

## Localization

OpenMarkov supports English and Spanish. All user-facing strings are stored in XML resource files:

- `src/main/resources/core/localize/core_en.xml`
- `src/main/resources/core/localize/core_es.xml`

At runtime, `StringDatabase.getUniqueInstance().getString("key")` looks up the current language's string. At compile time, the [`annotationProcessing`](../org.openmarkov.annotationProcessing/ARCHITECTURE.md) module generates type-safe binding code so that string keys can be refactored safely.

---

## Exception Hierarchy

The project defines ~30 exception classes organized in two branches:

- `OpenMarkovException` (checked) — recoverable errors the user can fix (bad input file, invalid network state).
  - `UserInputException` — the user entered invalid data.
  - `ParserException` — could not parse a file.
  - `ConstraintViolatedException` — a proposed edit would break a constraint.
- `OpenMarkovRuntimeException` (unchecked) — programming errors that should never reach a user.

Using a rich hierarchy lets catch-blocks be precise about what went wrong, and lets the GUI show helpful messages.

---

## Key External Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| [JGraphT](https://jgrapht.org/) | 1.5.2 | Graph data structures and algorithms |
| [JDOM2](http://www.jdom.org/) | 2.0.6.1 | XML parsing and serialization |
| [Apache Commons Math](https://commons.apache.org/proper/commons-math/) | 3.6.1 | Numerical algorithms, statistics |
| [Colt](https://dst.lbl.gov/ACSSoftware/colt/) | 1.2.0 | High-performance linear algebra |
| [ANTLR4](https://www.antlr.org/) | runtime | Parses arithmetic expressions |
| [ClassGraph](https://github.com/classgraph/classgraph) | 4.8.179 | Plugin/annotation discovery at startup |
| [Log4j 2](https://logging.apache.org/log4j/2.x/) | 2.25.3 | Logging |

---

## Tests

Tests live in `src/test/java/`. There are ~95 test classes covering:

- **Model tests**: creating networks, adding/removing nodes and links, editing CPTs.
- **Edit system tests**: undo/redo, constraint checking.
- **Potential tests**: arithmetic on CPTs, marginalization, restriction.
- **I/O tests**: round-trip serialization of networks.
- **Inference framework tests**: task construction, option parsing.

Test fixture networks (pre-built `.pgmx` files) are in `src/test/resources/nets/`.

Run with:

```bash
cd org.openmarkov.core
mvn test
```
