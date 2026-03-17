package net.ajaramillo.artifacts.saga;

/**
 * Status values for the saga flow manager.
 */
public enum FlowManagerSagaTransactionStatus {
    READY, //Manager ready to run transactions
    RUNNING,  //Manager in work with transactions synchronous
    FINISHED_CORRECTLY, //Correctly finished saga
    FINISHED_WITH_ERRORS_CONTROLLATED, // The saga is finished, the errors were handled by the same transactions (they may be rollbacks or additional transactions)
    INCONSISTENT, //Saga in inconsistent state
}
