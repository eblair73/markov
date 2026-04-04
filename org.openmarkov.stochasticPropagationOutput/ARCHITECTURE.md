# org.openmarkov.stochasticPropagationOutput — Architecture

**Role in the project:** Visualizes the uncertainty in inference results using sampling-based output.
**Source files:** ~6 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Statistical Background](#statistical-background)
3. [How It Integrates with the Inference Pipeline](#how-it-integrates-with-the-inference-pipeline)
4. [Output Format](#output-format)
5. [Relationship to Other Modules](#relationship-to-other-modules)

---

## What This Module Does

Most inference results in OpenMarkov are **exact** — given a network with known, fixed CPT values, the posterior probabilities are computed precisely. But in practice the CPT values themselves are uncertain (they are estimates from data, with sampling error). This module provides an alternative output mode where inference results are reported as **distributions over probabilities** rather than single point estimates, by running inference many times with CPT values perturbed according to their uncertainty.

---

## Statistical Background

Suppose a CPT entry *p* was estimated from a dataset of size *n*. Under a Dirichlet-multinomial model, the posterior distribution of *p* is a **Dirichlet distribution**, and the uncertainty in *p* decreases as *n* grows.

**Stochastic propagation** (also called parametric bootstrapping in this context) works as follows:

1. For each CPT entry, compute its uncertainty distribution (e.g., a Beta or Dirichlet posterior).
2. Draw a random sample of CPT values from those distributions.
3. Run exact inference with the sampled CPTs to get one realization of the posterior probability for each query variable.
4. Repeat steps 2–3 many times (e.g., 1,000 iterations).
5. Summarize the distribution of realized posterior probabilities: mean, standard deviation, 95% credible interval.

The result tells the analyst not only *what* the posterior probability is, but also *how confident* we should be in that estimate.

---

## How It Integrates with the Inference Pipeline

This module registers a custom **inference output formatter** via the plugin system. When the user selects "Stochastic Propagation Output" in the inference options:

1. The inference algorithm runs as normal.
2. Instead of displaying point-estimate probability bars, the output formatter draws each node's distribution of posterior probabilities as a box plot or credible interval bar.
3. A summary table lists mean ± standard deviation for each variable's states.

---

## Output Format

For each query variable, the stochastic output reports:

| Quantity | Description |
|----------|-------------|
| Mean | Average posterior probability across all simulation runs |
| Std. Dev. | Standard deviation of posterior probabilities |
| 2.5th percentile | Lower bound of 95% credible interval |
| 97.5th percentile | Upper bound of 95% credible interval |

These are displayed both numerically (in a table) and graphically (as error bars or box plots alongside the standard probability bars in the network editor).

---

## Relationship to Other Modules

| Module | How it is used here |
|--------|-------------------|
| [`org.openmarkov.inference`](../org.openmarkov.inference/ARCHITECTURE.md) | Runs the underlying exact inference algorithm repeatedly |
| [`org.openmarkov.core`](../org.openmarkov.core/ARCHITECTURE.md) | Provides `ProbNet`, `Potential`, `TablePotential` for CPT perturbation |
| [`org.openmarkov.gui`](../org.openmarkov.gui/ARCHITECTURE.md) | Hosts the output visualization in the network editor |
