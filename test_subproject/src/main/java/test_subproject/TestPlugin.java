import java.util.ArrayList;
import newsfeed.model.Plugin;

/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 */
public class TestPlugin extends Plugin {
    public TestPlugin()
    {
        source = "source.com";
        refreshInterval = 100;
    }
    
    @Override
    protected ArrayList<String> parseHeadlines(String pageText)
    {
        return new ArrayList<String>();
    }
}