package net.ajaramillo.artifacts.saga;

/**
 * Status values for the saga flow manager.
 */
public enum FlowManagerSagaTransactionStatus {
    READY, //Manager listo para lanzar las transacciones
    RUNNING,  //Manager administrando las transacciones SINCRONAMENT
    FINISHED_CORRECTLY, //Terminada la saga sin errores
    FINISHED_WITH_ERRORS_CONTROLLATED, // terminada la saga, pero los errores fueron manejados por las mismas transacciones (pueden ser rollbacks o transacciones adicionales)
    INCONSISTENT, //Terminada la saga con errores
}
