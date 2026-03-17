package net.ajaramillo.artifacts.saga;

/**
 * Status values for the transaction pre-process step.
 */
public enum PreProcessForRunTransactionStatus {
    READY,
    RUNNING,
    SUCCESFUL,
    FAILED
}
