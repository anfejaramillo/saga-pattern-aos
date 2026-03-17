package net.ajaramillo.artifacts.saga;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates a saga by registering transactions and executing them in order.
 * This class is the main entry point for running a saga and tracking its
 * overall context and state.
 */
@Slf4j
public final class SagaOrchestator {
    // Identificador UNICO del conjunto de transacciones
    private UUID sagaTransactionId;
    // Wrapper de la transaccion inicial
    private SagaTransaction initialSagaTransaction;
    // Numero de transacciones registrada en el orquestador
    private Integer numTransactions = 0;
    // Contexto de datos de todo el orquestador
    private SagaContext sagaContext;
    // Puede correr la saga?
    private Boolean canRunSagaInstance;
    // time to review any transaction in saga, through log or console the state of the transactions in milliseconds 
    private Long timeToReviewTransactions = 500L;

    /**
     * Creates a new orchestrator with the provided saga context.
     * @param sagaContext shared context for all transactions in the saga
     */
    public SagaOrchestator(SagaContext sagaContext) {
        this.initialSagaTransaction = new SagaTransaction();
        this.sagaTransactionId = UUID.randomUUID();
        try {
            if(sagaContext == null){
                throw new Exception();
            }
            this.sagaContext = sagaContext;
            this.sagaContext.setSagaOrchestrator(this);
            canRunSagaInstance = true;
        } catch (Exception e) {
            canRunSagaInstance = false;
            log.info("Error in initialization of saga data context.");
        }
    }

    /**
     * Creates a new orchestrator with a custom log review period.
     * @param sagaContext shared context for all transactions in the saga
     * @param timeToReviewTransactions interval (ms) between transaction status checks
     */
    public SagaOrchestator(SagaContext sagaContext, Long timeToReviewTransactions){
        this(sagaContext);
        this.timeToReviewTransactions = timeToReviewTransactions;
    }

    /**
     * Returns the shared saga context.
     * @return saga context
     */
    public SagaContext getSagaContext() {
        return this.sagaContext;

    }

    /**
     * Returns the number of registered transactions.
     * @return transaction count
     */
    public Integer getNumTransactions() {
        return numTransactions;
    }

    /**
     * Returns the unique saga identifier.
     * @return saga transaction id
     */
    public UUID getSagaTransactionId() {
        return sagaTransactionId;
    }

    /**
     * Executes the saga starting from the first transaction.
     * @throws Exception if the saga cannot be run or initialization failed
     */
    public void runSaga() throws Exception {
        if (canRunSagaInstance) {
            log.info("Running saga with id: " + this.sagaTransactionId);
            initialSagaTransaction.runSagaTransactions(timeToReviewTransactions);
            log.info("Saga with id: " + this.sagaTransactionId + " finished");
        } else {
            throw new Exception("Saga can not run, some initial configuration have been failed.");
        }
    }

    /**
     * Adds a transaction to the saga and registers it in the context.
     * @param trans transaction to add
     * @throws NullPointerException if the transaction is null
     */
    public void addSagaTransaction(Transaction trans) throws NullPointerException {
        if (trans == null) {
            throw new NullPointerException("Transaction instance can not be null.");
        }
        trans.setTransactionContext(this.sagaContext);
        this.initialSagaTransaction.addTransaction(trans);
        this.sagaContext.addTransaction(trans);
        this.numTransactions = this.numTransactions + 1;
    }

    /**
     * Removes the last transaction from the saga.
     * @throws Exception if there are no transactions to remove
     */
    public void removeLastSagaTransaction() throws Exception {
        if (this.initialSagaTransaction.transaction == null
                && this.initialSagaTransaction.innerSagaTransaction == null) {
            throw new Exception("The saga orchestrator have not transactions yet.");
        } else if (this.initialSagaTransaction.transaction != null // CUando es la primera transaccion borrar solo el
                                                                   // objeto transaction
                && this.initialSagaTransaction.innerSagaTransaction == null) {
            log.info("Transaction deleted: " + this.initialSagaTransaction.transaction.getDescription());
            this.sagaContext.removeTransaction(this.initialSagaTransaction.transaction);
            this.initialSagaTransaction.transaction.setTransactionContext(null);
            this.initialSagaTransaction.transaction = null;
            this.numTransactions = this.numTransactions - 1;
        } else {// Cuanto NO ES la primera se delega la responsabilidad de eliminar
            this.initialSagaTransaction.removeLastTransaction();
            this.sagaContext.removeTransaction(
                    this.sagaContext.getTransactions().get(
                            this.sagaContext.getTransactions().size()));
            this.numTransactions = this.numTransactions - 1;
        }
    }

    /**
     * Returns the interval (ms) between transaction status checks.
     * @return period in milliseconds
     */
    public Long getPeriodOfLogs() {
        return this.timeToReviewTransactions;
    }

    /**
     * Sets the interval (ms) between transaction status checks.
     * @param timeToReviewTransactions interval in milliseconds
     */
    public void setPeriodOfLogs(Long timeToReviewTransactions) {
        this.timeToReviewTransactions = timeToReviewTransactions;
    }

}
