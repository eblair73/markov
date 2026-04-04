# org.openmarkov.costEffectiveness — Architecture

**Role in the project:** Cost-effectiveness analysis and sensitivity analysis visualization for influence diagrams.
**Source files:** ~6 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Statistical and Decision-Analytic Background](#statistical-and-decision-analytic-background)
3. [Cost-Effectiveness Plane](#cost-effectiveness-plane)
4. [Tornado Diagram](#tornado-diagram)
5. [Sensitivity Analysis](#sensitivity-analysis)
6. [Relationship to Other Modules](#relationship-to-other-modules)

---

## What This Module Does

This module provides the graphical displays and analysis tools for **cost-effectiveness analysis** — a method widely used in health economics and policy analysis to compare the costs and benefits (usually measured in health outcomes) of alternative interventions. It also provides sensitivity analysis tools that show how the optimal decision changes as input parameters vary.

---

## Statistical and Decision-Analytic Background

### Cost-Effectiveness Analysis

In a medical or policy context, a **cost-effectiveness analysis** compares two or more interventions on two dimensions:
- **Expected cost** (e.g., total treatment expenditure per patient)
- **Expected effectiveness** (e.g., quality-adjusted life years, QALYs, gained)

Each intervention is represented as a point in the **cost-effectiveness (CE) plane**, with cost on the vertical axis and effectiveness on the horizontal axis. A decision-maker typically has a **willingness-to-pay threshold** λ (dollars per QALY), and an intervention is considered cost-effective if its **incremental cost-effectiveness ratio (ICER)** is below λ:

```
ICER = (Cost_new − Cost_old) / (Effect_new − Effect_old)
```

In OpenMarkov, these values are computed by running inference on an influence diagram whose utility nodes represent costs and effectiveness outcomes.

### Sensitivity Analysis

**Sensitivity analysis** asks: "How much does the optimal decision change if one of the input probabilities or parameters changes?" This is important because the CPT values in a network are often estimates with uncertainty.

---

## Cost-Effectiveness Plane

The CE plane is an interactive scatter plot (rendered with **JFreeChart**) where:

- Each point corresponds to one intervention (decision option).
- The x-axis shows expected effectiveness (higher is better).
- The y-axis shows expected cost (lower is better).
- A diagonal line representing the willingness-to-pay threshold λ divides the plane into cost-effective (below the line) and not cost-effective (above the line) regions.

Users can:
- Hover over points to see exact cost and effectiveness values.
- Adjust the λ threshold interactively.
- Export the plot as an image.

---

## Tornado Diagram

A **tornado diagram** displays the results of a one-way sensitivity analysis: for each uncertain parameter (CPT entry), it shows how much the expected value of the optimal decision changes when that parameter moves from its minimum to its maximum plausible value. Parameters are sorted by their impact (widest bar at the top), producing the characteristic tornado shape.

This immediately tells the analyst which parameters matter most and which can be safely ignored.

---

## Sensitivity Analysis

**Two-way sensitivity analysis** varies two parameters simultaneously and shows, as a 2D heat map or region plot, which decision is optimal for each combination of parameter values. This identifies the boundaries at which the preferred decision switches.

---

## Relationship to Other Modules

| Module | How it is used here |
|--------|-------------------|
| [`org.openmarkov.inference`](../org.openmarkov.inference/ARCHITECTURE.md) | Computes expected costs and effectiveness values by evaluating the influence diagram |
| [`org.openmarkov.core`](../org.openmarkov.core/ARCHITECTURE.md) | Provides `ProbNet`, `Node`, `Potential`, and `EvidenceCase` |
| [`org.openmarkov.gui`](../org.openmarkov.gui/ARCHITECTURE.md) | Hosts the CE plane and tornado diagram panels in the workspace |
| JFreeChart (external) | Renders charts |
