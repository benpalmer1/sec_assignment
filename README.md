SEC Assignment - Newsfeed Application 
=====================================

This is a simple Java and Swing-based newsfeed application. Works by parsing html versions of popular news sites and displays the headings, websites and time/dates to the user. Is multithreaded and various tasks operate on different threads to operate more efficiently.

Building and Running
--------------------

Use Gradle to compile the application:

    $ gradle build

I built a gradle task to move all subproject jars into the build/libs directory automatically:

    $ gradle moveJars

Or for ease of use, combine the commands:

    $ gradle build moveJars
    
Following this build process, you will create a 'newsfeed.jar' and move all 'aPlugin.jar' files into build/libs.
You can then run the application using the default news sources as follows:

    $ cd build/libs
    $ java -jar newsfeed.jar arstechnica.jar bbcsnews.jar nytimes.jar

Source code
-----------

The source code is (according to convention) located in src/main/java/newsfeed/.
For the developed plugins: plugin_name/src/main/java/plugin_name/

Author
------

Benjamin Palmer - 17743075 - Curtin University 2017
