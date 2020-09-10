import org.jsoup.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class TagRemover {

	public static String TagRemover(String input) {
		Document doc = Jsoup.parse(input);
		for (Element element : doc.select("pre")) {
			element.remove();
		}
		return doc.toString();
	}
}
