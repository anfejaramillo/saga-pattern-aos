package net.ajaramillo.artifacts.saga;

/**
 * Runnable wrapper that captures checked exceptions and updates thread status.
 * @param <E> exception type that can be thrown by {@link #runTask()}
 */
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

    /**
     * Executes the task logic.
     * @throws E when the task fails
     */
    void runTask() throws E;

    /**
     * Updates the internal transaction thread status.
     * @param tts new status
     */
    void setTransactionThreadStatus(TransactionThreadStatus tts);

    /**
     * Stores the exception raised by the task.
     * @param ex exception to store
     */
    void setTransactionThreadException(Exception ex);

}
