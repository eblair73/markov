# Informe de Oportunidades de Refactorización — org.openmarkov.core

> Generado el 2026-03-28. Basado en análisis estático del código fuente.

---

## Resumen ejecutivo

Se identificaron **12 categorías de oportunidades de refactorización** en el módulo `org.openmarkov.core`. Los problemas más graves afectan a clases con demasiadas responsabilidades (`DiscretePotentialOperations`, `ProbNet`), duplicación masiva de código en las factories de red, inconsistencia en la seguridad ante concurrencia, y cadenas de `instanceof` que impiden la extensión sin modificar código existente.

---

## 1. Clases con demasiadas responsabilidades (God Objects)

### 1.1 `DiscretePotentialOperations` — 2.287 líneas

**Archivo:** `src/main/java/org/openmarkov/core/model/network/potential/operation/DiscretePotentialOperations.java`

**Problema:** Una sola clase gestiona multiplicación, suma, marginalización, maximización, restricciones de enlace y manejo de `StrategyTree`. Tiene más de 12 métodos privados estáticos de apoyo dispersos por el fichero.

**Responsabilidades detectadas:**
1. Multiplicación de potenciales (líneas 88–208)
2. Suma y marginalización (líneas 247–387)
3. Maximización (líneas 390–450)
4. Gestión de intervenciones / StrategyTree (líneas 149–206 y 291–365)
5. Iteración de coordenadas multidimensionales (lógica repetida en varios métodos)
6. Gestión de criterios de decisión (líneas 465–481)

**Refactorización sugerida:**
- Extraer `MultiplicationOperation`, `SumOperation`, `MaximizationOperation` siguiendo el patrón Strategy.
- Extraer `MultidimensionalCoordinateIterator` para toda la aritmética de índices/offsets.
- Extraer `InterventionHandler` para el manejo de `StrategyTree` en operaciones.

---

### 1.2 `ProbNet` — 1.440 líneas

**Archivo:** `src/main/java/org/openmarkov/core/model/network/ProbNet.java`

**Problema:** Gestiona estructura del grafo, restricciones, potenciales, agentes, criterios, propiedades temporales y metadatos de red. Tiene más de 15 tipos de campo con propósitos distintos (líneas 98–162).

**Responsabilidades detectadas:**
1. Estructura del grafo (nodos y arcos)
2. Gestión de restricciones (`addConstraint`, `checkConstraint`, …)
3. Gestión de potenciales (`getPotentials`, `constantPotentials`, …)
4. Metadatos (`name`, `comment`, `agents`, `criteria`, `defaultStates`, `cycleLength`)
5. Soporte para redes temporales

**Refactorización sugerida:**
- Extraer `ProbNetMetadata` (nombre, comentario, agentes, criterios, estados por defecto, longitud de ciclo).
- Extraer `ProbNetConstraintRegistry` (añadir, eliminar, validar restricciones).
- Extraer `ProbNetPotentialRegistry` (gestión de `constantPotentials` y helpers de potenciales).
- Usar un patrón `Facade` sobre `ProbNet` para mantener compatibilidad hacia atrás.

---

## 2. Duplicación masiva de código en factories

### 2.1 `DANFactory` y `IDFactory`

**Archivos:**
- `src/main/java/org/openmarkov/core/model/network/factory/DANFactory.java`
- `src/main/java/org/openmarkov/core/model/network/factory/IDFactory.java`

**Problema:** Los métodos de construcción de redes son copias casi idénticas entre sí. El ejemplo más claro en `DANFactory`:

```java
// buildDANPerfectKnowledge() — líneas 54–86
// buildDANNoKnowledge()      — líneas 90–123
// CÓDIGO IDÉNTICO excepto la última línea:
// línea 84: nodeDisease.setAlwaysObserved(true);
// línea 120: nodeDisease.setAlwaysObserved(false);
```

El patrón repetido en todos los métodos:
1. Crear `ProbNet` con un tipo.
2. Crear `Variable`s con nombres y estados hardcodeados.
3. Añadir `Node`s con `NodeType`.
4. Configurar arcos.
5. Crear `TablePotential`s y asignar valores.

**Refactorización sugerida:**
- Aplicar el patrón Template Method con un método base `buildDAN(boolean alwaysObserved)`.
- Crear un `NetworkBuilder` fluido que encapsule el paso 1–5.
- Extraer `PotentialBuilder` para la creación de `TablePotential` con valores:
  ```java
  PotentialBuilder.forVariable(variableX)
      .withRole(CONDITIONAL_PROBABILITY)
      .withValues(0.86, 0.14)
      .build();
  ```
- Reducción estimada: de ~4.200 líneas a ~1.500.

---

## 3. Métodos excesivamente largos

### 3.1 `multiply()` en `DiscretePotentialOperations`

**Líneas:** 88–208 (120 líneas)

**Problema:** Mezcla validación de entradas, inicialización, cómputo con bucles anidados, gestión de `StrategyTree` y construcción del resultado.

**Refactorización sugerida:** Extraer:
- `validateMultiplyInput(List<TablePotential>)` — ~5 líneas
- `prepareMultiplicationData(List<TablePotential>)` — ~20 líneas
- `computeMultiplicationResults(...)` — ~45 líneas
- `buildMultiplicationResult(...)` — ~10 líneas

### 3.2 `sum()` en `DiscretePotentialOperations`

**Líneas:** 247–387 (140 líneas)

Mismos problemas que `multiply()`, con complejidad adicional por el manejo de potenciales constantes. Aplicar la misma descomposición.

---

## 4. Obsesión por primitivos

### 4.1 Nombres como `String` desnudos

**Archivos:** `Node.java` (líneas 190–192), `Variable.java`, `State.java`

Los nombres de nodo, variable y estado son `String` sin validación. Se hacen comparaciones con `equals()` directo en toda la base de código.

**Refactorización sugerida:** Crear value objects `NodeName`, `VariableName`, `StateName` con validación en el constructor.

### 4.2 Constantes de error como `String` estáticos

**Archivo:** `DiscretePotentialOperations.java` (líneas 52–54)

```java
private static final String nullVariable  = "decision variable = null";
private static final String nullPotentials = "potentials = null";
private static final String noPotentials   = "zero potentials";
```

**Refactorización sugerida:** Sustituir por un `enum PotentialOperationError` que implemente `IBundledOpenMarkovException` para localización consistente.

### 4.3 Arrays de `double` expuestos directamente

**Archivo:** `TablePotential.java` (líneas 51, 52, 58)

```java
public volatile double[] values;
public volatile StrategyTree[] strategyTrees;
public volatile UncertainValue[] uncertainValues;
```

Múltiples clases (p. ej. `DANFactory` línea 44) escriben directamente en `potentialX.values = new double[]{ … }`.

**Refactorización sugerida:** Crear clase `PotentialTable` que encapsule `double[]` con acceso controlado y aritmética de índices.

---

## 5. Cadenas de `instanceof` (violación del principio Open/Closed)

### 5.1 `BasicOperations` — selección de operación por tipo

**Patrón detectado en `BasicOperations.java` (líneas 47–51):**

```java
if (nodePotential instanceof SumPotential) {
    newTable = DiscretePotentialOperations.sum(parentsPotentials);
} else if (nodePotential instanceof ProductPotential) {
    newTable = DiscretePotentialOperations.multiply(parentsPotentials);
} else { // FunctionPotential
    // lógica diferente
}
```

Añadir un nuevo tipo de potencial exige modificar esta clase.

**Refactorización sugerida:** Definir `interface OperablePotential { TablePotential apply(List<TablePotential> inputs); }` e implementarla en cada subclase.

### 5.2 Verificaciones `instanceof TablePotential` en `PotentialOperations`

**Líneas:** 60, 98 de `PotentialOperations.java`

Múltiples métodos verifican `if (!(potential instanceof TablePotential))` y lanzan excepción. Sugiere que la jerarquía de tipos no refleja las capacidades reales.

**Refactorización sugerida:** Reemplazar con subtipos o interfaces que declaren la capacidad de forma estática.

---

## 6. Estado mutable compartido y problemas de concurrencia

### 6.1 Campos `volatile` insuficientes en `TablePotential`

**Archivo:** `TablePotential.java` (líneas 51–58)

```java
public volatile double[] values;
public volatile StrategyTree[] strategyTrees;
```

`volatile` protege la referencia al array, no sus elementos. Una escritura en `values[i]` desde otro hilo no está protegida.

**Refactorización sugerida:**
- Hacer los campos privados con acceso controlado.
- Aplicar semántica copy-on-write para los arrays si se necesita mutabilidad post-construcción.
- Documentar explícitamente el contrato de seguridad ante concurrencia.

### 6.2 Modelos de sincronización inconsistentes en `ProbNet`

**Archivo:** `ProbNet.java` (líneas 103, 152)

```java
private final NodeDepot nodeDepot;            // no thread-safe
// ...
private final Set<TablePotential> constantPotentials; // ConcurrentHashMap.newKeySet()
```

Mezcla de colecciones thread-safe y no thread-safe sin documentación del contrato de concurrencia.

**Refactorización sugerida:** Decidir y documentar una estrategia uniforme (todo concurrente, o todo protegido con `synchronized` a nivel de operación).

### 6.3 Lista de potenciales en `Node`

**Archivo:** `Node.java` (líneas 74–79)

```java
protected List<Potential> potentials;
// ...
potentials = Collections.synchronizedList(new ArrayList<>());
```

Las operaciones compuestas (leer + iterar) no son atómicas incluso con `synchronizedList`.

**Refactorización sugerida:** Usar `CopyOnWriteArrayList` o sincronización explícita a nivel de método.

---

## 7. Envidia de características (Feature Envy)

### 7.1 Métodos de `DANFactory` con envidia sobre `ProbNet`

**Archivo:** `DANFactory.java` (líneas 31–52)

Los métodos de factory acceden a muchos detalles internos de `ProbNet`:

```java
oneChanceDAN.setName(...);
oneChanceDAN.makeLinksExplicit(...);
oneChanceDAN.addLink(...);
oneChanceDAN.addNode(...);
// ...10+ llamadas más
```

**Refactorización sugerida:** Añadir métodos de dominio a `ProbNet` que encapsulen patrones de construcción frecuentes, p. ej. `probNet.addChanceAndUtilityNodes(...)`.

---

## 8. Grupos de datos (Data Clumps)

### 8.1 Trío Variable / NodeType / posición

En todos los métodos de factory y en las ediciones (`AddNodeEdit`), estos tres datos viajan siempre juntos.

**Refactorización sugerida:**
```java
class NodeSpecification {
    Variable variable;
    NodeType nodeType;
    Point2D.Double position;
}
```

### 8.2 Variable / PotentialRole / valores dobles

El patrón:
```java
TablePotential p = new TablePotential(List.of(var), PotentialRole.CONDITIONAL_PROBABILITY);
p.values = new double[]{ 0.86, 0.14 };
```
aparece más de 50 veces en las clases factory.

**Refactorización sugerida:** Fluent builder `PotentialBuilder` (ver sección 2.1).

---

## 9. Abstracciones ausentes

### 9.1 Iteración de coordenadas multidimensionales

**Patrón detectado en:**
- `DiscretePotentialOperations.multiply()` (líneas 130–146)
- `DiscretePotentialOperations.sum()` (líneas 297–306)
- `TablePotential` (lógica de offsets)

Bucles casi idénticos de seguimiento de coordenadas con arrays `int[]` de dimensiones, offsets e índices actuales.

**Abstracción ausente:** Clase `MultidimensionalIndex` (o `TableCoordinate`) que encapsule:
- Aritmética de offsets
- Incremento de coordenadas con acarreo
- Comprobación de límites

### 9.2 Manejador de `StrategyTree` en operaciones

La lógica de inicialización, seguimiento e integración de `StrategyTree` en operaciones de potenciales se repite en `multiply()` (líneas 149–206) y `sum()` (líneas 291–365).

**Abstracción ausente:** Clase `InterventionAwareOperationContext` que gestione el ciclo de vida de `StrategyTree` durante una operación.

---

## 10. Código muerto y deuda de documentación

### 10.1 Métodos comentados

**Archivo:** `BasicOperations.java` (líneas 123–142)

Métodos de evolución temporal comentados con `/* */`.

**Acción:** Eliminar o abrir ticket de trabajo.

### 10.2 TODO sin resolver

| Archivo | Línea | Texto |
|---|---|---|
| `Variable.java` | 28 | `TODO mantener la consistencia entre name y baseName cuando se cambian` |
| `UniformPotential.java` | varios | `// TODO Auto-generated method stub` |
| `StrategyTree.java` | cabecera | `// TODO Documentar la clase` |
| `GTablePotential.java` | 21 | `// TODO Remove this method` |

**Acción:** Resolver o convertir en issues rastreables.

---

## 11. API inconsistente

### 11.1 Variantes de `multiply` con tipos de parámetro distintos

**Archivo:** `PotentialOperations.java` (líneas 147–154)

```java
public static Potential multiply(List<? extends Potential> potentials)
public static Potential multiplyAndMarginalize(List<TablePotential> potentials, List<Variable> vars)
```

Un método acepta `List<? extends Potential>`, el otro `List<TablePotential>`. Sin convención clara sobre el orden de parámetros ni manejo uniforme de excepciones.

**Refactorización sugerida:** Unificar tipos de parámetro y documentar precondiciones.

### 11.2 Sobrecarga excesiva de constructores en `TablePotential`

**Archivo:** `TablePotential.java` (líneas 86–152)

Cuatro constructores públicos con diferencias sutiles y un constructor privado usado internamente.

**Refactorización sugerida:** Patrón Builder o métodos de factoría estáticos nombrados (`TablePotential.ofVariables(...)`, `TablePotential.withValues(...)`).

### 11.3 Cuatro constructores en `Variable` sin convención clara

**Archivo:** `Variable.java` (líneas 101–200)

No hay un "constructor canónico" evidente. El constructor de copia (línea 162) debe actualizarse manualmente cada vez que se añade un campo.

**Refactorización sugerida:**
- Separar `DiscreteVariableBuilder` y `ContinuousVariableBuilder`.
- Usar método de factoría `Variable.copyOf(Variable other)` para la copia defensiva.

---

## 12. Violaciones de SOLID

| Principio | Clase / Archivo | Descripción |
|---|---|---|
| **SRP** | `DiscretePotentialOperations` | 6 responsabilidades distintas en una clase |
| **SRP** | `ProbNet` | 5 responsabilidades distintas en una clase |
| **OCP** | `BasicOperations` | Cadena `instanceof` que requiere modificación para nuevos tipos |
| **OCP** | `PotentialOperations` | Verificaciones de tipo en tiempo de ejecución en lugar de polimorfismo |
| **LSP** | Jerarquía `Potential` | `TablePotential` expone arrays mutables que las subclases no respetan uniformemente |
| **DIP** | `DiscretePotentialOperations` | Acceso directo a `TablePotential.values[]` (dependencia de implementación concreta) |

---

## Priorización y esfuerzo estimado

| Prioridad | Oportunidad | Archivos afectados | Impacto |
|---|---|---|---|
| **Crítica** | Dios objeto `DiscretePotentialOperations` | 1 | Dividir en 4 clases enfocadas |
| **Crítica** | Duplicación en factories (`DANFactory`, `IDFactory`) | 2 | Reducir ~4.200 → ~1.500 líneas |
| **Crítica** | Dios objeto `ProbNet` | 1 | Separar en 3 clases enfocadas |
| **Alta** | Métodos largos `multiply()` / `sum()` | 1 | Extraer 8 métodos helper |
| **Alta** | Inconsistencia de concurrencia (`TablePotential`, `ProbNet`, `Node`) | 3 | Estrategia de sync uniforme |
| **Alta** | Cadenas `instanceof` en `BasicOperations` | 2+ | Patrón Strategy / polimorfismo |
| **Alta** | Obsesión por primitivos (`String` como nombres, arrays públicos) | 5+ | Value objects y encapsulación |
| **Media** | Abstracción ausente `MultidimensionalIndex` | 2 | Encapsular aritmética de índices |
| **Media** | API inconsistente (`multiply`, constructores) | 3+ | Unificar contratos |
| **Media** | Código muerto y TODO sin resolver | 5+ | Limpiar y abrir issues |
| **Baja** | Data clumps (`NodeSpecification`, `PotentialBuilder`) | 3+ | Crear objetos contenedores |
| **Baja** | Feature envy en `DANFactory` | 1+ | Acercar métodos a los datos |
