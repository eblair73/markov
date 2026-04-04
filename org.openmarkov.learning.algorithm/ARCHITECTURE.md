# org.openmarkov.learning.algorithm — Architecture

**Role in the project:** Concrete implementations of Bayesian network structure and parameter learning algorithms.
**Source files:** ~32 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Algorithm Overview](#algorithm-overview)
3. [Score-and-Search Algorithms](#score-and-search-algorithms)
4. [Constraint-Based Algorithms](#constraint-based-algorithms)
5. [Classifier Algorithms](#classifier-algorithms)
6. [Parameter Learning](#parameter-learning)
7. [Independence Testing](#independence-testing)
8. [Tests](#tests)

---

## What This Module Does

This module contains the actual learning algorithms: the code that takes a dataset and produces a Bayesian network (or a classifier). It builds on:

- [`org.openmarkov.learning.core`](../org.openmarkov.learning.core/ARCHITECTURE.md) — abstract framework and `CaseDatabase`.
- [`org.openmarkov.learning.metric`](../org.openmarkov.learning.metric/ARCHITECTURE.md) — scoring functions (BIC, AIC, K2, …).
- [`org.openmarkov.core`](../org.openmarkov.core/ARCHITECTURE.md) — network data structures and edit system.

---

## Algorithm Overview

| Algorithm | Class | Family | What it learns |
|-----------|-------|--------|----------------|
| Hill Climbing | `HillClimbingAlgorithm` | Score-and-search | General BN structure |
| PC | `PCAlgorithm` | Constraint-based | General BN structure |
| Naive Bayes | `NaiveBayesAlgorithm` | Classifier | Fixed-structure BN classifier |
| TAN | `TreeAugmentedNBAlgorithm` | Classifier | Tree-augmented classifier |
| KDB | `KDBAlgorithm` | Classifier | k-Dependence classifier |
| FANB | `ForestAugmentedNBAlgorithm` | Classifier | Forest-augmented classifier |
| SNB | `SelectiveNBAlgorithm` | Classifier | Selective Naive Bayes |
| SPNB | `SuperParentNBAlgorithm` | Classifier | Super-parent Naive Bayes |
| EM | `EMAlgorithm` | Parameter learning | CPTs with latent variables |

---

## Score-and-Search Algorithms

### Hill Climbing (`HillClimbingAlgorithm`)

**Statistical idea:** Hill Climbing is a **greedy local search** over the space of DAG structures, guided by a scoring metric. Starting from an empty graph (or a user-provided initial graph), at each step it considers every possible single-arc addition, deletion, or reversal, scores each candidate with the chosen metric (BIC by default), and applies the change with the highest positive score improvement. It stops when no single change improves the score — a **local optimum**.

**Algorithm steps:**

1. Initialize: set current network to the empty DAG (or user-provided structure).
2. Compute the initial score.
3. **Inner loop** (one iteration):
   a. For each pair of nodes (A, B): evaluate adding arc A→B (if not present and no cycle created), removing arc A→B (if present), reversing arc A→B (if present and no cycle created).
   b. Find the edit with the highest score delta.
   c. If max delta > 0, apply that edit. Else, stop.
4. After structure search, estimate CPTs from data by maximum likelihood.

**Limitation:** Hill Climbing finds a local optimum, not necessarily the global one. It can miss the true structure, especially with small datasets or many variables. In practice it works well and is fast.

**Configurable parameters:** scoring metric (default BIC), maximum number of parents per node, initial network structure.

---

## Constraint-Based Algorithms

### PC Algorithm (`PCAlgorithm`)

**Statistical idea:** The PC algorithm (named after its inventors Peter Spirtes and Clark Glymour) reconstructs the Bayesian network structure by performing **conditional independence tests** on the data. It does not optimize a score; instead it asks: "Are variables X and Y independent, given this set of conditioning variables Z?" and uses those answers to determine which arcs are present.

**Algorithm steps:**

1. **Skeleton phase:** Start with a complete undirected graph.
   - For each pair (X, Y), test whether X ⊥ Y | ∅ (unconditional independence). If yes, remove edge X–Y.
   - Increase the size of the conditioning set to 1, 2, … and repeat for all pairs still connected.
   - Record the **separating set** for each removed edge (the conditioning set that made the pair independent).

2. **Orientation phase (v-structures):** For each triple A–B–C where A–C is absent and B is not in the separating set of A and C, orient as A→B←C (a "collider" or "v-structure").

3. **Orientation propagation:** Apply the Meek rules to orient additional edges while avoiding new cycles and v-structures.

**Independence test used:** `CrossEntropyIndependenceTester` implements a **chi-squared test** of conditional independence on the data, with a user-configurable significance level (default α = 0.05).

**Limitation:** PC is correct under the faithfulness assumption (the true distribution is faithful to the true graph) and with a sufficient sample size. With small samples, many independence tests fail, and the algorithm may miss true edges or add false ones.

---

## Classifier Algorithms

All classifier algorithms fix the **class variable** (the one to be predicted) as the root or a distinguished node and build a network optimized for classification rather than modelling the full joint distribution. They all implement `IDiscriminativeBayes`.

### Naive Bayes (`NaiveBayesAlgorithm`)

The simplest classifier: assumes all feature variables are **conditionally independent given the class**. The network has arcs only from the class node to each feature node. Despite this strong (and often violated) assumption, Naive Bayes works surprisingly well in practice and is very fast to train.

### TAN — Tree-Augmented Naive Bayes (`TreeAugmentedNBAlgorithm`)

Extends Naive Bayes by adding one additional parent to each feature node (beyond the class), forming a **tree structure** over the features. The tree is chosen to maximize the conditional mutual information between feature pairs given the class. This relaxes the independence assumption and often improves accuracy.

Training procedure:
1. Compute pairwise conditional mutual information I(Xi; Xj | C) for all feature pairs.
2. Find the maximum spanning tree of the complete graph with these mutual information values as edge weights (Chow-Liu algorithm).
3. Root the tree at any node; orient arcs away from root.
4. Add arcs from the class node to all features.
5. Estimate CPTs.

### KDB — k-Dependence Bayesian classifier (`KDBAlgorithm`)

Generalizes TAN by allowing each feature to have up to *k* additional parents (beyond the class), selected by mutual information ranking. When k=1, KDB reduces to TAN.

### FANB — Forest-Augmented Naive Bayes (`ForestAugmentedNBAlgorithm`)

Like TAN but builds a **forest** (a collection of trees) rather than a single tree, which allows the algorithm to omit weak dependency arcs.

### SNB — Selective Naive Bayes (`SelectiveNBAlgorithm`)

Performs **feature selection**: removes features whose inclusion decreases classification performance (as measured by cross-validation or a scoring criterion). The result is a Naive Bayes classifier that uses only a subset of the available features.

### SPNB — Super-Parent Naive Bayes (`SuperParentNBAlgorithm`)

Adds one feature as a "super-parent" that is a parent of all other features (in addition to the class). The super-parent is chosen to maximize a classification criterion.

---

## Parameter Learning

### EM Algorithm (`EMAlgorithm`)

**Statistical idea:** **Expectation-Maximization** learns CPTs when some variables are **latent** (never observed in the data). It alternates between:

- **E step:** Given the current CPT estimates, compute the expected sufficient statistics (pseudo-counts) by running inference on each case in the dataset with the latent variables unobserved.
- **M step:** Re-estimate the CPTs from the expected pseudo-counts by maximum likelihood.

The algorithm is guaranteed to converge to a local maximum of the likelihood function.

> **Note:** As of this version, the EM implementation contains a placeholder `while(false)` loop — the algorithm is scaffolded but not fully activated. Check the source file `EMAlgorithm.java` for the current status.

---

## Independence Testing

`CrossEntropyIndependenceTester` implements the conditional independence test used by the PC algorithm:

- **Test:** Chi-squared test of the null hypothesis X ⊥ Y | Z.
- **Test statistic:** 2 × N × cross-entropy between the empirical joint distribution of (X, Y, Z) and the product of conditional distributions.
- **Degrees of freedom:** (|states(X)| − 1)(|states(Y)| − 1) × |configurations(Z)|.
- **Decision:** Reject independence if the p-value < α.

A `MockIndependenceTester` is provided for unit testing (hard-coded independence answers).

---

## Tests

About 9 test classes cover:

- Hill Climbing add/remove/reverse arc operations.
- Sensitivity analysis for structure perturbation.
- PC algorithm on simple networks with known structure.
- Classifier algorithm structure verification (correct arc sets for Naive Bayes, TAN).

Run with:

```bash
cd org.openmarkov.learning.algorithm
mvn test
```
