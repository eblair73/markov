# org.openmarkov.bnEvaluation — Architecture

**Role in the project:** Cross-validation and performance metrics for evaluating learned Bayesian network classifiers.
**Source files:** ~27 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Statistical Background](#statistical-background)
3. [Cross-Validation Workflow](#cross-validation-workflow)
4. [Performance Metrics](#performance-metrics)
5. [Visualizations](#visualizations)
6. [Relationship to Other Modules](#relationship-to-other-modules)

---

## What This Module Does

After you learn a Bayesian network classifier from data, you need to evaluate how good it is — does it generalize to new data, or did it merely memorize the training set? This module provides the tools to answer that question.

---

## Statistical Background

**Cross-validation** is the standard technique for estimating a model's out-of-sample predictive accuracy when the dataset is too small to split into separate training and test sets. The most common variant, **k-fold cross-validation**, works as follows:

1. Randomly partition the *N* cases in the dataset into *k* equal-sized **folds**.
2. For fold *i* = 1, …, k:
   a. Use all folds **except** fold *i* as training data; learn the classifier.
   b. Use fold *i* as test data; record predictions and true labels.
3. Aggregate all *N* predictions into a **confusion matrix**.
4. Compute summary metrics (accuracy, precision, recall, F1, …) from the confusion matrix.

This gives an approximately unbiased estimate of the classifier's true error rate on unseen data. The most common choice is k = 10 (ten-fold cross-validation).

---

## Cross-Validation Workflow

```
User selects:
  - A trained Bayesian network classifier
  - A case database (the full dataset)
  - Number of folds k (default: 10)
  - The class variable
         │
         ▼
BNEvaluationManager.runCrossValidation(net, data, k, classVar)
         │
         ▼
For each fold i = 1..k:
  1. Split data into training set (all cases not in fold i)
                    and test set (cases in fold i)
  2. Re-learn the classifier on the training set
     (using org.openmarkov.learning.algorithm)
  3. For each case in the test set:
     - Set all non-class variables as evidence
     - Run inference to get P(class | evidence)
       (using org.openmarkov.inference)
     - Predicted class = argmax P(class | evidence)
     - Compare to true class; record in confusion matrix
         │
         ▼
Aggregate confusion matrices across all folds
         │
         ▼
Compute and display performance metrics and visualizations
```

---

## Performance Metrics

Given the *k*-class confusion matrix *C* (where *C[i][j]* = number of cases with true class *i* predicted as class *j*):

| Metric | Definition |
|--------|-----------|
| **Accuracy** | Fraction of cases correctly classified: Σ C[i][i] / N |
| **Precision (per class)** | True positives / (True positives + False positives) |
| **Recall (per class)** | True positives / (True positives + False negatives) |
| **F1 score (per class)** | Harmonic mean of precision and recall: 2 · P · R / (P + R) |
| **Macro-average F1** | Unweighted average of per-class F1 scores |
| **Weighted-average F1** | Average weighted by true class frequency |

---

## Visualizations

The module provides two GUI components:

- **Confusion Matrix Table**: an interactive spreadsheet-style table showing the full *k*×*k* confusion matrix, with row and column labels for each class state.
- **Performance Summary Panel**: a panel listing overall accuracy, per-class precision/recall/F1, and macro/weighted averages.

Both components integrate with the main GUI window as panels that appear in the OpenMarkov workspace after a cross-validation run completes.

---

## Relationship to Other Modules

| Module | How it is used here |
|--------|-------------------|
| [`org.openmarkov.learning.algorithm`](../org.openmarkov.learning.algorithm/ARCHITECTURE.md) | Re-trains the classifier on each fold's training set |
| [`org.openmarkov.inference`](../org.openmarkov.inference/ARCHITECTURE.md) | Classifies each test case |
| [`org.openmarkov.learning.core`](../org.openmarkov.learning.core/ARCHITECTURE.md) | Provides `CaseDatabase` for data splitting |
| [`org.openmarkov.gui`](../org.openmarkov.gui/ARCHITECTURE.md) | Hosts the visualization panels in the main window |
