# About BlackBerry&reg; WebWorks&trade;

The BlackBerry&reg; WebWorks&trade; for the BlackBerry Smartphone OS allows web and mobile web developers to use the SDK in combination with their development 
tooling of choice to develop, test and package up their web applications as BlackBerry WebWorks applications for smartphones. 
BlackBerry WebWorks applications can be distributed through the BlackBerry App World&trade; storefront and they run on the BlackBerry&reg; Smartphones 
with access to the hardware.

The project is open source under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) license.

* Advanced Standards
* Powerful Integration
* Open
 
[Read more](http://blackberry.github.com) about the BlackBerry WebWorks open source project

## Downloads
Full installers for Windows and Mac OS X can be found on the [product download page](http://developer.blackberry.com/html5/download/sdk)

## Reference Material &amp; Community
You can also find associated reference material for the BlackBerry WebWorks platform as well as community forums for building and contributing to the BlackBerry WebWorks project

* [API Reference](http://developer.blackberry.com/html5/api)
* [Installation and Developer Guides](http://developer.blackberry.com/html5/documentation)
* [Community Forums](http://supportforums.blackberry.com/t5/Web-Development/bd-p/browser_dev)
* [Project Contributor Forums](http://supportforums.blackberry.com/t5/BlackBerry-WebWorks/bd-p/ww_con)
* [Open Source Project Contributor Forums](http://supportforums.blackberry.com/t5/BlackBerry-WebWorks/bd-p/ww_con)

## Building the Source Code
 
### Download and install Maven on Windows&reg;
Note: In order to build the source code you must have the [Java SE Development Kit v1.6](http://java.sun.com/javase/downloads/index.jsp#jdk) or higher installed
 
1. The first step is to [Download Maven v3.0](http://maven.apache.org/download.html) and create an installation directory.
2. On the Maven download page, select the _(Binary zip)_ format of the latest Maven v3.0 from the mirror of your choice.
3. When the download is finished, take the file and unzip it to the "C:\Program Files\Apache Software Foundation" directory. If the directory does not exist create it.
4. The second step is to add environment variables. From the start menu right click on "My Computer" and click on properties. If you are on Windows XP click the advanced tab then click on environment variables. If you arer using Windows 7 click on advanced systems settings then click on environment variables.
5. To add the first environment variable, look under system variables, click on the new button and enter "M2_HOME" (without the quotes) for the variable name and enter the path to your maven installation directory for the variable value e.g. C:\Program Files\Apache Software Foundation\apache-maven-(your version number). Click ok when you're done.
6. Add a second environment variable with "M2" for the name and "%M2_HOME%\bin" (without the quotes) as the value.
7. If JAVA_HOME is not listed as one of the variables under System Variables then add a new environment variable with "JAVA_HOME" as the name and the path to you JDK installation directory (not the bin folder) as the value.
8. Click on the Path variable and click edit. Then add the following string to the end of value for path:
 
        %JAVA_HOME%\bin;%M2%
     
9. Open up a command prompt and type "mvn --version". _NOTE: If you already had a command prompt open, close it and open a new one so that your changes are reflected._ You should see some information about your maven installation. If you get a prompt stating that the command was not found then you probably made a mistake in one of the previous steps.

### Download and install maven on Mac OSX

Note: In order to build the source code you must have the Java Development Kit version 1.6 installed.


#### Using MacPorts 
It is recommended to install Maven on OSX using MacPorts. If you do not currently have MacPorts you can install it from http://www.macports.org/install.php.

1. Run the following command:
 
        sudo port install maven3
      
2. Run mvn --version to vertify that it is correctly installed.

If you do not wish to use MacPorts simply use the following instructions.

#### Without using MacPorts
1. Extract the distribution archive, i.e. apache-maven-3.0.3-bin.tar.gz to the directory you wish to install Maven 3.0.3. These instructions assume you chose /usr/local/apache-maven. The subdirectory apache-maven-3.0.3 will be created from the archive.
2. In a command terminal, add the M2_HOME environment variable, e.g. export M2_HOME=/usr/local/apache-maven/apache-maven-3.0.3.
3. Add the M2 environment variable, e.g. export M2=$M2_HOME/bin.
4. Optional: Add the MAVEN_OPTS environment variable to specify JVM properties, e.g. export MAVEN_OPTS="-Xms256m -Xmx512m". This environment variable can be used to supply extra options to Maven.
5. Add M2 environment variable to your path, e.g. export PATH=$M2:$PATH.
6. Make sure that JAVA_HOME is set to the location of your JDK, e.g. export JAVA_HOME=/usr/java/jdk1.5.0_02 and that $JAVA_HOME/bin is in your PATH environment variable.
7. Run mvn --version to verify that it is correctly installed.

### Build the project
 
From command line, change to the root directory of the WebWorks repository and run the following commands:
 
        mvn clean install -DPRODUCT_VERSION=version
 
Where the version is the WebWorks version that you want to build (e.g. 2.3.0.1), which will be shown when you execute "bbwp" in the command line.
The first time the build is run it will take up to 5 minutes to complete and will require an internet connection. Subsequent builds take around 2 minutes.
 
If the build is successful two zip files will be generated in a "target" directory located in the root of the WebWorks repository.
The Two zip files are "WebWorksForSmartphoneWin.zip" for Windows and "WebWorksForSmartphoneMac.zip" for Mac.


## Patching an Existing WebWorks Installation

1. Locate your existing WebWorks SDK installation. The default path is 
"C:\Program Files\Research In Motion\BlackBerry WebWorks SDK (Version)"
for Windows and 
"/Developer/SDKs/Research In Motion/BlackBerry WebWorks SDK (Version)"
for Mac.
2. In your WebWorks installation directory backup and delete the following files/folders:
 - "bbwp" executable file in root folder
 - "bbwp.jar", "bbwp.properties", "tld.txt" in bin folder 
 - "device_templates" and "ext" folder
3. The output of the build is stored in the target folder in the root
directory. It contains two zip files, "WebWorksForSmartphoneWin.zip"
for windows and "WebWorksForSmartphoneMac.zip" for Mac. Unzip the zip
file for your platform into your installation directory. 
4. If you are using Windows, open up the backup "bin\bbwp.properties" file
and copy the path located in the java element to the java element in the
new "bin\bbwp.properties" file.
5. You can now start building WebWorks applications with the patched SDK.


