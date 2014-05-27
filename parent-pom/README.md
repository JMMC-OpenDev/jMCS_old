PARENT POM
==========

This directory contains the jMCS parent pom.
It may be used for any other project that want to use it as parent.

Proceed first to its installation.

### Install:

```bash
cd parent-pom
mvn -Dassembly.skipAssembly -Djarsigner.skip=true clean install
```

### Use:

```xml
<parent>
    <groupId>fr.jmmc</groupId>
    <artifactId>jmmc</artifactId>
    <version>TRUNK</version>    
</parent>
```

By default parent pom sign jar of classes. To skip this operation (developer profile), please set the *jarsigner.skip=true* property.
Signing step requires to prepare a keystore and some properties (see below) to make signing process valid
Else you will have :

```xml
<settings>
...

    <profiles>
        <profile>
            <id>dev</id>
            <properties>
		<!-- disable jar signer -->
                <jarsigner.skip>true</jarsigner.skip>
		<!-- disable javadoc -->
		<maven.javadoc.skip>true</maven.javadoc.skip>
		<!-- disable tests -->
                <maven.test.skip>true</maven.test.skip>
            </properties>
        </profile>

        <profile>
            <id>deployer</id>
            <properties>
                <jarsigner.skip>false</jarsigner.skip>
                <jarsigner.alias>codesigningcert</jarsigner.alias>
                <jarsigner.keystore>/home/MCS/etc/globalsign.jks</jarsigner.keystore>
                <jarsigner.keypass>XXXXXX</jarsigner.keypass>
                <jarsigner.storepass>XXXXXX</jarsigner.storepass>
            </properties>
        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>dev</activeProfile>
    </activeProfiles>

...
</settings>
```

