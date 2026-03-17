package net.ajaramillo.artifacts.saga;

import java.lang.reflect.Field;
import java.util.*;

public abstract class SagaContext {
    private List<Transaction> transactions = new ArrayList<>();
    private SagaOrchestator sagaOrchestrator;

    final void setSagaOrchestrator(SagaOrchestator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }

    public final SagaOrchestator getSagaOrchestrator() {
        return this.sagaOrchestrator;
    }

    public final List<Transaction> getTransactions() {
        return transactions;
    }

    final void addTransaction(Transaction trans) {
        this.transactions.add(trans);
    }

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
