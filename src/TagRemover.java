import org.jsoup.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/*
 * Utility class for manipulating an HTML DOM structure
 */
public class TagRemover {
	/*
	 * This method removes a provided DOM element from a provided document
	 */
	public static String TagRemover(String input, String tag) {
		Document doc = Jsoup.parse(input);
		for (Element element : doc.select(tag)) {
			element.remove();
		}
		return doc.toString();
	}
}
