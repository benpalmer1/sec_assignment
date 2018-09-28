/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * NyTimes Plugin - Plugin to parse headlines from the New York Times website.
 */

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import newsfeed.controller.NFWindowController;
import newsfeed.model.Headline;
import newsfeed.model.Plugin;

public class NyTimes extends Plugin
{
    public NyTimes()
    {
        source = "https://www.nytimes.com";
        refreshInterval = 40;
    }
    
    // Parse Headlines method - receives the entire page text as HTML and parses with regex.
    // Implements the template method pattern, overriding the parseHeadlines abstract method in Plugin.
    @Override
    protected ArrayList<Headline> parseHeadlines(String pageText)
    {
        ArrayList<Headline> headlineList = new ArrayList<>();
        Pattern headlinePattern1 = Pattern.compile("<h2 class=\"story-heading\"><a.*?>(.*?)<\\/a><\\/h2>");
        
        Matcher matcher = headlinePattern1.matcher(pageText);
        while (matcher.find())
        {
            if (matcher.group(1) != null && !matcher.group(1).trim().equals(""))
            {
                if(!headlineList.contains(new Headline(source, matcher.group(1).trim(), NFWindowController.getTime())))
                {
                    // Some headlines contain strange formatting characters, removed with the String.replace methods.
                    headlineList.add(new Headline(source, matcher.group(1).replace("<br>", "").replace("</br>", "").trim(), NFWindowController.getTime()));
                }
            }
        }
        return headlineList;
    }
}