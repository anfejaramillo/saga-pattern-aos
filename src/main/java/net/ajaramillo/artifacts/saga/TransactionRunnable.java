package net.ajaramillo.artifacts.saga;

interface TransactionRunnable<E extends Exception> extends Runnable {

    @Override
    default void run() throws RuntimeException {
        try {
            runTask();
        } catch (Exception ex) {
            System.out.println("Exception del runtask capturada.");
            setTransactionThreadException(ex);
            this.setTransactionThreadStatus(TransactionThreadStatus.THREAD_FINISHED_WITH_ERRORS);
        }
    }

    void runTask() throws E;

    void setTransactionThreadStatus(TransactionThreadStatus tts);

    void setTransactionThreadException(Exception ex);

}