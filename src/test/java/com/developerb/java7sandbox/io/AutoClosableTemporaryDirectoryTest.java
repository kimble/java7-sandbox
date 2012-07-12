package com.developerb.java7sandbox.io;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.Assertions;
import org.fest.assertions.FileAssert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static java.nio.file.Files.createTempDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * Using Java 7's AutoCloseable to execute some code with access to a system
 * temporary directory and have it automagically cleaned up when the code has executed.
 *
 * @author Kim A. Betti
 */
public class AutoClosableTemporaryDirectoryTest {

    @Test
    public void temporaryDirectoryAvailableInsideTryBlock() throws Exception {
        try (TemporaryDirectory directory = new TemporaryDirectory()) {
            assertThat(directory).exists().isDirectory();
        }
    }

    @Test
    public void temporaryDirectoryIsWritable() throws Exception {
        try (TemporaryDirectory directory = new TemporaryDirectory()) {
            File tmp = directory.getTemporaryDirectory();
            File testFile = new File(tmp, "test.txt");
            FileUtils.touch(testFile);

            Assertions.assertThat(testFile)
                    .as("file created in temporary folder")
                    .exists()
                    .isFile();
        }
    }

    @Test
    public void theDirectoryIsDeletedAfterTheTryBlock() throws Exception {
        File tmp;
        try (TemporaryDirectory directory = new TemporaryDirectory()) {
            tmp = directory.getTemporaryDirectory();
        }

        Assertions.assertThat(tmp)
                .as("temporary directory created inside the try block")
                .isNotNull()
                .doesNotExist();
    }

    @Test(expected = IllegalStateException.class)
    public void theTemporaryDirectoryIsCleanedUpEvenAfterExceptionInsideTryBlock() throws Exception {
        File tmp = null;
        try (TemporaryDirectory directory = new TemporaryDirectory()) {
            tmp = directory.getTemporaryDirectory();
            throw new IllegalStateException("Expected exception");
        }
        finally {
            Assertions.assertThat(tmp)
                    .as("temporary directory created inside the try block")
                    .isNotNull()
                    .doesNotExist();
        }
    }

    private FileAssert assertThat(TemporaryDirectory directory) {
        return Assertions.assertThat(directory.getTemporaryDirectory())
                .as("temporary directory")
                .isNotNull();
    }


    private static class TemporaryDirectory implements AutoCloseable {

        private final File temporaryDirectory;

        TemporaryDirectory() throws IOException {
            temporaryDirectory = createTempDirectory("java7-testing-").toFile();
        }

        public File getTemporaryDirectory() {
            return temporaryDirectory;
        }

        @Override
        public void close() throws Exception {
            deleteDirectory(temporaryDirectory);
        }

    }

}
