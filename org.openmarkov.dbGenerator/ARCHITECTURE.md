# org.openmarkov.dbGenerator — Architecture

**Role in the project:** Generates synthetic datasets by forward-sampling from a Bayesian network.
**Source files:** ~5 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Statistical Background: Forward Sampling](#statistical-background-forward-sampling)
3. [Workflow](#workflow)
4. [Output Formats](#output-formats)
5. [Relationship to Other Modules](#relationship-to-other-modules)

---

## What This Module Does

This module generates a synthetic case database — an artificial dataset — by **ancestral (forward) sampling** from a Bayesian network. Given a network with specified CPTs, it produces any number of simulated observations that, in expectation, follow the joint distribution encoded by the network.

This is useful for:

- **Testing learning algorithms**: learn a network from the generated data and compare the learned structure to the known truth.
- **Teaching**: demonstrate how learning algorithms behave with different sample sizes.
- **Imputation research**: generate a complete dataset from a partially specified model.
- **Sensitivity studies**: assess how robust a learned model is to sample size.

---

## Statistical Background: Forward Sampling

**Ancestral sampling** generates a joint sample from a Bayesian network by exploiting the topological ordering of the graph:

1. Order the nodes so that all parents of a node come before it in the ordering (a **topological sort** of the DAG).
2. For each node in topological order:
   a. Look up the current states of all its parents.
   b. Extract the row of the CPT corresponding to those parent states — this gives a probability distribution over the node's own states.
   c. **Draw a sample** from that distribution (using a uniform random number to invert the CDF).
3. After all nodes are processed, one complete sample is recorded.
4. Repeat for the desired number of samples.

Because parents are always sampled before children, each node's CPT is always conditioned on already-sampled parent values, making this procedure exact — the samples follow the true joint distribution.

---

## Workflow

```
User opens a Bayesian network in the GUI
User selects Tools → Generate Database
         │
         ▼
DatabaseGeneratorDialog opens:
  - Enter number of cases to generate
  - Choose output format (Excel, CSV, ARFF)
  - Choose output file path
         │
         ▼
DatabaseGenerator.generate(net, numCases, outputPath, format)
  1. Compute topological ordering of nodes
  2. For each case 1..numCases:
     a. For each node in topological order:
        - Look up parent states (already sampled)
        - Extract CPT row
        - Sample a state from the distribution
     b. Write the complete assignment as one row
  3. Write all rows to the output file
         │
         ▼
Output file ready; user can import it for learning
```

---

## Output Formats

| Format | Extension | Compatible with |
|--------|-----------|----------------|
| Excel spreadsheet | `.xlsx` | Microsoft Excel, LibreOffice Calc, and the [`io.database.excel`](../org.openmarkov.io.database.excel/ARCHITECTURE.md) importer |
| Weka ARFF | `.arff` | Weka, and the [`io.database.weka`](../org.openmarkov.io.database.weka/ARCHITECTURE.md) importer |
| Elvira database | `.dbc` | Elvira, and the [`io.database.elvira`](../org.openmarkov.io.database.elvira/ARCHITECTURE.md) importer |

The column headers in the output file match the variable names in the network; each column's values are the state names of that variable.

---

## Relationship to Other Modules

| Module | How it is used here |
|--------|-------------------|
| [`org.openmarkov.core`](../org.openmarkov.core/ARCHITECTURE.md) | Provides `ProbNet`, `Variable`, `TablePotential`, and topological sort utilities |
| [`org.openmarkov.gui`](../org.openmarkov.gui/ARCHITECTURE.md) | Hosts the generator dialog and menu item |
| Apache POI (external) | Writes Excel `.xlsx` output files |
