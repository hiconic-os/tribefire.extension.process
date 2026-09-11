# Process engine

A state machine engine for long running, persisted processes: the graph is configuration, the work is your Java code, and the engine moves a process through the graph.

> This document describes the **RX (Hiconic reflex) implementation**, the artifacts named `process-rx-*` together with `process-configuration-model`. The group also holds the older cortex implementation - `process-deployment-model`, `process-module`, `process-processing` - which works differently. The same text stands as the Javadoc of `ProcessDefinition`, the entry point of the configuration model.

## What a process is

A process is a (persisted) entity. It is an instance of a sub type of `ProcessItem`, and it carries the control properties of the state machine: `state`, `previousState`, `nextState`, `activity` and `transitionPhase`. The engine moves that entity through its graph and runs application code on the way.

Two properties follow from that:

- a process outlives the application, so it may wait in one state for months
- it does not matter which instance of the application drives a process, so the engine also works in a cluster

The engine writes a `ProcessLogEntry` for what it does - every state change, every processor it called, every condition it evaluated - which gives a process an audit trail.

## How a process finds its definition

The process definition for a given process is configured with `ManageProcessWith` metadata on its type, usually written as the annotation `@ManageProcessWith("<processDefinitionId>")`. A sub type inherits it.

That metadata names a `processDefinitionId`, and the engine looks up the definition with that id in `ProcessDefinitionsConfiguration`. Note that the `name` of a definition is a display name, never an identifier.

## Nodes and edges

Every state of the process is a `Node`, and every allowed state change is an `Edge` in `Node.edges` of the node it leaves. **Only a declared edge permits a state change.**

A process starts in the **root node**, the node with state `null`, and it ends when it reaches a **drain node**, a node without edges.

## How the engine routes

A state change happens in two passes over the edges of the current node:

1. Every edge that has a `condition` is evaluated, in list order. The first matching condition wins.
2. If no condition matched, the one edge without a condition is taken.

So the list order is only relevant among conditions, and an edge without a condition acts as the default of its node, wherever it stands in the list.

A node needs no default: conditions may well cover every case. But if none of them matched and the node has no edge without a condition, or has more than one, the engine cannot decide. It then halts the process and logs `UNDETERMINED_NEXT_NODE`.

Both passes are skipped when a transition processor demanded a state with `TransitionProcessorContext.continueWithState(String)`. An edge to that state must still exist.

A `Condition` is either a reference to a condition processor or an expression. Today the expression holds `"true"` or `"false"`, and it wins over the processor, so an edge can be disabled or forced temporarily without losing the processor it normally uses. An edge with `"false"` is never taken by routing, while a transition processor may still take it. Note that `"true"` is not the same as an edge without a condition: it takes part in the first pass and therefore shadows the conditions after it.

## What runs on a state change

After the state changed, the engine runs the transition processors of that transition in this order:

1. `onTransit` of the definition
2. `onLeft` of the left node
3. `onTransit` of the taken edge
4. `onEntered` of the entered node

All of them run when the process is **already in the entered state**, `onLeft` included. The engine remembers how far it got through that list, so a long chain of processors survives a restart of the application. A processor may influence the routing with `continueWithState`, and if several processors of one transition do so, the last call wins.

## Waiting, ending, failing

- A node with a `decoupledInteraction` stops the process. Work then happens outside the engine, and that party hands the process back with `ResumeProcess` or `ResumeProcessToState`. A `gracePeriod` turns the waiting into a deadline, after which the engine continues the process by itself - over the node's `overdueNode` if one is declared, else by normal routing.
- A drain node ends the process.
- A failing transition or condition processor halts the process, runs `onError` of the node and of the definition, and leaves it for an operator, who continues it with `RecoverProcess`.

A deadline is noticed by a search that runs per configured access, which `ProcessDefinitionsConfiguration.monitoredAccessIds` describes. The same search finds a process that stopped making progress, e.g. because the application was killed while it ran. In an access that is not listed there, a process runs normally, but nobody looks after it once it stops.

## The activity of a process

`ProcessItem.activity` says who is responsible for a process right now. It has four values:

- `processing` - the engine is responsible, and it keeps the progress date of the process fresh while it works
- `waiting` - a decoupled interaction is responsible, and the process continues when that party resumes it
- `halted` - something failed, and an operator has to look at it
- `ended` - the process reached a drain node

## Writing the experts

The work of a process is your code. Two interfaces in `process-api`:

```java
public interface TransitionProcessor<T extends GenericEntity> {
    void process(TransitionProcessorContext<T> context);
}

public interface ConditionProcessor<S extends GenericEntity> {
    boolean matches(ConditionProcessorContext<S> context);
}
```

A `TransitionProcessorContext` hands over the process entity, a user session and a system session, the state the process left and the one it entered, and `continueWithState(String)`, which is the only way a processor influences the routing. Both contexts have `setError(Reason)`, which halts the process as an exception would.

The engine resolves an expert by a **string id**, never by class. An application registers it on `ProcessRxContract`, usually in the `onDeploy` of its module:

```java
process.registerTransitionProcessor("export-action", ExportAction::new);
process.registerConditionProcessor("conversion-required", ConversionRequiredCondition::new);
```

Nothing compares the two sides, so the graph and the registration must agree on the string. A shared constant is the safer way to write it, as a wrong id shows up only when a process takes that transition.

## Building a definition

`ProcessDefinitionEditor` builds a graph without a session. It is the RX counterpart of drawing the graph in a tool:

```java
ProcessDefinitionEditor editor = ProcessDefinitionEditor.create("export-resource", "Export Resource Process");

editor.rootEdge("export", "root-export");
editor.conditionedEdge("export", "convert", "export-convert", "conversion-required");
editor.edge("export", "done", "export-done");
editor.onEnter("export", "export-action");

definitions.getDefinitions().add(editor.definition());
definitions.getMonitoredAccessIds().add("access.main");
```

Two details of the editor are worth knowing:

- An edge is **appended** to the edges of the node it leaves. `prependEdge` and `prependConditionedEdge` put it in front instead, which is how an extension gives its own condition priority over a graph that another module built.
- `conditionedEdge` also takes a `Condition`, so `Condition.expression(false)` declares an edge that routing never takes while a transition processor still may. That replaces a condition processor which always returns false.

An extension changes an existing graph through `ProcessDefinitionEditor.edit(definition)`, after it looked the definition up with `definitions.findDefinition(id)`.

The whole configuration is checked once at startup, and the application stops if it finds one of these:

- a missing or duplicate `processDefinitionId`
- an edge without a name, or with a name that another edge of the same definition uses
- an edge that leads to a state which is no node of its definition
- a node with more than one edge that has no condition
- a condition with neither a processor nor a supported expression

## Driving a process

An application creates the process like any other entity, and then hands it to the engine with a `StartProcess` request:

```java
ExportResourceProcess process = session.create(ExportResourceProcess.T);
process.setResource(resource);
session.commit();

StartProcess start = StartProcess.T.create();
start.item(process);
start.eval(session).get();
```

The process must be untouched for that: `state` and `activity` are still null. `StartProcessToState` starts it into a named state instead of letting the root node route.

| Purpose | Requests |
|---|---|
| flow | `StartProcess`, `StartProcessToState`, `ResumeProcess`, `ResumeProcessToState`, `RecoverProcess` |
| waiting for a result | `WaitForProcess`, `WaitForProcesses` |
| operations | `ReviveProcesses`, `ClearProcessLog`, `ClearProcessLogs` |
| analysis | `GetProcessList`, `GetProcessLog`, `GetProcessLogByScalarFilters` |

`HandleProcess` also belongs to that model, but an application never sends it: the engine sends it to itself, see below.

Failures come back as reasons, not as exceptions. A reason either answers the request, or, when the engine meets it while it drives the process, halts that process and lands in its log.

| Reason | When it happens |
|---|---|
| `ProcessNotFound` | no process of the given type and id exists in that access |
| `ProcessDefinitionNotFound` | the process type carries no `ManageProcessWith` metadata, or that metadata names an id which no configured definition has |
| `UnexpectedProcessActivity` | the request does not fit the situation of the process: starting one that already runs, resuming one that is not waiting, recovering one that is not halted |
| `UnexpectedProcessState` | starting a process that already has a state; a node where routing cannot decide, because no condition matched and the node has no edge without a condition or has several; a demanded next state on a drain node; a process whose state is no node of its definition any more |
| `IllegalTransition` | `StartProcessToState` or `ResumeProcessToState` names a state that does not exist, or one that has no edge from where the process stands. It carries the `NodeNotFound` or `EdgeNotFound` that says which of the two it is |
| `NodeNotFound` | a state is no node of the definition |
| `EdgeNotFound` | a state change has no edge: a transition processor demanded a state that cannot be reached from here, or a node became overdue and has no edge to its overdue node |
| `InvalidConditionExpression` | a condition holds an expression other than `"true"` or `"false"` |
| `CouldNotAcquireProcessLock` | another instance, or another request, works on the same process at that moment |

## Timing of the search

`ProcessManagerConfiguration` sets how often the search for processes that need attention runs, and how long a process may stay in `processing` without progress before it counts as unattended. Both default to one minute.

A process that runs a long transition processor is not unattended: the engine refreshes its progress date every 15 seconds while a processor works. A threshold below that refresh interval lets the search chase healthy processes, which does no damage, as the lock rejects the second attempt, but it fills the log.
## Under the hood

The engine does not run a process in one go. Whenever it took a process one step further - a state change, or one transition processor that ran - it commits that step and sends itself a message which says: continue this process. Reading such a message is what triggers the next step.

Two things follow from that:

- a request such as `StartProcess` or `ResumeProcess` returns once the process is on its way, not once it reached its end state
- the queue is shared by all instances of the application, so the next step of a process may be taken by any of them, and a lock on the process keeps two instances from working on it at the same time

After an interruption - a restart, a crash - the engine continues from the step it recorded on the process. The one case it does not repeat is a transition processor that was interrupted while it ran: such a process halts, because the engine cannot know whether that processor is idempotent.

## Where to look

| Artifact | What it holds |
|---|---|
| `process-configuration-model` | the graph: `ProcessDefinition`, `Node`, `Edge`, `Condition`, and the configuration types |
| `process-data-model` | `ProcessItem` and the process log |
| `process-api` | `TransitionProcessor` and `ConditionProcessor`, the interfaces your code implements |
| `process-api-model` | the requests: `StartProcess`, `ResumeProcess`, `RecoverProcess`, `GetProcessLog`, ... |
| `process-rx-module-api` | `ProcessRxContract` and `ProcessDefinitionEditor`, which an application uses to register processors and build graphs |
| `process-rx-module`, `process-rx-processing` | the engine itself |
| `process-rx-module-test` | the tests, and an example application built from an in-memory access, an in-memory queue and the worker |
