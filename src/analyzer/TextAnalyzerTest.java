package analyzer;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class TextAnalyzerTest {

	String header = "Rank    Word                 Frequency \n";
		
	@Test
	public void testStringReturns() {
		assertEquals(header, TextAnalyzer.stringHeaders());
	}
	
	@Test
	public void testAnalyzer() throws IOException {
		String oneOccurance = "Rank    Word                 Frequency \n" + 
				"1       the                  59       \n";
		assertEquals(oneOccurance, TextAnalyzer.Analyze(1));
	}
	
	@Test
	public void testFetchUrl() {
		try {
			String example = "https://example.com";
			String expected = "<!doctype html>";
			assertEquals(expected, TextAnalyzer.fetchUrlContent(example).readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}




