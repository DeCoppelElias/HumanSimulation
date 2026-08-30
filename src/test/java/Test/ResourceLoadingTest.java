package Test;

import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Guards the packaging contract rather than any simulation behaviour.
 *
 * GridPanel loads its icons with getClass().getResource("/Human.png"), which only
 * resolves if the images sit at the root of the classpath. Moving them out of
 * src/main/resources, or nesting them in a subdirectory, breaks the application at
 * runtime while everything still compiles and every other test still passes.
 */
class ResourceLoadingTest {

    @ParameterizedTest
    @ValueSource(strings = {"/Human.png", "/Food.jpg"})
    void iconIsAtTheClasspathRoot(String resourcePath) {
        URL url = ResourceLoadingTest.class.getResource(resourcePath);

        Assertions.assertNotNull(
                url,
                resourcePath + " is not at the classpath root. GridPanel loads it with "
                        + "getResource(\"" + resourcePath + "\"), so it has to stay directly "
                        + "under src/main/resources.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/Human.png", "/Food.jpg"})
    void iconDecodesToAnImage(String resourcePath) throws Exception {
        URL url = ResourceLoadingTest.class.getResource(resourcePath);
        Assertions.assertNotNull(url, resourcePath + " is missing from the classpath");

        BufferedImage image = ImageIO.read(url);

        Assertions.assertNotNull(image, resourcePath + " is present but ImageIO cannot decode it");
        Assertions.assertTrue(image.getWidth() > 0, resourcePath + " decoded to zero width");
        Assertions.assertTrue(image.getHeight() > 0, resourcePath + " decoded to zero height");
    }
}
