package net.ajaramillo.artifacts.saga;

import java.util.UUID;

/**
 * Base class for a saga transaction. Implementations should override
 * {@link #task()}, {@link #rollback()}, {@link #catchErrors()}, and
 * {@link #getDescription()} to define behavior.
 */
@TransactionPolitics
public abstract class STransaction {
    // id_transaction
    private UUID transactionId = UUID.randomUUID();
    // Estado de este objeto transaccion
    private TransactionStatus status = TransactionStatus.CREATED;
    // estado del preprocess for run transaction
    private PreProcessForRunTransactionStatus preProcessStatus = PreProcessForRunTransactionStatus.READY;
    // Contexto de datos de la transaccion
    // Es una unica instancia por orquestador, es decir, es un mismo objeto de
    // contexto para todos
    // los objetos de transaction que pertenezcan a un mismo orquestador
    private SagaContext transactionContext;

    /**
     * Returns the shared saga context.
     * @return saga context
     */
    public final SagaContext getTransactionContext() {
        return transactionContext;
    }

    final void setTransactionContext(SagaContext sagaContext) {
        this.transactionContext = sagaContext;
    }

    /**
     * Returns the unique transaction id.
     * @return transaction id
     */
    public UUID getTransactionId() {
        return this.transactionId;
    }

    /**
     * Whether this transaction should run even if the pre-process fails.
     * @return true to run even with pre-process errors
     */
    protected Boolean runTransactionWithErrorsInPreProcessForRunTransaction() {
        return false;
    }

    /**
     * Pre-processing hook executed before the transaction task runs.
     * @throws Exception if pre-process fails
     */
    protected void preProcessForRunTransaction() throws Exception {
        this.setPreProcessForRunTransactionStatus(PreProcessForRunTransactionStatus.SUCCESFUL);
    }

    /**
     * Executes the transaction logic.
     * @throws Exception if the task fails
     */
    public void task() throws Exception {
        throw new Exception("Task method not implemented");
    }

    /**
     * Called after rollback to handle or compensate errors.
     * @throws Exception if error handling fails
     */
    public void catchErrors() throws Exception {
        throw new Exception("Catch errors not implemented");
    }

    /**
     * Rollback logic for the transaction.
     * @throws Exception if rollback fails
     */
    public void rollback() throws Exception {
        throw new Exception("Rollback not implemented");
    }

    /**
     * Returns a short description of the transaction.
     * @return description string
     */
    public String getDescription() {
        return "Transaction description Not implemented";
    }

    /**
     * Returns the current transaction status.
     * @return status
     */
    public final TransactionStatus getTransactionStatus() {
        return this.status;
    }

    /**
     * Sets the current transaction status.
     * @param ts new status
     */
    void setTransactionStatus(TransactionStatus ts) {
        this.status = ts;
    }

    /**
     * Returns the pre-process status.
     * @return pre-process status
     */
    public final PreProcessForRunTransactionStatus getPreProcessForRunTransactionStatus() {
        return this.preProcessStatus;
    }

    /**
     * Sets the pre-process status.
     * @param pre new pre-process status
     */
    protected void setPreProcessForRunTransactionStatus(PreProcessForRunTransactionStatus pre) {
        this.preProcessStatus = pre;
    }

    @Override
    public final String toString() {
        return this.getClass().getName() + "[transactionId=" + transactionId + ", status=" + status
                + ", preProcessStatus="
                + preProcessStatus
                + ", transactionContext=" + transactionContext + "]";
    }

    /**
     * Returns the {@link TransactionPolitics} annotation applied to this class.
     * @return transaction politics annotation
     */
    public final TransactionPolitics getTransactionPolitics() {
        TransactionPolitics implementationAnnotation = this.getClass().getAnnotation(TransactionPolitics.class);
        if (implementationAnnotation != null) {
            return implementationAnnotation;
        } else {
            TransactionPolitics impl = super.getClass().getAnnotation(TransactionPolitics.class);
            return impl;
        }
    }
}
