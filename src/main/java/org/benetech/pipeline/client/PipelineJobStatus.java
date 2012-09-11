package org.benetech.pipeline.client;

/**
 * Possible values for the status of job in the DAISY Pipeline 2 Web Service.
 * @see http://docs.daisy-pipeline.googlecode.com/hg/2012/2012-02-workshop/presentations/pipeline2-rest-api.html#slide-27.
 * @author John Brugge
 */
public enum PipelineJobStatus {
    IDLE,
    RUNNING,
    DONE,
    ERROR,
    ;
}
