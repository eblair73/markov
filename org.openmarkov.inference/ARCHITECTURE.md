# org.openmarkov.inference — Architecture

**Role in the project:** Implements the algorithms that compute probability distributions given evidence.
**Source files:** ~69 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Statistical Background](#statistical-background)
3. [Package Structure](#package-structure)
4. [Inference Algorithms](#inference-algorithms)
5. [Variable Elimination Order Heuristics](#variable-elimination-order-heuristics)
6. [Decision Analysis](#decision-analysis)
7. [Temporal Models](#temporal-models)
8. [How an Inference Call Works](#how-an-inference-call-works)
9. [Tests](#tests)

---

## What This Module Does

Given a probabilistic network and a set of observations (evidence), inference computes the **posterior probability distribution** of the unobserved variables. This module provides the concrete implementations of those computations.

It also handles **decision analysis** (finding optimal strategies in influence diagrams and MDPs) and **temporal propagation** (rolling inference forward through time slices in dynamic Bayesian networks).

This module depends on [`org.openmarkov.core`](../org.openmarkov.core/ARCHITECTURE.md) for all domain objects.

---

## Statistical Background

For a statistician, inference in a Bayesian network is essentially computing the marginal or conditional distribution of a subset of variables by summing (marginalizing) over the others, using the chain rule factorization expressed by the network structure.

OpenMarkov implements two families of exact inference algorithms:

1. **Junction tree (Hugin propagation)** — Converts the network into a tree of cliques, runs a message-passing algorithm, and then reads off exact marginals from the cliques. This is efficient when the network is not too dense (small tree-width).

2. **Variable elimination** — Chooses an ordering of the non-query, non-evidence variables, and eliminates them one by one by summing out each variable from the product of potentials that mention it. The order of elimination strongly affects computational cost (hence the heuristics below).

For **influence diagrams**, the goal shifts from computing marginals to finding the **optimal decision policy** — the strategy that maximizes expected utility.

---

## Package Structure

Source lives under `src/main/java/org/openmarkov/inference/`:

| Package | Contents |
|---------|----------|
| `huginPropagation` | Hugin junction-tree propagation for Bayesian networks |
| `variableElimination` | Variable elimination algorithm |
| `likelihoodWeighting` | Approximate inference via weighted sampling |
| `lilyPad` | Solver for LIMIDs (limited-memory influence diagrams) |
| `andOrSearch` | AND/OR search for MDPs and POMDPs |
| `decomposition` | Decomposition algorithms for multi-agent decision models |
| `heuristics` | Variable elimination order heuristics |
| `decisionTree` | Generates decision trees from influence diagrams |
| `temporal` | Temporal evaluation for dynamic Bayesian networks |

---

## Inference Algorithms

### HuginPropagation

`HuginPropagation` implements the classic **Hugin algorithm**:

1. **Moralization**: convert the directed network to an undirected moral graph (connect all parents of each node).
2. **Triangulation**: add fill-in edges until the graph is chordal (every cycle of length ≥ 4 has a chord).
3. **Junction tree construction**: identify maximal cliques; build a spanning tree weighted by clique intersections (separators).
4. **Initialization**: assign each CPT to a clique that contains all its variables.
5. **Evidence absorption**: multiply the CPTs for observed variables into their cliques.
6. **Message passing**: propagate (collect then distribute) probability messages up and down the tree.
7. **Marginalization**: read posterior distributions by marginalizing the clique potentials.

This algorithm runs in time exponential in the **tree-width** of the network, which is a measure of how densely connected the graph is. For typical networks (tree-width ≤ 10–15), it is very fast.

### VariableElimination

`VariableElimination` works variable by variable:

1. Choose an elimination ordering of all variables not in the query or evidence set.
2. For each variable *X* in that order:
   a. Collect all factors (potentials) that mention *X*.
   b. Multiply them together.
   c. Sum out *X*, producing a new, smaller factor.
3. Multiply the remaining factors and normalize.

The computational cost depends heavily on the ordering (see heuristics below).

### LilyPad

`LilyPad` solves **LIMIDs** (limited-memory influence diagrams) — a generalization of influence diagrams where decision nodes do not necessarily have access to all prior observations. It uses a message-passing scheme that alternates between computing expected utilities and updating decision policies until convergence.

### AndOrSearch

`AndOrSearch` solves **MDPs** (Markov decision processes) and **POMDPs** (partially observable MDPs) using an AND/OR search tree. AND nodes correspond to probabilistic outcomes; OR nodes correspond to decision choices. The algorithm finds the strategy with maximum expected discounted reward.

### LikelihoodWeighting

An approximate inference algorithm based on Monte Carlo sampling. Each sample is a full assignment to all variables, drawn by ancestral sampling with evidence variables fixed; the sample is weighted by the likelihood of the evidence. Useful when exact inference is computationally intractable (very dense networks).

---

## Variable Elimination Order Heuristics

The order in which variables are eliminated dramatically changes computational cost. OpenMarkov provides several heuristics, all of which try to keep intermediate factors small:

| Class | Strategy |
|-------|----------|
| `SimpleElimination` | Eliminates variables in a fixed static order |
| `MinimalFillIn` | Choose the variable whose elimination adds fewest fill-in edges |
| `WeightedMinFill` | Like MinFill, but weights fill edges by variable domain size |
| `HybridElimination` | Combines MinFill with a size criterion |
| `CanoMoralElimination` | Considers the moral graph structure |
| `LookaheadMinFill` | Looks ahead *k* steps before choosing |
| `LookaheadMinCliqueSize` | Looks ahead to minimize the largest clique |

`MinimalFillIn` is the default and performs well on most networks encountered in practice.

---

## Decision Analysis

For influence diagrams, the module provides:

- **`GenerateDecisionTreeTaskFactory`** — Converts an influence diagram into an explicit decision tree showing all possible sequences of decisions and outcomes, annotated with expected utilities.
- **`DecisionTreeManagerImpl`** — Manages the decision tree data structure and traversal.
- **Backward induction** — Evaluates the decision tree from leaves to root, propagating expected utilities upward to find the optimal policy at each decision node.

---

## Temporal Models

**Dynamic Bayesian networks (DBNs)** model how a set of variables evolves over time. The network is structured as a sequence of time slices, where each slice is a copy of the same variables connected by transition arcs to the next slice.

`TemporalEvaluation` rolls the inference forward through time:

1. Initialize the distribution over the first time slice using the prior CPTs.
2. For each subsequent time slice, multiply the current distribution by the transition CPTs and marginalize over the previous slice's variables.
3. Apply evidence at each time slice.

`MIDTemporalEvolution` handles the multi-agent generalization.

---

## How an Inference Call Works

A simplified view of what happens when the GUI triggers inference:

```
User sets evidence in the GUI
        │
        ▼
EvidenceCase is constructed (list of Findings)
        │
        ▼
InferenceManager selects the appropriate algorithm
  (based on NetworkType and InferenceOptions)
        │
        ▼
Algorithm.query(task, evidenceCase) is called
        │
        ▼
  [Algorithm-specific steps]
  e.g., HuginPropagation:
    1. Build junction tree
    2. Assign CPTs to cliques
    3. Absorb evidence
    4. Run message passing
        │
        ▼
Result object returned (Propagation, Evaluation, etc.)
        │
        ▼
GUI displays posterior distributions in node panels
```

---

## Tests

About 11 test classes verify algorithm correctness against known networks:

- Inference results checked against analytically computed values.
- Round-trip tests: build a network programmatically, run inference, check posteriors match expected values.
- Tests with and without evidence.
- Decision analysis tests: verify optimal policies in small influence diagrams.

Test fixture networks are in `src/test/resources/`.

Run with:

```bash
cd org.openmarkov.inference
mvn test
```
