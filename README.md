Saga Pattern AOS (Atomic, Orchestrated, Synchronous)

This library provides a simple, synchronous Saga pattern implementation for Java. A saga is executed by an orchestrator, runs transactions in order, and triggers rollbacks on failure. Each transaction runs in its own thread with a configurable timeout, while the orchestrator polls status and drives the overall flow.

**Quick Start**
1. Create a context that extends `SagaContext`.
2. Implement your transactions by extending `STransaction`.
3. Register transactions in a `SagaOrchestator`.
4. Run the saga with `runSaga()`.

```java
package demo;

import net.ajaramillo.artifacts.saga.*;

public class Demo {
    public static class OrderContext extends SagaContext {
        public String orderId;
        public String paymentId;
    }

    @TransactionPolitics(transactionTimeOut = 30_000L)
    public static class ReserveInventory extends STransaction {
        @Override
        public void task() throws Exception {
            OrderContext ctx = (OrderContext) getTransactionContext();
            // reserve inventory
        }

        @Override
        public void rollback() throws Exception {
            // release inventory
        }

        @Override
        public void catchErrors() throws Exception {
            // optional compensation or alerting
        }

        @Override
        public String getDescription() {
            return "Reserve inventory";
        }
    }

    public static class ChargePayment extends STransaction {
        @Override
        public void task() throws Exception {
            OrderContext ctx = (OrderContext) getTransactionContext();
            // charge payment
        }

        @Override
        public void rollback() throws Exception {
            // refund payment
        }

        @Override
        public void catchErrors() throws Exception {
            // handle errors during rollback or execution
        }

        @Override
        public String getDescription() {
            return "Charge payment";
        }
    }

    public static void main(String[] args) throws Exception {
        OrderContext ctx = new OrderContext();
        ctx.orderId = "ORD-123";

        SagaOrchestator orchestrator = new SagaOrchestator(ctx, 500L);
        orchestrator.addSagaTransaction(new ReserveInventory());
        orchestrator.addSagaTransaction(new ChargePayment());

        orchestrator.runSaga();
    }
}
```

**Core Concepts**

**SagaContext**
- File: `SagaContext.java`
- Purpose: Shared data container for all transactions in a saga.
- Usage:
1. Extend `SagaContext` with fields for shared data.
2. Read/update those fields inside your transactions via `getTransactionContext()`.
3. The orchestrator sets the context automatically when transactions are added.

Important details:
- `SagaContext` keeps an internal list of registered transactions.
- `toString()` uses reflection to show all declared fields on your context class.

**SagaOrchestator** (Orchestrator entry point)
- File: `SagaOrchestator.java`
- Purpose: Registers transactions, owns the `SagaContext`, and executes the saga.
- Key methods:
1. `addSagaTransaction(STransaction trans)`: Adds a transaction in order.
2. `runSaga()`: Runs all transactions, triggers rollbacks on failure.
3. `removeLastSagaTransaction()`: Removes the last registered transaction.
4. `setPeriodOfLogs(Long ms)`: Controls polling interval for transaction threads.

Execution model:
- Each transaction runs in its own thread.
- The orchestrator polls status every `timeToReviewTransactions` milliseconds.
- If a transaction fails, rollbacks execute in reverse order on already completed transactions.

**STransaction** (Your transaction base class)
- File: `STransaction.java`
- Purpose: Base class you extend to define saga steps.
- Methods you typically override:
1. `task()`: Required. The main work of the transaction.
2. `rollback()`: Required. Compensates the transaction when later steps fail.
3. `catchErrors()`: Optional. Called after rollback to handle errors/compensation.
4. `getDescription()`: Required. Human-readable description for logs.
5. `preProcessForRunTransaction()`: Optional. Pre-checks before `task()`.
6. `runTransactionWithErrorsInPreProcessForRunTransaction()`: Optional override to allow `task()` even if pre-process fails.

Transaction flow:
1. Pre-process runs (`preProcessForRunTransaction()`).
2. If pre-process succeeds (or you allow execution on failure), `task()` runs.
3. If task fails or times out, status is set to `FAILED` and rollbacks begin.
4. Rollbacks run on all previously successful transactions (reverse order).
5. `catchErrors()` runs after each rollback, allowing error handling.

**TransactionPolitics** (Annotation)
- File: `TransactionPolitics.java`
- Purpose: Defines per-transaction runtime policies.
- Current policy:
1. `transactionTimeOut`: Milliseconds before the transaction thread is interrupted.

Usage:
```java
@TransactionPolitics(transactionTimeOut = 60000L)
public class MyTransaction extends STransaction {
    // ...
}
```

**TransactionStatus** (Transaction lifecycle)
- File: `TransactionStatus.java`
- Purpose: Tracks each transaction state.
- Values:
1. `CREATED`: Instantiated but not registered.
2. `FETCHED`: Added to the orchestrator.
3. `RUNING`: Task is running.
4. `FAILED`: Task or rollback failed.
5. `SUCCESSFUL`: Task completed without errors.
6. `SUCCESFUL_WITH_ERRORS`: Task completed but with controlled errors.

**FlowManagerSagaTransactionStatus** (Saga lifecycle)
- File: `FlowManagerSagaTransactionStatus.java`
- Purpose: Tracks overall saga flow.
- Values:
1. `READY`: Flow manager created but not started.
2. `RUNNING`: Saga executing.
3. `FINISHED_CORRECTLY`: All transactions succeeded.
4. `FINISHED_WITH_ERRORS_CONTROLLATED`: Some errors handled/compensated.
5. `INCONSISTENT`: Rollbacks failed or unexpected error occurred.

**How the Flow Manager Works (Internal)**
- The orchestrator creates a `FlowManagerSagaTransaction` internally.
- The flow manager:
1. Runs pre-process for each transaction.
2. Executes transaction `task()` in its own thread.
3. Monitors thread status until completion or timeout.
4. On failure, executes rollbacks in reverse order.
5. Sets the final saga status.

**Common Usage Pattern**
1. Build a shared `SagaContext` with needed data.
2. Implement transactions and annotate with `@TransactionPolitics` if needed.
3. Register transactions in order with `SagaOrchestator`.
4. Call `runSaga()` and check logs or transaction statuses via context list.

**Maven Coordinates**
You can use the project coordinates from `pom.xml`:

```
groupId: net.ajaramillo.artifacts
artifactId: saga-pattern-aos
```
