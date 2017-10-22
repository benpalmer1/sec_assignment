import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import newsfeed.model.Headline;
import newsfeed.model.Plugin;

/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 */
public class TestPlugin extends Plugin
{
    public TestPlugin()
    {
        source = "http://www.aerocare.com.au";
        refreshInterval = 1;
    }
    
    @Override
    protected ArrayList<Headline> parseHeadlines(String pageText)
    {
        ArrayList<Headline> headlineList = new ArrayList();
        Pattern pattern = Pattern.compile("<h(.).*>(.*)</h\\1>");
        Matcher matcher = pattern.matcher(pageText);

        while (matcher.find())
        {
            if (matcher.group(2) != null)
            {
                headlineList.add(new Headline(source, matcher.group(2), getWindowController().getWindow().getTime()));
            }
        }

        return headlineList;
    }
}