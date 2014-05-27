# Libraries that should be installed in your maven local repository:
# grep "mvn install" ../pom.xml

mvn install:install-file -Dfile=lib/AppleJavaExtensions-1.6.jar -DgroupId=apple -DartifactId=AppleJavaExtensions -Dversion=1.6 -Dpackaging=jar
mvn install:install-file -Dfile=lib/BrowserLauncher2-1_3.jar -DgroupId=edu.stanford.ejalbert -DartifactId=BrowserLauncher2.orig -Dversion=1.3 -Dpackaging=jar
mvn install:install-file -Dfile=lib/jsamp-1.3-4+.jar -DgroupId=org.astrogrid -DartifactId=jsamp -Dversion=1.3-4+ -Dpackaging=jar

