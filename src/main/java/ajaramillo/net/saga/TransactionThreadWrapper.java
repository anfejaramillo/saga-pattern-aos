package ajaramillo.net.saga;

//@FunctionalInterface
class TransactionThreadWrapper<E extends Transaction> implements TransactionRunnable<Exception> {
    Transaction tr;
    private TransactionThreadStatus tts = TransactionThreadStatus.THREAD_CREATED;
    private Exception ttException;

    TransactionThreadWrapper(Transaction tr) {
        this.tr = tr;
    }

    @Override
    public void runTask() throws Exception {
        //thisThread = Thread.currentThread();
        // Change state of transaction
        tr.setTransactionStatus(TransactionStatus.RUNING);
        //Change thread status
        setTransactionThreadStatus(TransactionThreadStatus.THREAD_RUNNING);
        //Run task
        tr.task();
        //Update transaction status
        tr.setTransactionStatus(TransactionStatus.SUCCESSFUL);
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
