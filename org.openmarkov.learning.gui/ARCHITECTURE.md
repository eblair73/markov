# org.openmarkov.learning.gui — Architecture

**Role in the project:** GUI dialogs and panels for configuring and running Bayesian network learning.
**Source files:** ~25 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [User Workflow](#user-workflow)
3. [Key Dialogs and Panels](#key-dialogs-and-panels)
4. [How Algorithm and Metric Selection Works](#how-algorithm-and-metric-selection-works)
5. [Relationship to Other Modules](#relationship-to-other-modules)

---

## What This Module Does

This module provides the **graphical front-end** for the learning subsystem. It bridges the gap between the user (clicking buttons in the GUI) and the learning algorithms (Java code in [`org.openmarkov.learning.algorithm`](../org.openmarkov.learning.algorithm/ARCHITECTURE.md)). Without this module, running learning would require writing Java code; with it, a user can configure and launch learning through a series of dialog windows.

---

## User Workflow

```
User selects Learning → Learn Network from Data
         │
         ▼
[1] DataImportDialog — choose a case database file
    (Excel, Weka ARFF, or Elvira format)
         │
         ▼
[2] AlgorithmConfigDialog — choose algorithm and metric
    - Select algorithm (Hill Climbing, PC, Naive Bayes, …)
    - Select scoring metric (BIC, AIC, K2, …) [if applicable]
    - Set algorithm-specific parameters
         │
         ▼
[3] Learning runs on a background thread
    - Progress bar shows completion percentage
    - Cancel button available
         │
         ▼
[4] Result displayed in the workspace
    - Learned network opens as a new tab
    - Learning report (score, number of arcs, running time) shown
```

---

## Key Dialogs and Panels

### DataImportDialog

Lets the user select a case database file and previews the data:

- File-chooser filtered to `.xlsx`, `.arff`, and `.dbc` extensions.
- Preview table showing the first 10 rows of the imported data.
- Variable mapping: allows the user to match dataset columns to network variables if names differ.

### AlgorithmConfigDialog

Populated dynamically by querying `AlgorithmManager` (from [`org.openmarkov.learning.core`](../org.openmarkov.learning.core/ARCHITECTURE.md)) for available algorithms:

- Dropdown menu listing all discovered algorithms.
- When a score-and-search algorithm is selected, a second dropdown appears for metric selection (populated from `MetricManager` in [`org.openmarkov.learning.metric`](../org.openmarkov.learning.metric/ARCHITECTURE.md)).
- Algorithm-specific parameter panel (e.g., maximum parents for Hill Climbing, significance level for PC).

### ProgressPanel

Shown while learning runs:

- Progress bar (percentage of search steps completed).
- Elapsed time counter.
- Current best score display (for Hill Climbing).
- Cancel button that safely interrupts the background thread.

### LearningResultPanel

Shown after learning completes:

- Summary statistics (final score, number of arcs, number of nodes, running time).
- Option to open the learned network in the editor.
- Option to run cross-validation evaluation (launches [`org.openmarkov.bnEvaluation`](../org.openmarkov.bnEvaluation/ARCHITECTURE.md)).

---

## How Algorithm and Metric Selection Works

Because algorithms and metrics are discovered via annotations (not hard-coded), the dialogs in this module never need to be updated when new algorithms or metrics are added. The sequence is:

1. Dialog opens → queries `AlgorithmManager.getAvailableAlgorithms()`.
2. `AlgorithmManager` returns a list of `(name, class)` pairs discovered at startup.
3. Dialog populates the dropdown.
4. User selects "Hill Climbing" → dialog checks whether the class implements `ScoreAndSearchAlgorithm` → if yes, shows metric dropdown.
5. User clicks OK → dialog instantiates the chosen algorithm class with the configured parameters and calls `algorithm.run()` on a background thread.

---

## Relationship to Other Modules

| Module | How it is used here |
|--------|-------------------|
| [`org.openmarkov.learning.algorithm`](../org.openmarkov.learning.algorithm/ARCHITECTURE.md) | Provides the algorithm implementations that this GUI configures and runs |
| [`org.openmarkov.learning.metric`](../org.openmarkov.learning.metric/ARCHITECTURE.md) | Provides scoring metrics available for selection |
| [`org.openmarkov.learning.core`](../org.openmarkov.learning.core/ARCHITECTURE.md) | Provides `AlgorithmManager`, `CaseDatabase`, and `LearningAlgorithm` base class |
| [`org.openmarkov.io.database.*`](../org.openmarkov.io/ARCHITECTURE.md) | Reads the case database file chosen by the user |
| [`org.openmarkov.gui`](../org.openmarkov.gui/ARCHITECTURE.md) | Hosts these dialogs; provides the parent window |
| [`org.openmarkov.bnEvaluation`](../org.openmarkov.bnEvaluation/ARCHITECTURE.md) | Evaluates the learned network if the user requests it |
