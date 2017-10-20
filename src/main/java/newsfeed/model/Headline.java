package newsfeed.model;

/**
 * Model class to represent a headline for output to the user.
 */

public class Headline
{
    private String source;
    private String headline;
    private String timestamp;
    
    public Headline(String source, String headline, String timestamp)
    {
        this.source = source;
        this.headline = headline;
        this.timestamp = timestamp;
    }
    
    public String getSource()
    {
        return source;
    }
    
    public String getHeadline()
    {
        return headline;
    }

    public String getTimestamp()
    {
        return timestamp;
    }
    
    public void setSource(String source)
    {
        this.source = source;
    }
    
    public void setHeadline(String headline)
    {
        this.headline = headline;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }
    
    public String toString()
    {
        return (source + ": " + headline + "(" + timestamp + ")");
    }
}