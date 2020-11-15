package analyzer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing the removal of tags
 */
class TagRemoverTest {

    String testDOM = "<html>\n" +
            "<head>\n" +
            "</head>\n" +
            "<pre>\n" +
            "<p>this is the pre</p>\n" +
            "</pre>\n" +
            "<body><p>this is a test</p>\n" +
            "</body>" +
            "</html>";

    String expectedOutput = "<html> \n" +
            " <head> \n" +
            " </head> \n" +
            " <body> \n" +
            "  <p>this is a test</p> \n" +
            " </body>\n" +
            "</html>";

    @Test
    void tagRemover() {
        assertEquals(expectedOutput, TagRemover.TagRemover(testDOM, "pre"));
    }
}