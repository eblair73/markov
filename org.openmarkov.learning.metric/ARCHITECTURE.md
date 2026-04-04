# org.openmarkov.learning.metric — Architecture

**Role in the project:** Implements scoring functions that measure how well a network structure fits data.
**Source files:** ~17 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Statistical Background](#statistical-background)
3. [Implemented Metrics](#implemented-metrics)
4. [Class Structure](#class-structure)
5. [How a Metric Is Used During Learning](#how-a-metric-is-used-during-learning)
6. [Tests](#tests)

---

## What This Module Does

A **scoring metric** assigns a real number to a (network structure, dataset) pair: higher is better. During structure learning, an algorithm proposes changes to the network graph and uses the metric to decide whether each change is an improvement. This module provides implementations of the most widely used metrics.

---

## Statistical Background

All metrics are based on the **log-likelihood** of the data given the model, possibly with a penalty term for model complexity (to avoid overfitting):

```
Score(structure, data) = log-likelihood(data | structure) − complexity-penalty(structure)
```

The log-likelihood of a Bayesian network with CPTs estimated from data decomposes over each node:

```
log L = Σ_i  Σ_{j} Σ_{k}  N_{ijk} · log( N_{ijk} / N_{ij} )
```

where:
- *i* indexes nodes (variables),
- *j* indexes configurations of the parent set of node *i*,
- *k* indexes states of node *i*,
- *N_{ijk}* is the number of cases in the data with parent configuration *j* and child state *k*,
- *N_{ij}* = Σ_k N_{ijk}.

The metrics differ in how they penalize complexity:

| Metric | Penalty for complexity |
|--------|----------------------|
| **Log-Likelihood (LL)** | None — maximizes fit to data; overfits readily |
| **BIC** | (log N / 2) · dim, where dim = number of free parameters |
| **AIC** | dim |
| **MDL** | Same as BIC (different derivation) |
| **K2** | Bayesian score with a Dirichlet prior on CPTs |
| **AGG** | Weighted combination of multiple criteria |

**BIC** (Bayesian Information Criterion) is the default and most commonly used metric. It is consistent — as the sample size grows, it recovers the true structure (assuming the true distribution is in the model class).

**K2** is a Bayesian score that assumes a uniform Dirichlet prior over CPTs. It requires a topological ordering of variables as input (i.e., you must specify which variables can be parents of which).

---

## Implemented Metrics

| Class | Metric | Notes |
|-------|--------|-------|
| `MetricLL` | Log-Likelihood | No penalty; tends to overfit |
| `MetricBIC` | Bayesian Information Criterion | Default; penalty = (log N / 2) · params |
| `MetricAIC` | Akaike Information Criterion | Penalty = params (lighter than BIC) |
| `MetricMDL` | Minimum Description Length | Numerically identical to BIC |
| `MetricK2` | K2 score | Bayesian; requires variable ordering |
| `MetricAGG` | AGG metric | Aggregated / ensemble scoring |
| Conditional variants | As above, but conditioning on a class variable | Used for discriminative classifier learning |

---

## Class Structure

```
Metric (abstract)
  ├── MetricLL
  ├── MetricBIC
  ├── MetricAIC
  ├── MetricMDL
  ├── MetricK2
  └── MetricAGG

MetricManager  ← registry; maps metric names to classes
```

### Metric (abstract base class)

Every metric provides:

- `score(Node node, CaseDatabase data)` — Returns the contribution of one node to the overall network score. Because the BIC score decomposes over nodes, the total score is the sum of individual node scores.
- `scoreEdit(PNEdit edit, ProbNet net, CaseDatabase data)` — Returns the **change** in score caused by a proposed edit (e.g., adding an arc). Computing only the delta (rather than the full score each time) makes the Hill Climbing algorithm much faster.

### MetricManager

Analogous to `AlgorithmManager`, this singleton discovers metrics by scanning for `@MetricType`-annotated classes and registers them. The GUI then populates a metric selection dropdown.

---

## How a Metric Is Used During Learning

During **Hill Climbing** (see [`org.openmarkov.learning.algorithm`](../org.openmarkov.learning.algorithm/ARCHITECTURE.md)):

```
Initialize: current_score = metric.score(currentNet, data)

Repeat:
    For each candidate edit e (add/remove/reverse each arc):
        delta = metric.scoreEdit(e, currentNet, data)
    
    If max(delta) > 0:
        Apply the edit with the highest positive delta
        Update current_score
    Else:
        Stop (local optimum reached)
```

The key optimization: `scoreEdit` only recomputes the score contributions of the nodes affected by the edit (the child node and, for arc reversal, the parent node). This reduces each iteration from O(n) to O(1) node-score evaluations.

---

## Tests

About 6 test classes verify:

- Score values against manually computed results on toy datasets.
- Score decomposition: total score equals the sum of per-node scores.
- Delta computation: `scoreEdit` returns the correct change.
- MetricManager discovers all annotated metric classes.

Run with:

```bash
cd org.openmarkov.learning.metric
mvn test
```
