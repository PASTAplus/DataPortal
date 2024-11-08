For the DataPortal, the DataPortal webapp deployment failed due to a runtime error
"Caused by: java.lang.UnsupportedClassVersionError: org/eclipse/jdt/internal/compiler/env/INameEnvironment
has been compiled by a more recent version of the Java Runtime (class file version 55.0), this version of
the Java Runtime only recognizes class file versions up to 52.0"
resulting from a class compilation error in the file "eclipse-jdt-core-3.27.0.jar." The short summary
is that "eclipse-jdt-core-3.27.0.jar" was compiled wiht JDK 11, while the running JRE is Java 8.

The solution was posted here: https://ubuntuforums.org/showthread.php?t=2475357. The steps of the
solution follow:
 - Downloading "org.eclipse.jdt.core-3.18.0.jar" file from https://repo1.maven.org/maven2/org/e...t.core/3.18.0/ (picked this file because it was compiled using JAVA 8)
 - Copying the downloaded file to the "/usr/share/java" folder
 - Removing the "/usr/share/java/eclipse-jdt-core.jar" symlink
 - Creating a new symlink -> ln -s /usr/share/java/org.eclipse.jdt.core-3.18.0.jar /usr/share/java/eclipse-jdt-core.jar
