package org.benetech.pipeline.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * An implementation that reads and writes files directly from Http method bodies. The implementation should be able to
 * handle large files, as it does not buffer the entire file in memory.
 * @author Jake Brownell
 */
public final class FileHttpMessageConverter extends AbstractHttpMessageConverter<File> {
    private String tempFilePrefix;
    private String tempFileSuffix;

    /**
     * Default.
     */
    public FileHttpMessageConverter() {
        super(MediaType.ALL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long getContentLength(final File file, final MediaType contentType)
            throws IOException
    {
        return file.length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MediaType getDefaultContentType(final File file)
            throws IOException
    {
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File readInternal(final Class<? extends File> clazz, final HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException
    {
        final File destination = File.createTempFile(tempFilePrefix, tempFileSuffix);
        FileUtils.copyInputStreamToFile(inputMessage.getBody(), destination);
        return destination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean supports(final Class<?> clazz) {
        return File.class.equals(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeInternal(final File file, final HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException
    {
        FileInputStream fis = null;
        try {
            fis = FileUtils.openInputStream(file);
            IOUtils.copyLarge(fis, outputMessage.getBody());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * @param tempFilePrefix the tempFilePrefix to set
     */
    public void setTempFilePrefix(final String tempFilePrefix) {
        this.tempFilePrefix = tempFilePrefix;
    }

    /**
     * @param tempFileSuffix the tempFileSuffix to set
     */
    public void setTempFileSuffix(final String tempFileSuffix) {
        this.tempFileSuffix = tempFileSuffix;
    }
}
