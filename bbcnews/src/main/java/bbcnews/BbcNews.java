/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * BBC News Plugin. Parses the headlines from the BBC news website.
 */
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import newsfeed.controller.NFWindowController;
import newsfeed.model.Headline;
import newsfeed.model.Plugin;

public class BbcNews extends Plugin
{
    public BbcNews()
    {
        source = "http://www.bbc.com/news";
        refreshInterval = 90;
    }
    
    // Parse Headlines method - receives the entire page text as HTML and parses with regex.
    // Implements the template method pattern, overriding the parseHeadlines abstract method in Plugin.
    @Override
    protected ArrayList<Headline> parseHeadlines(String pageText)
    {
        ArrayList<Headline> headlineList = new ArrayList<>();
        Pattern headlinePattern1 = Pattern.compile("<h3.*?promo-heading__title.*?>(.*?)<\\/h3>");
        
        Matcher matcher = headlinePattern1.matcher(pageText);
        while (matcher.find())
        {
            if (matcher.group(1) != null && !matcher.group(1).trim().equals(""))
            {
                if(!headlineList.contains(new Headline(source, matcher.group(1).trim(), NFWindowController.getTime())))
                {
                    headlineList.add(new Headline(source, matcher.group(1).trim(), NFWindowController.getTime()));
                }
            }
        }
        return headlineList;
    }
}