package net.ajaramillo.artifacts.saga;

class SagaTransaction {
    SagaTransaction fatherSagaTransaction;
    SagaTransaction innerSagaTransaction;
    Transaction transaction;

    SagaTransaction() {
    }

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

    public void addTransaction(Transaction transactionToAdd) {
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
            System.out.println("Trans added: " + this.transaction.getDescription());
        }
    }

    public void removeLastTransaction() throws Exception {
        if (this.innerSagaTransaction != null
                && this.innerSagaTransaction.innerSagaTransaction == null
                && this.innerSagaTransaction.transaction != null) {
            System.out.println("Trans deleted: " + this.innerSagaTransaction.transaction.getDescription());
            this.innerSagaTransaction.transaction.setTransactionContext(null);
            this.innerSagaTransaction = null;
        } else {
            this.innerSagaTransaction.removeLastTransaction();
        }
    }
}