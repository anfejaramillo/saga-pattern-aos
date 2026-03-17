package net.ajaramillo.artifacts.saga;

public enum TransactionStatus {
    // Created Transaction
    CREATED, 
    // Currently added to the Saga orchestrator
    FETCHED, 
    // In execution
    RUNING, 
    // Transaction Failed
    FAILED, 
    // Transaction Finished without errors
    SUCCESSFUL, 
    // Transaction finished with errors (correct execution)
    SUCCESFUL_WITH_ERRORS
}
