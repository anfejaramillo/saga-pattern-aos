package net.ajaramillo.artifacts.saga;

//@FunctionalInterface
class TransactionRollbackThreadWrapper<E extends Transaction> implements TransactionRunnable<Exception> {
    Transaction tr;
    private TransactionThreadStatus tts = TransactionThreadStatus.THREAD_CREATED;
    private Exception ttException;

    TransactionRollbackThreadWrapper(Transaction tr) {
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

    public TransactionThreadStatus getTransactionThreadStatus(){
        return this.tts;
    }

    //Save the exception raised by transaction task
    public void setTransactionThreadException(Exception ex){
        this.ttException = ex;
    }

    //Get the exception raised by transaction task
    public Exception getTransactionThreadException(){
        return this.ttException;
    }

}
