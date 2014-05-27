# Libraries that should be installed in your maven local repository:
# grep "mvn install" ../pom.xml

mvn install:install-file -Dfile=lib/jmcs-demo-doc.jar -DgroupId=fr.jmmc.jmcs -DartifactId=jmcs-demo-doc -Dversion=1.0 -Dpackaging=jar

