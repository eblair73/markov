# org.openmarkov.gui — Architecture

**Role in the project:** The graphical user interface — windows, menus, the network editor, and all dialogs.
**Source files:** ~322 Java files
**[← Back to overall architecture](../ARCHITECTURE.md)**

---

## Table of Contents

1. [What This Module Does](#what-this-module-does)
2. [Technology: Java Swing](#technology-java-swing)
3. [Package Structure](#package-structure)
4. [Main Window](#main-window)
5. [Network Editor (Graphical Canvas)](#network-editor-graphical-canvas)
6. [Dialogs](#dialogs)
7. [Menu and Toolbar](#menu-and-toolbar)
8. [Theme System](#theme-system)
9. [Validation](#validation)
10. [Exception Handling in the GUI](#exception-handling-in-the-gui)
11. [Localization](#localization)
12. [Tests](#tests)

---

## What This Module Does

This module provides the complete graphical user interface for OpenMarkov. A user interacts with this layer to:

- Create, open, and save probabilistic network files.
- Add and edit nodes and arcs in a network by clicking on a canvas.
- Edit conditional probability tables (CPTs) in a table editor.
- Run inference and see updated probability bars in each node.
- Launch learning algorithms on imported datasets.
- View cost-effectiveness plots and decision trees.

This module depends on [`org.openmarkov.inference`](../org.openmarkov.inference/ARCHITECTURE.md), [`org.openmarkov.io`](../org.openmarkov.io/ARCHITECTURE.md), and [`org.openmarkov.core`](../org.openmarkov.core/ARCHITECTURE.md). Plugin modules (cost-effectiveness, BN evaluation, etc.) depend *on* this module.

---

## Technology: Java Swing

**Swing** is the standard Java library for building desktop graphical applications. It provides windows, buttons, text fields, tables, scroll panes, and other visual widgets. Swing widgets are called **components**, and they are arranged in a tree (the **component hierarchy**) — a window contains panels, panels contain buttons and labels, and so on.

Swing is **single-threaded**: all GUI updates must happen on the **Event Dispatch Thread (EDT)**. Long-running operations (inference, learning) must run on a background thread and post results back to the EDT using `SwingUtilities.invokeLater(...)`. OpenMarkov uses `SwingWorker` for background computation.

**FlatLaf** is a third-party library that replaces Swing's default appearance with a modern, flat look-and-feel supporting dark mode and UI scaling.

---

## Package Structure

| Package | Contents |
|---------|----------|
| `window` | Main application window, document management, workspace tabs |
| `action` | Event handlers for menu items and toolbar buttons |
| `menutoolbar` | Menu bar and toolbar construction |
| `dialog` | Modal dialogs (new network wizard, preferences, node properties, CPT editor) |
| `component` | Reusable Swing component wrappers |
| `commonComponents` | Shared visual elements used across dialogs |
| `graphic` | Network visualization: node rendering, arc rendering, layout |
| `validator` | Input validation for user-entered text |
| `configuration` | Application preferences (language, look-and-feel, UI scale) |
| `loader` | Resource loading (icons, fonts, theme files) |
| `toolplugin` | Plugin interfaces for custom tools and look-and-feels |
| `exception` | GUI-specific exception types and the global exception handler |

---

## Main Window

`MainGUI` is the **singleton** main application window (there is exactly one instance). It:

- Contains a menu bar and a toolbar at the top.
- Contains a **workspace** in the center — a tabbed pane where each tab shows one open network.
- Contains a status bar at the bottom showing contextual messages.

When a user opens a network file:
1. The file-chooser dialog returns a path.
2. `MainGUI` calls `FormatManager` (from [`org.openmarkov.io`](../org.openmarkov.io/ARCHITECTURE.md)) to read the `ProbNet`.
3. A new tab is created and a `NetworkPanel` is placed in it.
4. The `NetworkPanel` renders the network on screen.

---

## Network Editor (Graphical Canvas)

The network editor is the central interactive component. It lives in the `graphic` package.

### Node Rendering

Each node is drawn as a rectangle or oval on a 2D canvas. The node's visual appearance conveys its type:
- **Chance nodes** (random variables): oval.
- **Decision nodes**: rectangle.
- **Utility nodes**: diamond/hexagon.

When inference is active, each chance node additionally shows a **bar chart** of its current marginal (or posterior) distribution directly inside the node box.

### Arc Rendering

Arcs are drawn as arrows between nodes. The direction of the arrow follows the direction of probabilistic dependence (parent → child).

### Mouse Interaction

The canvas listens for mouse events:

- **Click on empty space**: deselect; in "add node" mode, creates a new node.
- **Click on a node**: select the node; show its properties.
- **Drag from one node to another**: in "add arc" mode, creates a directed arc.
- **Drag a node**: moves it on the canvas.
- **Double-click on a node**: opens the node properties dialog.

Every mouse-triggered change goes through the edit system ([`core`](../org.openmarkov.core/ARCHITECTURE.md)): a `PNEdit` is created, validated, and executed. This ensures undo/redo works for graphical operations.

### Layout

A force-directed layout algorithm can automatically arrange nodes on the canvas so that connected nodes are close and overlapping nodes are pushed apart.

---

## Dialogs

Key dialogs in the `dialog` package:

| Dialog | Purpose |
|--------|---------|
| New Network Wizard | Choose network type, name, initial settings |
| Node Properties Dialog | Edit variable name, states, node type, position |
| CPT Editor Dialog | Edit the conditional probability table as a spreadsheet |
| Preferences Dialog | Language, look-and-feel, UI scale |
| Learning Configuration Dialog | Select algorithm, metric, dataset file |
| Inference Options Dialog | Select algorithm, elimination heuristic |
| Open/Save File Dialogs | Standard file-chooser filtered by supported extensions |

---

## Menu and Toolbar

Menus and toolbar buttons are constructed in the `menutoolbar` package. Each action is implemented as an `AbstractAction` subclass in the `action` package. This separates the visual component (menu item / button) from the business logic (what happens when you click).

Menu items and their labels are loaded from the localization XML files so that they automatically switch language when the user changes the language preference.

---

## Theme System

The GUI supports pluggable look-and-feels via the `UILookAndFeelPlugin` interface in the `toolplugin` package:

- The default theme uses **FlatLaf** for a modern appearance.
- Dark mode is available.
- UI scaling adapts the interface for high-DPI (Retina) displays.

The active look-and-feel is saved in application preferences and restored on next startup.

---

## Validation

The `validator` package provides input validators for text fields. For example:

- `VariableNameValidator` ensures node names are non-empty, unique, and contain only permitted characters.
- `StateNameValidator` ensures state names within a variable are unique.

Validators are called before an edit is submitted to the edit system, giving the user immediate feedback (field turns red, error message shown) rather than failing silently.

---

## Exception Handling in the GUI

`OMExceptionHandler` is registered as the global uncaught exception handler. When an unhandled exception escapes any thread:

1. The stack trace is written to the log file.
2. A dialog box is shown to the user with a friendly message and the option to report the bug.

`CorruptNetworkFile` is a GUI-specific exception thrown when a network file cannot be parsed, prompting the user with a clear error message.

---

## Localization

All user-visible strings in menus, dialogs, and tooltips come from XML resource files:

- `src/main/resources/gui/localize/Menus_en.xml` / `Menus_es.xml`
- `src/main/resources/gui/localize/Exceptions_en.xml` / `Exceptions_es.xml`
- Additional files per dialog.

When the user changes the language preference, the GUI rebuilds its menus and repaints its components using the new locale. The [`annotationProcessing`](../org.openmarkov.annotationProcessing/ARCHITECTURE.md) module binds class names to string keys at compile time.

---

## Tests

About 24 test classes cover:

- Component rendering tests (does the node panel display the correct probability bars?).
- Action tests (does clicking "Add Node" result in the correct `AddNodeEdit`?).
- Validation tests (does the validator correctly reject invalid variable names?).
- Swing integration tests using **AssertJ-Swing** (a library that simulates mouse clicks and keyboard input in headless tests).

Run with:

```bash
cd org.openmarkov.gui
mvn test
```
