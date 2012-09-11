package org.benetech.pipeline.client;

import java.io.File;

/**
 * An interface for communicating with the DAISY Pipeline 2 web service API.
 * @see http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI
 * @author John Brugge
 */
public interface DAISYPipeline2Client {
    /**
     * Submit a job to the Pipeline.
     * @param scriptName String
     * @param inputFile File, a zip containing all files needed
     * @param fileReferences one or more Strings that specify the relative path to files within the inputFile
     * that are needed by the given script
     * @return String job identifier
     */
    String submitJob(String scriptName, File inputFile, String... fileReferences);

    /**
     * Get the status for a previously submitted job.
     * @param jobId String
     * @return status enum, or null if no job found
     */
    PipelineJobStatus getJobStatus(String jobId);

    /**
     * Get the output for a previously submitted job.
     * @param jobId String
     * @return File if job is complete, null otherwise
     */
    File getJobResults(String jobId);

    /**
     * Get the log output for a previously submitted job.
     * @param jobId String
     * @return log data if job and log output exists, null otherwise
     */
    String getJobLog(String jobId);
}
