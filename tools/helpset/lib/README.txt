# Libraries that should be installed in your maven local repository:
# grep "mvn install" ../pom.xml

# Downloaded from : http://sourceforge.net/projects/jhelpdev/files/jhelpdev/0.63/jhelpdev-0.63.zip/download

mvn install:install-file -Dfile=lib/jhelpdev.jar -DgroupId=net.sourceforge.jhelpdev -DartifactId=jhelpdev -Dversion=0.63 -Dpackaging=jar

mvn install:install-file -Dfile=lib/xmlenc.jar -DgroupId=org.znerd.xmlenc -DartifactId=xmlenc -Dversion=0.48 -Dpackaging=jar
