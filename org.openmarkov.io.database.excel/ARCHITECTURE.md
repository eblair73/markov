# org.openmarkov.io.database.excel — Architecture

**Role in the project:** Reads case databases (datasets) from Microsoft Excel spreadsheet files for use in learning algorithms.
**Source files:** ~5 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## What This Module Does

When you want to learn a Bayesian network from real data stored in an Excel spreadsheet (`.xlsx`), this module reads that file and produces a `CaseDatabase` object that learning algorithms can consume.

---

## Expected File Format

The Excel file should be formatted as a **flat data table**:

- **Row 1**: column headers, one per variable. Headers must exactly match the variable names in the network (or be remapped in the data import dialog).
- **Rows 2–N**: one observation (case) per row. Each cell contains the **state name** of the corresponding variable for that observation.

Example:

| Rain | Sprinkler | WetGrass |
|------|-----------|----------|
| yes  | no        | yes      |
| no   | yes       | yes      |
| no   | no        | no       |

Missing values (empty cells) are handled according to user-configured rules (skip the case, or treat as unknown).

---

## How It Works

`ExcelCaseDatabaseReader` uses **Apache POI** (the standard Java library for reading Microsoft Office files) to parse the `.xlsx` file:

1. Open the workbook and select the first sheet.
2. Read the header row to identify variable names.
3. For each subsequent row, read cell values and convert state names to integer indices (using the `Variable` objects from the network).
4. Accumulate the integer-indexed cases into a `CaseDatabase`.

The integer-index representation is important for performance: the scoring metrics in [`org.openmarkov.learning.metric`](../org.openmarkov.learning.metric/ARCHITECTURE.md) perform arithmetic on state indices, not string comparisons.

---

## Key Class

| Class | Role |
|-------|------|
| `ExcelCaseDatabaseReader` | Implements `CaseDatabaseReader`; reads `.xlsx` → `CaseDatabase` |

---

## Relationship to Other Modules

| Module | Relationship |
|--------|-------------|
| [`org.openmarkov.learning.core`](../org.openmarkov.learning.core/ARCHITECTURE.md) | Provides the `CaseDatabaseReader` interface and `CaseDatabase` class |
| [`org.openmarkov.core`](../org.openmarkov.core/ARCHITECTURE.md) | Provides `Variable` and `State` for column-to-variable mapping |
| Apache POI 5.4.0 (external) | Reads `.xlsx` files |
