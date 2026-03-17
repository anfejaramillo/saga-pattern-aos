package ajaramillo.net.saga;

import java.util.UUID;

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

    //Constructor
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
            System.out.println("Error in initialization of saga data context.");
        }
    }

    //Constructor
    public SagaOrchestator(SagaContext sagaContext, Long timeToReviewTransactions){
        this(sagaContext);
        this.timeToReviewTransactions = timeToReviewTransactions;
    }

    public SagaContext getSagaContext() {
        return this.sagaContext;

    }

    public Integer getNumTransactions() {
        return numTransactions;
    }

    public UUID getSagaTransactionId() {
        return sagaTransactionId;
    }

    public void runSaga() throws Exception {
        if (canRunSagaInstance) {
            System.out.println("Running saga with id: " + this.sagaTransactionId);
            initialSagaTransaction.runSagaTransactions(timeToReviewTransactions);
            System.out.println("Saga with id: " + this.sagaTransactionId + " finished");
        } else {
            throw new Exception("Saga can not run, some initial configuration have been failed.");
        }
    }

    public void addSagaTransaction(Transaction trans) throws NullPointerException {
        if (trans == null) {
            throw new NullPointerException("Transaction instance can not be null.");
        }
        trans.setTransactionContext(this.sagaContext);
        this.initialSagaTransaction.addTransaction(trans);
        this.sagaContext.addTransaction(trans);
        this.numTransactions = this.numTransactions + 1;
    }

    public void removeLastSagaTransaction() throws Exception {
        if (this.initialSagaTransaction.transaction == null
                && this.initialSagaTransaction.innerSagaTransaction == null) {
            throw new Exception("The saga orchestrator have not transactions yet.");
        } else if (this.initialSagaTransaction.transaction != null // CUando es la primera transaccion borrar solo el
                                                                   // objeto transaction
                && this.initialSagaTransaction.innerSagaTransaction == null) {
            System.out.println("Trans deleted: " + this.initialSagaTransaction.transaction.getDescription());
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

    public Long getPeriodOfLogs() {
        return this.timeToReviewTransactions;
    }

    public void setPeriodOfLogs(Long timeToReviewTransactions) {
        this.timeToReviewTransactions = timeToReviewTransactions;
    }

}
