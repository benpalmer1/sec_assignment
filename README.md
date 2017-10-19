SEC Assignment - Newsfeed Application 
=====================================

This is a simple Java and Swing-based newsfeed application. Works by parsing html versions of popular news sites and displays the headings, websites and time/dates to the user. Is multithreaded and various tasks operate on different threads to operate more efficiently.

Building and Running
--------------------

Use Gradle to compile the application:

    $ gradle build
    
The build process will create a .jar file in build/libs. You can run the application as follows:

    $ java -jar build/libs/newsfeed.jar
        
Source code
-----------

The source code is (according to convention) located in src/main/java/newsfeed/.

Author
------

Benjamin Palmer - Curtin University 2017
