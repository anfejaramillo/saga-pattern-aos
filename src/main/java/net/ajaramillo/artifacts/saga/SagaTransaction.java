package net.ajaramillo.artifacts.saga;

import lombok.extern.slf4j.Slf4j;

/**
 * Internal linked structure that stores the ordered transactions of a saga.
 * Each node contains a transaction and a reference to the next node.
 */
@Slf4j
class SagaTransaction {
    SagaTransaction fatherSagaTransaction;
    SagaTransaction innerSagaTransaction;
    STransaction transaction;

    /**
     * Creates an empty saga transaction node.
     */
    SagaTransaction() {
    }

    /**
     * Runs all transactions starting from this node.
     * This should be called only on the root node.
     * @param timeToReviewTransactions interval (ms) between transaction status checks
     * @throws Exception if execution fails or if called on a non-root node
     * @throws NullPointerException if the transaction is null
     */
    public void runSagaTransactions(Long timeToReviewTransactions) throws Exception, NullPointerException {
        // Ejecucion de transaccion
        if (transaction == null) {
            throw new NullPointerException("Error: Transaction can not be null");
        }
        if(fatherSagaTransaction != null){
            throw new Exception("Error: the function runTransactions() only can be call by the initial sagaTransaction object.");
        }
        FlowManagerSagaTransaction flowManagerSagaTransaction = new FlowManagerSagaTransaction(this, timeToReviewTransactions);
        flowManagerSagaTransaction.runSagaTransactions();
    }

    /**
     * Adds a transaction to the end of the chain.
     * @param transactionToAdd transaction to add
     */
    public void addTransaction(STransaction transactionToAdd) {
        if (this.transaction != null) { // false: significa que este objeto no tiene trans y por eso la añadimos
            if (this.innerSagaTransaction == null) {
                // Nueva instancia
                this.innerSagaTransaction = new SagaTransaction();
                // Añadir referencia del sagatrans padre
                this.innerSagaTransaction.fatherSagaTransaction = this;
            }
            // Añadimos la transaccion a la saga interna
            this.innerSagaTransaction.addTransaction(transactionToAdd);
        } else {
            // añadimos la transaccion
            this.transaction = transactionToAdd;
            // cambiamos el estatus a fetch
            this.transaction.setTransactionStatus(TransactionStatus.FETCHED);
            log.info("Transaction added: " + this.transaction.getDescription());
        }
    }

    /**
     * Removes the last transaction from the chain.
     * @throws Exception if removal fails
     */
    public void removeLastTransaction() throws Exception {
        if (this.innerSagaTransaction != null
                && this.innerSagaTransaction.innerSagaTransaction == null
                && this.innerSagaTransaction.transaction != null) {
            log.info("Transaction deleted: " + this.innerSagaTransaction.transaction.getDescription());
            this.innerSagaTransaction.transaction.setTransactionContext(null);
            this.innerSagaTransaction = null;
        } else {
            this.innerSagaTransaction.removeLastTransaction();
        }
    }
}
