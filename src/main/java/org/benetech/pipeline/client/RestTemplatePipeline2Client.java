package org.benetech.pipeline.client;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of DAISYPipeline2Client interface that uses the Spring RestTemplate for web service calls.
 * @author Rom Srinivasan
 * @author John Brugge
 * @author Jake Brownell
 */
public class RestTemplatePipeline2Client implements DAISYPipeline2Client {
    private String serviceUrl;
    private String jobsSubmitUrl;
    private String jobsStatusUrl;
    private String jobsResultUrl;
    private String jobsLogUrl;

    private RestTemplate restTemplate;

    /**
     * Default constructor.
     */
    public RestTemplatePipeline2Client() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String submitJob(final String scriptName, final File inputFile, final String... fileReferences) {
        File jobRequestFile = null;
        try {
            //Create job request xml file
            jobRequestFile = createJobRequestFile(scriptName, fileReferences);

            //Collect arguments to web service
            final MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
            parts.add("job-data", inputFile);
            parts.add("job-request", jobRequestFile);

            //POST job request to web service
            final String result = restTemplate.postForObject(jobsSubmitUrl, parts, String.class);

            // parse xml result to return newly created job's id
            final Document dom = parse(result);
            final Element job = dom.getRootElement();

            return job.getAttribute("id").getValue();

        } catch (final IOException e) {
            throw new DAISYPipelineException(e);
        } catch (final JDOMException e) {
            throw new RuntimeException(e);
        } catch (final RestClientException e) {
            throw new DAISYPipelineException("Error from pipeline URL " + jobsSubmitUrl, e);
        } finally {
            FileUtils.deleteQuietly(jobRequestFile);
        }
    }

    /**
     *
     * @param scriptName String name of script to run
     * @param fileReferences one or more Strings that specify the relative path to files within the inputFile
     * @return String filename of new job request xml file
     * @throws IOException on file operation
     */
    private File createJobRequestFile(final String scriptName, final String[] fileReferences)
            throws IOException
    {
        final StringBuilder jobRequest =
                new StringBuilder("<jobRequest xmlns='http://www.daisy.org/ns/pipeline/data'>\n");
        jobRequest.append("<script href='").append(serviceUrl).append("/scripts/").append(scriptName).append("'/>");
        jobRequest.append("\n<input name='source'>");
        for (final String fileRef : fileReferences) {
            jobRequest.append("\n<item value='").append(fileRef).append("'/>");
        }
        jobRequest.append("\n</input>");
        // XML validation should be warnings only, not errors
        jobRequest.append("\n<option name='assert-valid'>false</option>");
        // following line is only needed when web service is running in local mode
        //jobRequest.append("\n<option name='output-dir'>/result</option>");
        jobRequest.append("\n</jobRequest>");

        final File jobRequestFile = File.createTempFile("pipelineRequest_", ".xml");
        FileUtils.writeStringToFile(jobRequestFile, jobRequest.toString());

        return jobRequestFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PipelineJobStatus getJobStatus(final String jobId) {
        try {
            final String result = restTemplate.getForObject(jobsStatusUrl, String.class, jobId);
            final Document dom = parse(result);
            if (dom != null) {
                final Element job = dom.getRootElement();
                final String statusString = job.getAttributeValue("status");
                return PipelineJobStatus.valueOf(statusString);
            } else {
                throw new DAISYPipelineException("Job not found " + jobId);
            }
        } catch (final IOException e) {
            throw new DAISYPipelineException(e);
        } catch (final JDOMException e) {
            throw new RuntimeException(e);
        } catch (final RestClientException e) {
            throw new DAISYPipelineException("Error from pipeline URL " + jobsStatusUrl + ", jobId " + jobId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getJobResults(final String jobId) {
        File zipFile = null;
        try {
            zipFile = restTemplate.getForObject(jobsResultUrl, File.class, jobId);

            //Return epub within zip file
            final List<String> fileNames = getZipNames(zipFile);
            for (String name : fileNames) {
                if (name.endsWith(".epub")) {
                    return extractFile(zipFile, name, FileUtils.getTempDirectory());
                }
            }
            throw new DAISYPipelineException("Epub file not found in zip for jobId " + jobId);
        } catch (final RestClientException e) {
            throw new DAISYPipelineException("Error from pipeline URL " + jobsResultUrl + ", jobId " + jobId, e);
        } catch (final IOException e) {
            throw new DAISYPipelineException("Error reading zip file for jobId " + jobId, e);
        } finally {
            if (null != zipFile) {
                FileUtils.deleteQuietly(zipFile);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJobLog(final String jobId) {
        try {
            return restTemplate.getForObject(jobsLogUrl, String.class, jobId);
        } catch (final RestClientException e) {
            throw new DAISYPipelineException("Error from pipeline URL " + jobsLogUrl + ", jobId " + jobId, e);
        }
    }

    /**
     * @param serviceUrl the url of the pipeline web service
     */
    public void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
        this.jobsSubmitUrl = serviceUrl + "/jobs";
        this.jobsStatusUrl = serviceUrl + "/jobs/{id}?msgSeq=100000";
        this.jobsResultUrl = serviceUrl + "/jobs/{id}/result";
        this.jobsLogUrl = serviceUrl + "/jobs/{id}/log";
    }

    /**
     * @param restTemplate the restTemplate to set
     */
    public void setRestTemplate(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    //-------- Utility method --------
    
    /**
     * Get a list of the names of the files inside of a zip.
     * @param file not null
     * @return the names of the files in the zip.
     */
    private List<String> getZipNames(final File file)
            throws IOException
    {
        ZipFile zip = null;
        try {
            zip = new ZipFile(file);
            final List<? extends ZipEntry> entries = Collections.list(zip.entries());
            final List<String> names = new ArrayList<String>(entries.size());
            for (final ZipEntry entry : entries) {
                names.add(entry.getName());
            }
            return names;
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }
    
    /**
     * Extract a single file from a zip file.
     * @param file the reference to the zip file
     * @param path the path in the zip file to extract
     * @param tmpPath the location under which the extracted file will be created, could be a subtree depending on the
     * filePath
     * @return a reference to the extracted file
     * @throws IOException zip or io issue
     */
    private File extractFile(final File file, final String path, final File tmpPath)
            throws IOException
    {
        ZipFile zip = null;
        try {
            zip = new ZipFile(file);
            final ZipEntry entry = zip.getEntry(path);
            if (entry == null) {
                throw new ZipException("No zip file under path=" + path);
            }
            final File out = new File(tmpPath, entry.getName());
            if (!entry.isDirectory()) {
                // this will make any necessary parent directories
                FileUtils.copyInputStreamToFile(zip.getInputStream(entry), out);
            } else {
                // if there is an empty directory it will not be made as part of the if branch, so go ahead and make it
                out.mkdirs();
            }

            return out;
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }

    /**
     * Parses a string into a Document. Note that this does <em>not</em> do any validation.
     * It's main use is for turning an abitrary XML fragment into a DOM structure.
     * @param s is a valid String to parse
     * @return an unvalidated Document
     * @throws IOException if there are any problems with the stream
     * @throws JDOMException if there are any parsing problems
     */
    private Document parse(final String s)
        throws IOException, JDOMException
    {
        final SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setValidation(false);
        return saxBuilder.build(new StringReader(s));
    }
}
