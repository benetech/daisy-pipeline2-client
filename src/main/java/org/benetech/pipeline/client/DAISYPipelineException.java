package org.benetech.pipeline.client;

/**
 * An exception condition related to DAISY to Epub conversion using Pipeline2.
 * @author Rom Srinivasan
 */
public class DAISYPipelineException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public DAISYPipelineException() {
        super();
    }

    /**
     * Constructor with message.
     * @param message String
     */
    public DAISYPipelineException(final String message) {
        super(message);
    }

    /**
     * Wrapped exception with additional message.
     * @param message String
     * @param t Throwable
     */
    public DAISYPipelineException(final String message, final Throwable t) {
        super(message, t);
    }

    /**
     * Wrapped exception.
     * @param t Throwable
     */
    public DAISYPipelineException(final Throwable t) {
        super(t);
    }
}
