package net.ajaramillo.artifacts.saga;

/**
 * Status values for transaction execution threads.
 */
public enum TransactionThreadStatus {
    THREAD_CREATED,
    THREAD_RUNNING,
    THREAD_FINISHED,
    THREAD_FINISHED_WITH_ERRORS
}
