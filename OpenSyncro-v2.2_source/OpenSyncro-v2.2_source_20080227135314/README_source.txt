======================================================================
Smilehouse OpenSyncro (version 2.2) Source Code Distribution
======================================================================

This file:            README_source.txt 
Purpose:              Overview of the software's source code package



----------------------------------------------------------------------
TABLE OF CONTENTS
----------------------------------------------------------------------

1.   Information about the software
2.   Contents of the source code package
3.   How to proceed?
3.1  Preparing to build OpenSyncro
3.2  Building OpenSyncro web application
3.3  Building a dynamic OpenSyncro component .jar package
4.   Contact



----------------------------------------------------------------------
1. Information about the software
----------------------------------------------------------------------

OpenSyncro is a lightweight, open source enterprise application
integration tool written in Java language and based on the J2SE
architecture. It runs on Apache Tomcat application server and requires
MySQL database with a JDBC driver.

OpenSyncro was originally developed for connecting Smilehouse
Workspace to other enterprise applications, for example ERP, CRM and
SCM systems, but it can be used independently of Workspace for
integrations between other applications, as well as for various data
conversion tasks.

For more information about Smilehouse OpenSyncro, visit
http://opensyncro.org

For more information about Smilehouse Oy, visit
http://www.smilehouse.com

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.


----------------------------------------------------------------------
2. Contents of the source code package
----------------------------------------------------------------------

This distribution should include the following:

README_source.txt               - This file

License.txt                     - OpenSyncro license text (GPL)

3rd_party_licenses/*            - License texts of 3rd party
                                  libraries used by OpenSyncro

Source/CHANGELOG.opensyncro     - Detailed version history
                                  document

Source/build.xml                - Apache Ant script for
                                  building the OpenSyncro
                                  web application

Source/web.xml                  - Web-app description file

Source/src/*                    - Java source files
	
Source/lib/*                    - 3rd party JAR files used by
                                  OpenSyncro

Source/files/*                  - Other files included in the
                                  OpenSyncro web application
                                  (graphics, style sheets,
                                   JavaScript code etc.)

Source/components/*             - Example properties files
                                  for building dynamic
                                  component JAR files

Installation/mysql/initialization/opensyncro_emptydb.sql
                                - MySQL dump file for creating
                                  an empty OpenSyncro database

Installation/mysql/updates/*	
                                - MySQL dump files for updating
                                  an existing OpenSyncro
                                  database

Installation/tomcat5/conf/catalina.policy
                                - Example security policy file
                                  for restricting OpenSyncro
                                  web application's privileges

Installation/tomcat5/shared/classes/smilehouse/conf/opensyncro.properties
                                - OpenSyncro configuration file
                                  to be copied under Tomcat
                                  installation directory

----------------------------------------------------------------------
3. How to proceed?
----------------------------------------------------------------------

NOTE: This is the source code package which provides files for
software developers to build and modify OpenSyncro web application as
well as create new OpenSyncro integration Pipe Components. If you wish
to install OpenSyncro and/or read the product's documentation, please
download the installation package from
http://opensyncro.org/download.shtml .

To build OpenSyncro web application from source code, you will need
to have Java 2 Standard Environment (J2SE) v1.5 (5.0) or newer
(http://java.sun.com/j2se/) and Apache Ant (http://ant.apache.org/)
installed. Both software are also necessary for building external,
dynamic OpenSyncro components. The build process also requires
servlet-2.3.jar (or newer) file from Tomcat. 


---------------------------------
3.1 Preparing to build OpenSyncro
---------------------------------

All files needed to compile and build OpenSyncro are located under
"Source/" directory, which is the root directory for build process.
Change your current directory to "Source/".

Prior to commencing build, Tomcat 5 installation path should be
edited into "build.xml" script file's "tomcat.dir" property. This
allows build script to find the servlet-2.3.jar package it needs for
compilation.


---------------------------------------
3.2 Building OpenSyncro web application
---------------------------------------

	To build the OpenSyncro web application you simply need to run
	command "ant" in the "Source/" directory. This will execute
	the default Ant target ("build-standalone") in file "build.xml".

	As a result, the "opensyncro.war" file will be created in
	"dist/" directory.


--------------------------------------------------------
3.3 Building a dynamic OpenSyncro component .jar package
--------------------------------------------------------

	Run Ant target "jarComponent" in build.xml:

		"ant jarComponent -Dcomponent.file={filename}",
		
	where {filename} is a jarComponent properties file.
	See "components/filecomponents.properties" for an example.
	
	The component .jar file will be created in "dist/" directory.

---

If you encounter a problem that is not documented on our web site
please report it to us: support@opensyncro.org . Bug reports and
patches are also very welcome.

----------------------------------------------------------------------
4. Contact
----------------------------------------------------------------------

www:    http://opensyncro.org
e-mail: support@opensyncro.org


Smilehouse Oy
Itälahdenkatu 22 A
00210 Helsinki
FINLAND

p. +358 - 9 - 25 122 10
f. +358 - 9 - 25 122 119

e-mail: info@smilehouse.com
www:    http://www.smilehouse.com
======================================================================
