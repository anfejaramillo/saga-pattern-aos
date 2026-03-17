package net.ajaramillo.artifacts.saga;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Base context shared by all transactions in a saga. Implementations can
 * add custom fields to hold shared data.
 */
public abstract class SagaContext {
    private List<Transaction> transactions = new ArrayList<>();
    private SagaOrchestator sagaOrchestrator;

    /**
     * Sets the orchestrator that owns this context.
     * @param sagaOrchestrator orchestrator instance
     */
    final void setSagaOrchestrator(SagaOrchestator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }

    /**
     * Returns the associated saga orchestrator.
     * @return orchestrator instance
     */
    public final SagaOrchestator getSagaOrchestrator() {
        return this.sagaOrchestrator;
    }

    /**
     * Returns the list of transactions registered in the saga.
     * @return transactions list
     */
    public final List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Adds a transaction to the internal list.
     * @param trans transaction to add
     */
    final void addTransaction(Transaction trans) {
        this.transactions.add(trans);
    }

    /**
     * Removes a transaction from the internal list.
     * @param trans transaction to remove
     */
    final void removeTransaction(Transaction trans) {
        this.transactions.remove(trans);
    }

    @Override
    public final String toString() {
        String result = "";
        Object value;
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true); // You might want to set modifier to public first.
            try {
                value = field.get((Object) this);
                if (value != null) {
                    result = result + field.getName() + "=" + value + ",";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "SagaContext [" + result + "]";
    }

}
