package net.ajaramillo.artifacts.saga;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that defines runtime policies for a transaction.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TransactionPolitics {
    /**
     * Timeout (ms) for transaction processing or rollback. When the timeout is
     * reached, the executing thread is interrupted.
     * @return timeout in milliseconds
     */
    public long transactionTimeOut() default 120000L;
}
