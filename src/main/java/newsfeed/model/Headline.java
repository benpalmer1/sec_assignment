package newsfeed.model;

/**
 * Model class to represent a headline for output to the user.
 */

public class Headline
{
    private String sourceURL;
    private String headline;
    private String timestamp;
    
    public Headline(String source, String headline, String timestamp)
    {
        this.sourceURL = source;
        this.headline = headline;
        this.timestamp = timestamp;
    }
    
    public String getSourceURL()
    {
        return sourceURL;
    }
    
    public String getHeadline()
    {
        return headline;
    }

    public String getTimestamp()
    {
        return timestamp;
    }
    
    public void setSourceURL(String sourceURL)
    {
        this.sourceURL = sourceURL;
    }
    
    public void setHeadline(String headline)
    {
        this.headline = headline;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString()
    {
        // Trim off the start of the source URL
        String tempSource = sourceURL.replace("http://", "");
        tempSource = sourceURL.replace("https://", "");
        return (tempSource + ": " + headline + " (" + timestamp + ")");
    }
}