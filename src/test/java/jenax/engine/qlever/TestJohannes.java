package jenax.engine.qlever;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.jenax.engine.qlever.RDFDatabaseBuilderQlever;
import org.junit.Test;


public class TestJohannes {
    @Test
    public void test() throws IOException, InterruptedException {
        Path basePath = Path.of("src/test/resources").toAbsolutePath();
        System.out.println(basePath);
        RDFDatabaseBuilderQlever<?> builder = new RDFDatabaseBuilderQlever<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(basePath, "*.ttl.gz")) {
            for (Path entry : stream) {
                // System.out.println("Matched file: " + entry.getFileName());
                builder.addPath(entry.toString());
            }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
        }

        Path outPath = Path.of("/tmp/myqlever");
        Files.createDirectories(outPath);
        builder.setOutputFolder(outPath);
        builder.setIndexName("foo");
        builder.build();
        // builder.addPath(path);
    }
}
