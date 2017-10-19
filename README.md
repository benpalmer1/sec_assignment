FileSearcher 
============
(Part of worksheet 3 for COMP3003 Software Engineering Concepts)

This is a simple (simplistic) Java and Swing-based file searching application. It works, but it's essentially single-threaded, with everything happening inside Swing's event-dispatch thread. Your task is to make it multithreaded, using a  blocking queue to arrange for thread communication.

Building and Running
--------------------

Use Gradle to compile the application:

    $ gradle build
    
The build process will create a .jar file in build/libs. You can run the application as follows:

    $ java -jar build/libs/file_searcher.jar
        
Source code
-----------

The source code is (according to convention) located in src/main/java/edu/curtin/cs/filesearcher.
