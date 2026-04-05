# OpenMarkov

OpenMarkov is an open-source Java application for building, visualizing, and analyzing **probabilistic graphical models** — mathematical structures that compactly represent the joint probability distribution of a collection of random variables.

It provides both a point-and-click graphical interface and a Java programming API, making it useful for statisticians who want to interactively explore models as well as developers who want to embed probabilistic reasoning in their own software.

---

## What You Can Do with OpenMarkov

- **Build networks visually** — draw Bayesian networks, influence diagrams, Markov networks, dynamic Bayesian networks, MDPs, and POMDPs using a drag-and-drop editor.
- **Run inference** — set observations (evidence) on any variables and instantly see updated posterior probability distributions on all other variables.
- **Learn from data** — import a dataset and use algorithms such as Hill Climbing, PC, Naive Bayes, or TAN to automatically estimate network structure and parameters.
- **Evaluate learned models** — assess classifier accuracy with k-fold cross-validation, confusion matrices, and precision/recall/F1 metrics.
- **Conduct decision analysis** — find optimal strategies in influence diagrams using backward induction; perform cost-effectiveness analysis with tornado diagrams.
- **Generate synthetic data** — forward-sample from any network to produce artificial datasets for testing or teaching.
- **Import and export** — read and write networks in PGMX, XMLBIF, XDSL, AMUA, and Elvira formats; import case data from Excel, Weka ARFF, or Elvira database files.

---

## Getting the Code (Mac)

These steps walk you through downloading the project onto your Mac for the first time, even if you have never used Git before.

### Step 1 — Install Git

Git is the tool used to download the code. To check if you already have it, open the **Terminal** app (search for "Terminal" in Spotlight with Cmd+Space) and run:

```bash
git --version
```

If you see a version number, Git is already installed — skip to Step 2.

If you see a pop-up asking you to install the **Command Line Developer Tools**, click **Install** and wait for it to finish. Once done, run `git --version` again to confirm.

If no pop-up appeared, install Git manually by running:

```bash
xcode-select --install
```

### Step 2 — Choose a folder

Decide where on your Mac you want to save the project. Your home folder or Desktop are common choices. In Terminal, navigate there. For example, to go to your Desktop:

```bash
cd ~/Desktop
```

### Step 3 — Clone the repository

Run the following command to download the project into a new folder called `markov`:

```bash
git clone https://github.com/eblair73/markov.git
```

This creates a `markov` folder containing all the source code. You only need to do this once.

### Step 4 — Enter the project folder

```bash
cd markov
```

You are now ready to build and run the application. Continue with the **Quick Start** steps below.

### Updating the code later

After the initial clone, you do not need to clone again. To pull down the latest changes from GitHub, open Terminal, navigate to the `markov` folder, and run:

```bash
cd ~/Desktop/markov
git pull
```

If you have local changes that conflict with what is on GitHub, Git will tell you. The safest way to discard any local edits and fully reset to the latest version on GitHub is:

```bash
git fetch origin
git reset --hard origin/develop
```

> **Warning:** `git reset --hard` permanently discards any uncommitted local changes. Only use it if you are sure you do not need them.

---

## Installing Java and Maven (Mac)

This project requires Java 21 and Maven 3.8 or higher. Follow these steps to set them up cleanly on a Mac using [Homebrew](https://brew.sh).

### Step 1 — Install Homebrew (if you don't have it)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### Step 2 — Remove any existing Java installations

Other Java variants (such as OpenJDK installed via Homebrew) can conflict. First, see what is installed:

```bash
brew list | grep -i java
```

Uninstall each entry you see. For example, if the output includes `openjdk` and `openjdk@17`, run:

```bash
brew uninstall openjdk openjdk@17
```

Replace the package names with whatever `brew list` actually showed. If nothing appeared, skip this step.

### Step 3 — Install Java 21

```bash
brew install --cask temurin@21
```

This installs Eclipse Temurin 21 (a production-ready OpenJDK distribution). Verify it:

```bash
java -version
```

You should see output containing `21`.

### Step 4 — Install Maven

```bash
brew install maven
```

Verify it:

```bash
mvn -version
```

You should see `Apache Maven 3.8` or higher.

---

## Quick Start

### Requirements

| Tool | Minimum version |
|------|----------------|
| [Java Development Kit (JDK)](https://adoptium.net/) | 21 |
| [Apache Maven](https://maven.apache.org/download.cgi) | 3.8 |

Verify your installation:

```bash
java -version   # must say 21 or higher
mvn -version    # must say 3.8 or higher
```

### Build

The project is a flat multi-module Maven layout. All modules are siblings inside `markov/` — the parent POM does not declare them, so you must build in two steps.

**Step 1 — install the parent POM** (only needed once, or after pulling parent changes):

```bash
cd markov/org.openmarkov
mvn install -DskipTests
cd ..
```

**Step 2 — build the executable JAR:**

```bash
cd markov/org.openmarkov.full
mvn clean package -DskipTests
```

Maven resolves the other module artifacts (core, gui, inference, etc.) from the configured Nexus snapshot repository. The first run downloads dependencies (~150 MB). The finished JAR will be at:

```
org.openmarkov.full/target/openmarkov-full-0.3.0-SNAPSHOT-jar-with-dependencies.jar
```

### Run

From inside the `org.openmarkov.full` directory:

```bash
java -jar target/org.openmarkov.full-0.3.0-SNAPSHOT-jar-with-dependencies.jar
```

Optional flags:

```bash
-l es                  # start in Spanish (default: en)
mynetwork.pgmx         # open a network file at startup
```

---

## Architecture Documentation

The project is documented in a set of linked architecture files, one for the overall system and one per module. All documents are written to be accessible to statisticians who may be new to Java development.

### Overall Architecture

| Document | Contents |
|----------|----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System overview, module dependency map, layer diagram, build and run instructions, glossary |

### Module Architecture Documents

| Document | Module | Summary |
|----------|--------|---------|
| [org.openmarkov.core](org.openmarkov.core/ARCHITECTURE.md) | Foundation | Domain model: networks, nodes, variables, potentials, constraints, edit system |
| [org.openmarkov.annotationProcessing](org.openmarkov.annotationProcessing/ARCHITECTURE.md) | Foundation | Compile-time code generation for internationalized strings |
| [org.openmarkov.inference](org.openmarkov.inference/ARCHITECTURE.md) | Computation | Hugin propagation, variable elimination, decision analysis, temporal models |
| [org.openmarkov.io](org.openmarkov.io/ARCHITECTURE.md) | File I/O | Network file format readers and writers (PGMX, XMLBIF, XDSL, AMUA, Elvira) |
| [org.openmarkov.io.database.excel](org.openmarkov.io.database.excel/ARCHITECTURE.md) | File I/O | Import case data from Excel spreadsheets (.xlsx) |
| [org.openmarkov.io.database.weka](org.openmarkov.io.database.weka/ARCHITECTURE.md) | File I/O | Import case data in Weka ARFF format |
| [org.openmarkov.io.database.elvira](org.openmarkov.io.database.elvira/ARCHITECTURE.md) | File I/O | Import case data in legacy Elvira format |
| [org.openmarkov.learning.core](org.openmarkov.learning.core/ARCHITECTURE.md) | Learning | Abstract framework: CaseDatabase, LearningAlgorithm base classes, plugin system |
| [org.openmarkov.learning.metric](org.openmarkov.learning.metric/ARCHITECTURE.md) | Learning | Scoring metrics: BIC, AIC, K2, MDL, log-likelihood |
| [org.openmarkov.learning.algorithm](org.openmarkov.learning.algorithm/ARCHITECTURE.md) | Learning | Concrete algorithms: Hill Climbing, PC, Naive Bayes, TAN, KDB, FANB, SNB, SPNB, EM |
| [org.openmarkov.learning.gui](org.openmarkov.learning.gui/ARCHITECTURE.md) | Learning | GUI dialogs for configuring and running learning algorithms |
| [org.openmarkov.gui](org.openmarkov.gui/ARCHITECTURE.md) | User Interface | Main Swing GUI: windows, menus, network editor, dialogs, theme system |
| [org.openmarkov.full](org.openmarkov.full/ARCHITECTURE.md) | User Interface | Application entry point; assembles the executable JAR |
| [org.openmarkov.bnEvaluation](org.openmarkov.bnEvaluation/ARCHITECTURE.md) | Analysis Plugin | Cross-validation, confusion matrix, accuracy/precision/recall/F1 |
| [org.openmarkov.costEffectiveness](org.openmarkov.costEffectiveness/ARCHITECTURE.md) | Analysis Plugin | Cost-effectiveness plane, ICER, tornado diagram, sensitivity analysis |
| [org.openmarkov.dbGenerator](org.openmarkov.dbGenerator/ARCHITECTURE.md) | Analysis Plugin | Ancestral sampling to generate synthetic datasets |
| [org.openmarkov.stochasticPropagationOutput](org.openmarkov.stochasticPropagationOutput/ARCHITECTURE.md) | Analysis Plugin | Parametric bootstrap for CPT uncertainty propagation |
| [org.openmarkov.integrationTests](org.openmarkov.integrationTests/ARCHITECTURE.md) | Testing | End-to-end tests spanning I/O, inference, learning, and the GUI |

---

## Project Structure

```
markov/
├── README.md                              ← this file
├── ARCHITECTURE.md                        ← overall architecture
├── org.openmarkov/                        ← parent Maven module (build coordinator)
├── org.openmarkov.core/                   ← domain model (foundation)
├── org.openmarkov.inference/              ← inference algorithms
├── org.openmarkov.io/                     ← network file I/O
├── org.openmarkov.io.database.excel/      ← Excel case data reader
├── org.openmarkov.io.database.weka/       ← Weka ARFF case data reader
├── org.openmarkov.io.database.elvira/     ← Elvira case data reader
├── org.openmarkov.learning.core/          ← learning algorithm framework
├── org.openmarkov.learning.metric/        ← scoring metrics (BIC, AIC, K2, …)
├── org.openmarkov.learning.algorithm/     ← learning algorithm implementations
├── org.openmarkov.learning.gui/           ← learning GUI dialogs
├── org.openmarkov.gui/                    ← main graphical user interface
├── org.openmarkov.full/                   ← entry point + executable JAR assembly
├── org.openmarkov.bnEvaluation/           ← classifier evaluation plugin
├── org.openmarkov.costEffectiveness/      ← cost-effectiveness analysis plugin
├── org.openmarkov.dbGenerator/            ← synthetic dataset generator plugin
├── org.openmarkov.stochasticPropagationOutput/ ← stochastic output plugin
└── org.openmarkov.integrationTests/       ← end-to-end test suite
```

---

## Supported Network Types

| Network type | Use case |
|-------------|---------|
| Bayesian network | General probabilistic reasoning and prediction |
| Markov network | Undirected probabilistic dependencies |
| Influence diagram | Decision making under uncertainty |
| LIMID | Decision making with limited memory |
| Dynamic Bayesian network | Temporal / time-series models |
| MDP | Sequential decision making (Markov decision process) |
| POMDP | Sequential decisions with partial observability |
| Multi-agent influence diagram | Decentralized decision making |

---

## Supported File Formats

| Format | Extension | Description |
|--------|-----------|-------------|
| ProbModel XML | `.pgmx` | OpenMarkov native format |
| XMLBIF | `.xml` | XML Bayesian Interchange Format |
| XDSL / DSL | `.xdsl` | GeNIe/SMILE format |
| AMUA | `.amua` | AMUA influence diagram format |
| Elvira | `.elv` | Legacy Elvira format |

Case data (for learning) can be imported from Excel (`.xlsx`), Weka ARFF (`.arff`), and Elvira database (`.dbc`) files.

---

## Interface Languages

English and Spanish are supported. Switch languages at startup:

```bash
java -jar openmarkov.jar -l es   # Spanish
java -jar openmarkov.jar -l en   # English (default)
```
