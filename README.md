jMCS
====

Java framework from [JMMC](http://www.jmmc.fr), to homogenize your GUI across all the 3 main desktop OS, and further integrates your app to them.

Its primary goal is to centralize all GUI apps shared functionalities (e.g menubar handling, about box window, ...) in order to provide end users with a more consistent, feature-reach, desktop-class application family, as integrated as possible across Linux, Mac OS X and Windows, while freeing you developers of this tedious work !

The ultimate goal is to leverage your end users knowledge of their favorite platform, to let them fill right at home while using your applications, thus improving the perceived quality of your products while factorizing and sharing your development efforts.

Your app then feels better to the end user (by truly respecting its platform of choice), and we free you of all those nasty details !

Documentation
=============

Lets get started by browsing the [developer documentation](https://github.com/JMMC-OpenDev/jMCS/wiki/jMCS-Developer-Documentation) to further discover what jMCS provides...

The `javadoc` is also included in our [releases](https://github.com/JMMC-OpenDev/jMCS/releases).

License
=======

BSD 3-Clause : see [LICENSE.txt](../master/LICENSE.txt)

Goodies are also greatly appreciated if you feel like rewarding us for the job :)

Build
=====

jMCS uses `maven` to build from sources. Please type the following commands:

```
git clone https://github.com/JMMC-OpenDev/jMCS.git


# first time only: install parent-pom and missing libraries in maven repositories:
cd jMCS/parent-pom
mvn -Dassembly.skipAssembly -Djarsigner.skip=true clean install
cd ..
mvn process-resources
# build jMCS jar files
mvn clean package
```

Jar files are then available in `target` directory !
