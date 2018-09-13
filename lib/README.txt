# Libraries that should be installed in your maven local repository:
# grep "mvn install" ../pom.xml

mvn install:install-file -Dfile=lib/jmcs-MacOSX-9-TRUNK.jar -DgroupId=oss.jmcs -DartifactId=jmcs-MacOSX-9 -Dversion=TRUNK -Dpackaging=jar
mvn install:install-file -Dfile=lib/AppleJavaExtensions-1.6.jar -DgroupId=apple -DartifactId=AppleJavaExtensions -Dversion=1.6 -Dpackaging=jar
mvn install:install-file -Dfile=lib/BrowserLauncher2-1_4.jar -DgroupId=edu.stanford.ejalbert -DartifactId=BrowserLauncher2 -Dversion=1.4 -Dpackaging=jar
mvn install:install-file -Dfile=lib/jsamp-1.3.5.jar -DgroupId=org.astrogrid -DartifactId=jsamp -Dversion=1.3.5 -Dpackaging=jar
mvn install:install-file -Dfile=lib/jide-oss-3.7.4.jar -DgroupId=com.jidesoft -DartifactId=jide-oss -Dversion=3.7.4 -Dpackaging=jar

Or use:
mvn process-resources
