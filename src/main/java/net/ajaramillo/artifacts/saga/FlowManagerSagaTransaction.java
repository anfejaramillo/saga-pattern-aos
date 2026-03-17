package net.ajaramillo.artifacts.saga;

import lombok.extern.slf4j.Slf4j;

/**
 * Executes the saga flow, coordinating pre-process, execution, and rollback
 * logic for each transaction in order.
 */
@Slf4j
class FlowManagerSagaTransaction {
    private SagaTransaction initialSagaTransaction;
    private FlowManagerSagaTransactionStatus flowManagerSagaTransactionStatus = FlowManagerSagaTransactionStatus.READY;
    private Long timeToReviewTransactions = 500L;
    private Boolean exceptionInRollback = false;
    private Long transactionElapsedTime = 0L;

    /**
     * Returns the current flow status.
     * 
     * @return flow status
     */
    public FlowManagerSagaTransactionStatus getFlowManagerSagaTransactionStatus() {
        return flowManagerSagaTransactionStatus;
    }

    /**
     * Creates a flow manager for the provided saga.
     * 
     * @param saga root saga transaction
     */
    public FlowManagerSagaTransaction(SagaTransaction saga) {
        this.initialSagaTransaction = saga;
    }

    /**
     * Creates a flow manager with a custom review interval.
     * 
     * @param saga                     root saga transaction
     * @param timeToReviewTransactions interval (ms) between transaction status
     *                                 checks
     */
    public FlowManagerSagaTransaction(SagaTransaction saga, Long timeToReviewTransactions) {
        this(saga);
        this.timeToReviewTransactions = timeToReviewTransactions;
    }

    private void setFlowManagerSagaTransactionStatus(FlowManagerSagaTransactionStatus fMSTS) {
        // El estado inconsistente no puede ser cambiado
        if (this.flowManagerSagaTransactionStatus == FlowManagerSagaTransactionStatus.INCONSISTENT) {
            return;
        }
        // El estado finished with errors solo puede ser cambiado a inconsistente
        if (this.flowManagerSagaTransactionStatus == FlowManagerSagaTransactionStatus.FINISHED_WITH_ERRORS_CONTROLLATED
                && fMSTS == FlowManagerSagaTransactionStatus.INCONSISTENT) {
            this.flowManagerSagaTransactionStatus = fMSTS;
            return;
        }
        // Ademas,
        if (this.flowManagerSagaTransactionStatus != FlowManagerSagaTransactionStatus.FINISHED_WITH_ERRORS_CONTROLLATED
                && this.flowManagerSagaTransactionStatus != FlowManagerSagaTransactionStatus.INCONSISTENT) {
            this.flowManagerSagaTransactionStatus = fMSTS;
        }
    }

    /**
     * Runs the saga transaction chain and computes the final status.
     * 
     * @throws Exception if an unexpected error occurs
     */
    public void runSagaTransactions() throws Exception {
        setFlowManagerSagaTransactionStatus(FlowManagerSagaTransactionStatus.RUNNING);
        try {
            SagaTransaction currentTransaction = this.initialSagaTransaction;
            do {
                // before run process of current transaction
                runPreProcessForRunTransaction(currentTransaction);
                // run currenttransaction
                runSagaTransaction(currentTransaction);
                // post process for transaction
                postRunTransactionProcess(currentTransaction);
                // asign next transaction (The roolbacks are processed in
                // postRunTransactionProcess)
                if (currentTransaction.transaction.getTransactionStatus() == TransactionStatus.FAILED) {
                    break;
                }
                currentTransaction = currentTransaction.innerSagaTransaction;
            } while (currentTransaction != null);
            setFlowManagerSagaTransactionStatus(getStatusOfSaga());
        } catch (Exception ex) {
            setFlowManagerSagaTransactionStatus(FlowManagerSagaTransactionStatus.INCONSISTENT);
            log.info("Saga finished with inconsistencies");
        }
        log.info("No more transactions to process.");
        log.info("The final status of saga is: " + this.getFlowManagerSagaTransactionStatus());
    }

    private void runPreProcessForRunTransaction(SagaTransaction sagaTransaction) {
        sagaTransaction.transaction.setPreProcessForRunTransactionStatus(PreProcessForRunTransactionStatus.RUNNING);
        try {
            sagaTransaction.transaction.preProcessForRunTransaction();
            sagaTransaction.transaction
                    .setPreProcessForRunTransactionStatus(PreProcessForRunTransactionStatus.SUCCESFUL);
        } catch (Exception ex) {
            sagaTransaction.transaction.setPreProcessForRunTransactionStatus(PreProcessForRunTransactionStatus.FAILED);
            log.info("Preprocess for transaction: " + sagaTransaction.transaction.getDescription()
                    + " have been failed.");
        }
    }

    private void runSagaTransaction(SagaTransaction sagaTransaction) {
        // Si el preprocess salio bien O puedo correr la transaccion aun cuando fallo el
        // preprocess
        if (sagaTransaction.transaction
                .getPreProcessForRunTransactionStatus() == PreProcessForRunTransactionStatus.SUCCESFUL
                || sagaTransaction.transaction
                        .getPreProcessForRunTransactionStatus() == PreProcessForRunTransactionStatus.READY
                || sagaTransaction.transaction.runTransactionWithErrorsInPreProcessForRunTransaction()) {
            runThreadForTransaction(sagaTransaction);
        } else {
            sagaTransaction.transaction.setTransactionStatus(TransactionStatus.FAILED);
        }

    }

    private void runThreadForTransaction(SagaTransaction sagaTransaction) {
        Thread threadCurrentTransaction;
        TransactionThreadWrapper threadTransactionWrapper;
        // run transaction
        try {
            // sagaTransaction.transaction.
            // instantiate the thread wrapper for transaction object
            threadTransactionWrapper = new TransactionThreadWrapper<>(
                    sagaTransaction.transaction);
            // Create a thread object
            threadCurrentTransaction = new Thread(threadTransactionWrapper,
                    "Transaction - Run: " + sagaTransaction.transaction.getDescription());
            // Trasnaction time out
            Long transactionTimeOut = sagaTransaction.transaction.getTransactionPolitics().transactionTimeOut();
            // Start the thread execution
            threadCurrentTransaction.start();
            while (true) {
                log.info("Still processing transaction: " + sagaTransaction.transaction.getDescription() + "\n");
                log.info("Thread Status: " + threadTransactionWrapper.getTransactionThreadStatus() + "\n");
                // Sleep this thread
                Thread.sleep(timeToReviewTransactions);
                // update elapsed time for transaction
                this.transactionElapsedTime = this.transactionElapsedTime + timeToReviewTransactions;
                // Hilo de ejecucion correcto? si, entonces salimos del while
                if (threadTransactionWrapper
                        .getTransactionThreadStatus() == TransactionThreadStatus.THREAD_FINISHED) {
                    break;
                } // Si hilo de ejecucion con errores, entonces levantar la excepsion
                else if (threadTransactionWrapper
                        .getTransactionThreadStatus() == TransactionThreadStatus.THREAD_FINISHED_WITH_ERRORS) {
                    throw new Exception(threadTransactionWrapper.getTransactionThreadException());
                } // Verifico el timeout de la transaction
                else if (this.transactionElapsedTime >= transactionTimeOut) {
                    // Clean timeout variable
                    this.transactionElapsedTime = 0L;
                    // interrupt transaction thread
                    threadCurrentTransaction.interrupt();
                    // raise exception
                    throw new Exception("Error, transaction timeout has been reach.");
                }
            }
            // Clean timeout variable
            this.transactionElapsedTime = 0L;
            // Set status for transaction
            sagaTransaction.transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        } catch (Exception e) {
            sagaTransaction.transaction.setTransactionStatus(TransactionStatus.FAILED);
            log.info(
                    "Transaction: " + sagaTransaction.transaction.getClass() + " have been failed. Error: " + e);
        }
    }

    private void postRunTransactionProcess(SagaTransaction sagaTransactionDone) throws Exception {
        // Report transaction status
        log.info("Final status of transaction: " + sagaTransactionDone.transaction.toString());
        switch (sagaTransactionDone.transaction.getTransactionStatus()) {
            case FAILED:
                runRollBacksSagaTransaction(sagaTransactionDone);
                break;
            case SUCCESSFUL:
                log.info("Transaction done correctly: " + sagaTransactionDone.transaction.getDescription());
                break;
            default:
                throw new Exception(
                        "Callback for the setted state for 'after run transaction' do not implemented.");
        }
    }

    private void runRollBacksSagaTransaction(SagaTransaction sagaTransaction) throws Exception {
        // Si father null significa que es la transaccion inicial.

        if (sagaTransaction.fatherSagaTransaction == null) {
            log.info(
                    "No rollbacks to do for the failed transaction: " + sagaTransaction.transaction.getDescription());
            return;
        }
        try {
            SagaTransaction currentTransactionToRollback = sagaTransaction.fatherSagaTransaction;
            do {
                runRollBackSagaTransaction(currentTransactionToRollback);
                // asign next transaction to rollbacked
                currentTransactionToRollback = currentTransactionToRollback.fatherSagaTransaction;

            } while (currentTransactionToRollback != null && !exceptionInRollback);
        } catch (Exception e) {
            setFlowManagerSagaTransactionStatus(FlowManagerSagaTransactionStatus.INCONSISTENT);
            log.info("Saga generates inconsistencies at rollbacks triggeres by transaction: "
                    + sagaTransaction.transaction.getDescription());
        }

    }

    private void runCatchErrorSagaTransaction(SagaTransaction sagaTransaction) throws Exception {
        sagaTransaction.transaction.catchErrors();
    }

    private void runRollBackSagaTransaction(SagaTransaction sagaTransaction) throws Exception {
        try {
            // sagaTransaction.transaction.rollback();
            runThreadForRollbackTransaction(sagaTransaction);
        } catch (Exception e) {
            exceptionInRollback = true;
            sagaTransaction.transaction.setTransactionStatus(TransactionStatus.FAILED);
            setFlowManagerSagaTransactionStatus(FlowManagerSagaTransactionStatus.INCONSISTENT);
            log.info("Saga generates inconsistencies at rollback of transaction: "
                    + sagaTransaction.transaction.getDescription());
        }
        try {
            runCatchErrorSagaTransaction(sagaTransaction);
        } catch (Exception e) {
            setFlowManagerSagaTransactionStatus(FlowManagerSagaTransactionStatus.FINISHED_WITH_ERRORS_CONTROLLATED);
            log.info("Saga generates controllated errors at catchErrors function of transaction: "
                    + sagaTransaction.transaction.getDescription());
        }
    }

    private void runThreadForRollbackTransaction(SagaTransaction sagaTransaction) throws Exception {
        Thread threadCurrentTransaction;
        TransactionRollbackThreadWrapper threadTransactionWrapper;
        // instantiate the thread wrapper for transaction object
        threadTransactionWrapper = new TransactionRollbackThreadWrapper<>(
                sagaTransaction.transaction);
        // Create a thread object
        threadCurrentTransaction = new Thread(threadTransactionWrapper,
                "Rollback Transaction - Run: " + sagaTransaction.transaction.getDescription());
        // Rollback timeout
        Long transactionTimeOut = sagaTransaction.transaction.getTransactionPolitics().transactionTimeOut();
        // Start the thread execution
        threadCurrentTransaction.start();
        while (true) {
            log.info("Still processing transaction Rollback: " + sagaTransaction.transaction.getDescription() + "\n");
            log.info("Thread Status: " + threadTransactionWrapper.getTransactionThreadStatus() + "\n");
            Thread.sleep(timeToReviewTransactions);
            // update elapsed time for transaction
            this.transactionElapsedTime = this.transactionElapsedTime + timeToReviewTransactions;
            // Hilo de ejecucion correcto? si, entonces salimos del while
            if (threadTransactionWrapper
                    .getTransactionThreadStatus() == TransactionThreadStatus.THREAD_FINISHED) {
                break;
            } // Si hilo de ejecucion con errores, entonces levantar la excepcion
            else if (threadTransactionWrapper
                    .getTransactionThreadStatus() == TransactionThreadStatus.THREAD_FINISHED_WITH_ERRORS) {
                exceptionInRollback = true;
                throw new Exception(threadTransactionWrapper.getTransactionThreadException());
            } // Verifico el timeout de la transaction
            else if (this.transactionElapsedTime >= transactionTimeOut) {
                // Clean timeout variable
                this.transactionElapsedTime = 0L;
                // interrupt transaction thread
                threadCurrentTransaction.interrupt();
                // raise exception
                throw new Exception("Error, rollback timeout has been reach.");
            }
        }
        // Clean timeout variable
        this.transactionElapsedTime = 0L;
    }

    private FlowManagerSagaTransactionStatus getStatusOfSaga() {
        // Inconsistent status always will be inconsistent
        if (this.getFlowManagerSagaTransactionStatus() == FlowManagerSagaTransactionStatus.INCONSISTENT) {
            return FlowManagerSagaTransactionStatus.INCONSISTENT;
        }
        // If any transaction have the state of succesfull with errors or failed, then,
        // the saga status will be sucessfull with errors
        // else status will be finished correctly
        FlowManagerSagaTransactionStatus result = FlowManagerSagaTransactionStatus.FINISHED_CORRECTLY;
        SagaTransaction currentSagaTransaction = this.initialSagaTransaction;
        do {
            if (currentSagaTransaction.transaction.getTransactionStatus() == TransactionStatus.FAILED
                    || currentSagaTransaction.transaction
                            .getTransactionStatus() == TransactionStatus.SUCCESFUL_WITH_ERRORS) {
                result = FlowManagerSagaTransactionStatus.FINISHED_WITH_ERRORS_CONTROLLATED;
                break;
            }
            currentSagaTransaction = currentSagaTransaction.innerSagaTransaction;
        } while (currentSagaTransaction != null);
        return result;
    }

}
