package org.benetech.pipeline.client;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

// NoCheck
public class RestTemplatePipeline2ClientTest {
    private RestTemplatePipeline2Client client;
    private RestTemplate mockTemplate;
    private String jobId;

    @Before
    public void setUpClient() {
        client = new RestTemplatePipeline2Client();

        mockTemplate = createMock(RestTemplate.class);

        client.setRestTemplate(mockTemplate);
        client.setServiceUrl("https://some.place/ws");

        jobId = "test-id";
    }

    @Test
    public void testGetJobStatus() {
        // Set up the data
        final String statusResult = "<job xmlns=\"http://www.daisy.org/ns/pipeline/data\" id=\"job-id-01\" "
        		+ "href=\"http://example.org/ws/jobs/job-id-01\" status=\"DONE\"/>";
        expect(mockTemplate.getForObject(anyObject(String.class), eq(String.class), eq(jobId))).andReturn(statusResult);
        replay(mockTemplate);

        // Run the test
        final PipelineJobStatus result = client.getJobStatus(jobId);

        // Check the results
        assertEquals("Job status", PipelineJobStatus.DONE, result);
        verify(mockTemplate);
    }

    @Test
    public void testGetJobLog() {
        // Set up the data
        final String jobLog = "Some random text";
        expect(mockTemplate.getForObject(anyObject(String.class), eq(String.class), anyObject(String.class))).andReturn(jobLog);
        replay(mockTemplate);

        // Run the test
        final String result = client.getJobLog(jobId);

        // Check the results
        assertEquals("Results log length", true, result.length() > 0);
        verify(mockTemplate);
    }

    @Test
    public void testSubmitJob() {
        // Set up the data
        final File inputFile = new File("foo");
        final String fileReference = "some/relative/path";
        final String jobResult = "<job xmlns=\"http://www.daisy.org/ns/pipeline/data\" id=\"job-id-01\" "
                + "href=\"http://example.org/ws/jobs/job-id-01\" status=\"WHATEVER\"/>";
        expect(mockTemplate.postForObject(anyObject(String.class), anyObject(MultiValueMap.class), eq(String.class))).andReturn(jobResult);
        replay(mockTemplate);

        // Run the test
        final String result = client.submitJob("test", inputFile, fileReference);

        // Check the results
        assertEquals("Job ID", "job-id-01", result);
        verify(mockTemplate);
    }

    @Test
    public void testGetJobResults() throws IOException {
        // Set up the data
        final File tempDir = new File(FileUtils.getTempDirectory(), "pipelineTest");
        tempDir.mkdir();
        File tempEpub = new File(tempDir, "sample.epub");
        if (!tempEpub.exists()) {
            tempEpub.createNewFile();
        }
        final File jobResults = createZip("result.zip", tempDir);

        expect(mockTemplate.getForObject(anyObject(String.class), eq(File.class), eq(jobId))).andReturn(jobResults);
        replay(mockTemplate);

        // Run the test
        final File result = client.getJobResults(jobId);

        // Check the results
        assertNotNull("Results file", result);
        verify(mockTemplate);
    }

    @Test(expected=DAISYPipelineException.class)
    public void testServiceError() {
        // Set up the data
        expect(mockTemplate.getForObject(anyObject(String.class), eq(String.class), eq(jobId))).andThrow(new RestClientException("test"));
        replay(mockTemplate);

        // Run the test
        client.getJobStatus(jobId);

        // Check the results
    }
    
    /**
     * Create a zip file based upon the structure of a given directory.
     * @param fileName The output file name of the zip
     * @param dirToZip The directory to zip - complete tree of the output contents
     * @return a file reference to the new zip
     * @throws IOException if file access fails
     */
    private File createZip(final String fileName, final File dirToZip)
            throws IOException
    {
        final File outFile = new File(FileUtils.getTempDirectory(), fileName);
        final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile));
        try {
            for (final File file : dirToZip.listFiles()) {
                final ZipEntry anEntry = new ZipEntry(file.getName());
                zos.putNextEntry(anEntry);
                copyFileToStream(file, zos);
            }
        } finally {
            zos.close();
        }
        return outFile;
    }

    /**
     * Copy the contents of a file to the given OutputStream.
     * @param inputFile must be a file, not a directory
     * @param outputStream must be open
     * @throws IOException if file access fails
     */
    private void copyFileToStream(final File inputFile, final OutputStream outputStream)
            throws IOException
    {
        FileInputStream fis = null;
        try {
            fis = org.apache.commons.io.FileUtils.openInputStream(inputFile);
            IOUtils.copyLarge(fis, outputStream);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
}
// CheckOn
