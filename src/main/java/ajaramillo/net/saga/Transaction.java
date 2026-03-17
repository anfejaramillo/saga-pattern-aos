package ajaramillo.net.saga;

import java.util.UUID;

@TransactionPolitics
public abstract class Transaction {
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

    // Getter publico no sobreescribible
    public final SagaContext getTransactionContext() {
        return transactionContext;
    }

    final void setTransactionContext(SagaContext sagaContext) {
        this.transactionContext = sagaContext;
    }

    // Get for transaction ID
    public UUID getTransactionId() {
        return this.transactionId;
    }

    // Continue with run transaction even when the before process failed (default:
    // false, this means that the transaction do not runs when preprocess have any
    // error)
    protected Boolean runTransactionWithErrorsInPreProcessForRunTransaction() {
        return false;
    }

    // Processing before run this transaction, this can be overide in derivated
    // classes
    protected void preProcessForRunTransaction() throws Exception {
        this.setPreProcessForRunTransactionStatus(PreProcessForRunTransactionStatus.SUCCESFUL);
    }

    // Transaction to execute
    public void task() throws Exception {
        throw new Exception("Task method not implemented");
    }

    // Function that execute after rollback function
    public void catchErrors() throws Exception {
        throw new Exception("Catch errors not implemented");
    }

    // Function of roll back, it is executed when any exception raises in
    // transaction process
    public void rollback() throws Exception {
        throw new Exception("Rollback not implemented");
    }

    // Short description of transaction type
    public String getDescription() {
        return "Transaction description Not implemented";
    }

    // Get for status of transaction
    public final TransactionStatus getTransactionStatus() {
        return this.status;
    }

    // Set for transaction status
    void setTransactionStatus(TransactionStatus ts) {
        this.status = ts;
    }

    // get for preprocess status
    public final PreProcessForRunTransactionStatus getPreProcessForRunTransactionStatus() {
        return this.preProcessStatus;
    }

    // set for pre process status
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

    public final TransactionPolitics getTransactionPolitics() {
        TransactionPolitics implementationAnnotation = this.getClass().getAnnotation(TransactionPolitics.class);
        if (implementationAnnotation != null) {
            System.out.println(implementationAnnotation);
            return implementationAnnotation;
        } else {
            TransactionPolitics impl = super.getClass().getAnnotation(TransactionPolitics.class);
            System.out.println(impl);
            return impl;
        }
    }
}
