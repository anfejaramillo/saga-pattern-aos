package net.ajaramillo.artifacts.saga;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TransactionPolitics {
    //Timeout for whole transaction processing (or rollback processing). When the time has reach timeout then the thread is stopped.
    //This is in Milliseconds
    public long transactionTimeOut() default 120000L;
}
