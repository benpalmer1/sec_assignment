/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * Arstechnica plugin - Plugin to parse headlines from the Arstechnica website.
 */
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import newsfeed.controller.NFWindowController;
import newsfeed.model.Headline;
import newsfeed.model.Plugin;

public class Arstechnica extends Plugin
{
    public Arstechnica()
    {
        source = "https://arstechnica.com";
        refreshInterval = 120;
    }
    
    // Parse Headlines method - receives the entire page text as HTML and parses with regex.
    // Implements the template method pattern, overriding the parseHeadlines abstract method in Plugin.
    @Override
    protected ArrayList<Headline> parseHeadlines(String pageText)
    {
        ArrayList<Headline> headlineList = new ArrayList<>();
        Pattern headlinePattern1 = Pattern.compile("(?:<h1 class=\"heading\">)<a.*?>(.*?)<\\/a><\\/h1>");

        Matcher matcher = headlinePattern1.matcher(pageText);
        while (matcher.find())
        {
            if (matcher.group(1) != null && !matcher.group(1).trim().equals(""))
            {
                if(!headlineList.contains(new Headline(source, matcher.group(1).trim(), NFWindowController.getTime())))
                {
                    // Headlines contain strange formatting characters, removed with the String.replace methods.
                    String headline = matcher.group(1).replace("<em>","").replace("</em>","").trim();
                    headlineList.add(new Headline(source, headline, NFWindowController.getTime()));
                }
            }
        }
        return headlineList;
    }
}