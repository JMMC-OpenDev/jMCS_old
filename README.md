jMCS
====

Java framework from [JMMC](http://www.jmmc.fr), to homogenize your GUI across all the 3 main desktop OS, and further integrates your app to them.

Its primary goal is to centralize all GUI apps shared functionalities (e.g menubar handling, about box window, ...) in order to provide end users with a more consistent, feature-reach, desktop-class application family, as integrated as possible across Linux, Mac OS X and Windows, while freeing you developers of this tedious work !

The ultimate goal is to leverage your end users knowledge of their favorite platform, to let them fill right at home while using your applications, thus improving the perceived quality of your products while factorizing and sharing your development efforts.

Your app then feels better to the end user (by truly respecting its platform of choice), and we free you of all those nasty details !

License
=======

BSD 3-Clause : see [LICENSE.txt](../master/LICENSE.txt)

Build
=====

jMCS uses `maven` to build from sources. Please type following commands:

```
git clone https://github.com/JMMC-OpenDev/jMCS.git
cd jMCS
mvn clean package
```

Jar files are then available in `target` directory !
