package net.ajaramillo.artifacts.saga;

/**
 * Executes a transaction rollback in a thread and captures its status/exception.
 * @param <E> transaction type
 */
//@FunctionalInterface
class TransactionRollbackThreadWrapper<E extends STransaction> implements TransactionRunnable<Exception> {
    STransaction tr;
    private TransactionThreadStatus tts = TransactionThreadStatus.THREAD_CREATED;
    private Exception ttException;

    /**
     * Creates a wrapper for the given transaction.
     * @param tr transaction to rollback
     */
    TransactionRollbackThreadWrapper(STransaction tr) {
        this.tr = tr;
    }

    @Override
    public void runTask() throws Exception {
        //Change thread status
        setTransactionThreadStatus(TransactionThreadStatus.THREAD_RUNNING);
        //Run task
        tr.rollback();
        //Update thread status
        this.setTransactionThreadStatus(TransactionThreadStatus.THREAD_FINISHED);
    }

    @Override
    public void setTransactionThreadStatus(TransactionThreadStatus tts){
        this.tts = tts;
    }

    /**
     * Returns the current thread status.
     * @return status
     */
    public TransactionThreadStatus getTransactionThreadStatus(){
        return this.tts;
    }

    /**
     * Stores the exception raised by the rollback.
     * @param ex exception to store
     */
    public void setTransactionThreadException(Exception ex){
        this.ttException = ex;
    }

    /**
     * Returns the exception raised by the rollback.
     * @return exception or null
     */
    public Exception getTransactionThreadException(){
        return this.ttException;
    }

}
