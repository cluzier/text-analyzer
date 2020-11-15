package analyzer;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing analysis of the html and content on page, fetching the content from the url and headers
 */
class TextAnalyzerTest {
    String header = "Rank    Word                 Frequency \n";
    String oneOccurance = "Rank    Word                 Frequency \n" + "1       the                  59       \n" + "";

    @Test
    void analyze() {
        assertEquals(oneOccurance, TextAnalyzer.Analyze(1));
    }

    @Test
    void fetchUrlContent() {
        try {
            String example = "https://example.com";
            String expected = "<!doctype html>";
            assertEquals(expected, TextAnalyzer.fetchUrlContent(example).readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void stringHeaders() {
        assertEquals(header, TextAnalyzer.stringHeaders());
    }
}