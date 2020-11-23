package analyzer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TagRemoverTest {

	String testDOM = "<html> \n" + 
			" <head> \n" +
			"</head>\n" + 
			"<pre>\n" + 
			"<p>this is the pre</p>\n" + 
			"</pre>\n" + 
			"<body>  \n" +
			"<p>this is a test</p> \n" +
			"</body>" +
			"</html>";
	
	String expectedOutput = "<html> \n" + 
			" <head> \n" + 
			" </head> \n" +
			" <body>  \n" +
			"  <p>this is a test</p> \n" + 
			" </body>\n" +
			"</html>";
	
	@Test
	void test() {
		assertEquals(expectedOutput, TagRemover.TagRemover(testDOM, "pre"));
	}

}
