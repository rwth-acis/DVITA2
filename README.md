
#A Web-Based Dynamic Visual Topic Analytics Toolkit 

The following stepwise documentation has grown over time, not all of it might work out of the box. If you find incorrect info, please suggest improvements.

##Clone repositories
Do NOT clone this module, since it often points to outdated commits of the submodules. Instead, create a folder on your machine and clone all submodules individually there.

##Eclipse
If not already on your computer, download Eclipse IDE for Java Developers from http://www.eclipse.org/downloads/ 
These instructions were made with version 4.3.2 (Kepler), ran with JRE 7 on x86.

##GWT
Follow these instructions to install GWT related stuff: https://developers.google.com/eclipse/docs/getting_started

It is sufficient to install the "Google Web Toolkit SDK" and the "Google Plugin for Eclipse".
For Eclipse 4.3: 
*	Go to Help > Install New Software; add https://dl.google.com/eclipse/plugin/4.3 as a location ("Add" button).
*	Select "Google Plugin for Eclipse" and "Google Web Toolkit SDK" (under "SDKs") and finish the wizard

Now import the folder containing all the individual projects in your Eclipse workspace (`File > Import > General > Existing projects into workspace`)

##Set up D-VITA Database
The D-VITA Web app needs a config file dvita_config.txt residing in the app's WEB-INF folder --- Create a file dvita_config.txt (the example values below are for a mysql database, adapt them to your needs by replacing the fake values with real values):

```
server=honolulu
port=3306
databasename=DVitaConfig
user=your_username
password=your_password
type=1
openIDreturnUrl=http://mydvita.iscool/dvita
```
Note: type 1 = mysql, type 2 = DB2

Another Note: Since April the OpenID 2.0 auth implemented in D-VITA is not working any more with Google Accounts (). If you have time, you could fix this by upgrading to OpenID Connect.

This is a mysql dump of the tables in DVitaConfig database configured in the dvita_config.txt file. Adapt the DDL to your DBMS if needed.
```
CREATE TABLE `acl_roles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role` varchar(45) DEFAULT NULL,
  `operation` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `acl_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `operation` varchar(45) DEFAULT NULL,
  `target_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `analysis_requests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `status` varchar(150) DEFAULT NULL,
  `analysis_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `configurations_analysis` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dvita_configurations_rawdata_id` int(11) DEFAULT NULL,
  `dvita_database_connections_id` int(11) DEFAULT NULL,
  `granularity` int(11) DEFAULT NULL,
  `rangeStart` datetime DEFAULT NULL,
  `rangeEnd` datetime DEFAULT NULL,
  `numberTopics` int(11) DEFAULT NULL,
  `meta_title` varchar(100) DEFAULT NULL,
  `meta_description` varchar(250) DEFAULT NULL,
  `tablePrefix` varchar(50) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `configurations_analysis_representation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dvita_configurations_analysis_id` int(11) DEFAULT NULL,
  `title_analysis_overwrite` varchar(100) DEFAULT NULL,
  `description_analysis_overwrite` varchar(250) DEFAULT NULL,
  `title_rawdata_overwrite` varchar(100) DEFAULT NULL,
  `description_rawdata_overwrite` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `configurations_rawdata` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dvita_database_connections_id` int(11) DEFAULT NULL,
  `columnNameID` varchar(50) DEFAULT NULL,
  `columnNameDate` varchar(50) DEFAULT NULL,
  `columnNameContent` varchar(50) DEFAULT NULL,
  `columnNameTitle` varchar(50) DEFAULT NULL,
  `columnNameURL` varchar(50) DEFAULT NULL,
  `from_clause` varchar(200) DEFAULT NULL,
  `where_clause` varchar(200) DEFAULT NULL,
  `meta_title` varchar(100) DEFAULT NULL,
  `meta_description` varchar(250) DEFAULT NULL,
  `tablePrefix` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `database_connections` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `description` varchar(250) DEFAULT NULL,
  `server` varchar(100) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `databasename` varchar(50) DEFAULT NULL,
  `schema` varchar(50) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `user` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `error` (
  `iderror` int(11) NOT NULL AUTO_INCREMENT,
  `errorcol` longtext,
  PRIMARY KEY (`iderror`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `toolservers` (
  `id` int(11) NOT NULL,
  `titel` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `ipAdr` varchar(80) DEFAULT NULL,
  `portAdr` varchar(20) DEFAULT NULL
) DEFAULT CHARSET=utf8;

CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `role` varchar(45) DEFAULT NULL,
  `ident` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;
```

##Build
It might help to temporarily turn off automatic building. 

Check DVITA_WebApplication/war/WEB-INF/lib whether the DVITA_DBAccess.jar and DVITA_Commons.jar are present. If both or any one of the .jar is not present, follow the next two steps or if it is present skip the next two steps. 
* Build DVITA_Commons;DVITA_Commons.jar is generated in your DVITA_Commons project folder; copy the resulting JAR file to DVITA_DBAccess/lib, DVITA_Tools/lib, DVITA_ToolServer/lib, and DVITA_WebApplication/war/WEB-INF/lib. if the .jar file already exists then replace it with the new one. 
* Build DVITA_DBAccess; DVITA_DBAccess.jar is generated in your DVITA_Commons project folder; copy the resulting JAR file to DVITA_Tools/lib, and DVITA_WebApplication/war/WEB-INF/lib ; if the .jar file already exists then replace it with the new one. 

If there is anything in DVITA_WebApplication/gwt-UnitCache folder, remove all there is.

Check DVITA_WebApplication/war/WEB-INF/lib whether commons-lang3-3.3.2.jar is present. If not download it and add it.

Build DVITA_WebApplication: right click > Google > GWT Compile. Click Compile. After some time the console should put "Compilation succeeded". If not, it is about time to go berserk.

If there are any strange errors during runtime, it is likely because the exported JAR files of DBAccess and Commons in the lib folders of dependent projects are out of date. If this is the case, export those as JAR and copy them to the lib folders. This needs to be done after every change and rebuild of Commons and/or DBAccess. You could also write a build script that performs this automatically. So far no one got around to do that.

##Launching D-VITA web app locally
Put the dvita_config.txt in the DVITA_WebApplication/war/WEB-INF folder. Make sure to put correct username and database there.

Make sure Chrome is your default browser; will ask you to install GWT Developer extension if not installed yet.

Right click DVITA_WebApplication > Run As > Web Application. [If there are error messages about missing builders, go to the mentioned project, right click > Properties > Builders. Remove the invalid builders. Then repeat Run As command]. Click the URL that shows up. After some time a Select Dataset dialog should appear, and no error message boxes should appear.

OpenID login tweak for local deployment: After the OpenID provider redirects back, copy the parameter part of the URL and append it to the local deployment run URL 

##User management
The D-VITA backend features work with OpenID authentication. Each newly registered OpenID will be a registered user without any special permissions (role USER). Currently the permissions need to be set in the DVitaConfig database manually after signup. In the table users the column role can be adapted to roles defined in table acl_roles. The most permissive role currently is ADMIN.

Feel free to develop a user management UI into the control panel offered by the web app.

##Deploying a D-VITA Tool Server
Note: The tool server will run in the backend and listen to requests from the D-VITA control panel for building topic models. Each "tool" performs one step in the transition from the raw data set to the final topic model. We offer only a few essential tools that were needed for our reseearch. If you need more (e.g. uploading zipped documents, fetching from websites, etc.) you need to develop your processing tools.

(Note 2: The tools in the tool server don't run very reliably. Some work is needed there.)

Anyway: Export DTS.jar from DVITA_ToolServer

Create a DTS folder on some server and put the following there:
`dtsconfig.txt`, example (note ip/port need to be matched in the `DVitaConfig` database, table `toolservers`, fields `ipAdr` and `port`, so the GUI can connect to the toolserver):

```
toolfolder=dtstools
logfolder=.
executionchainsfolder=executionChains
ecRunningFolder=executionChainsRunning
port=54127
ip=137.226.232.999
```

... the `toolfolder` hosts the tools. Each tool needs a `manifest.xml` and a data folder with the actual tool. The set of tools can be found and compiled in DVITA_Tools project, at least the Java based ones.

Run DTS.jar as background process: 
`java -jar DTS.jar &`

##Deploying D-VITA on your server

You need a tomcat server running.

dvita_config.txt must be available in the WEB-INF folder under the application folder on the tomcat server, i.e.: 
`/[PATH TO TOMCAT]/webapps/[DEPLOY NAME]/WEB-INF/dvita_config.txt`

Compile:
* Rightclick the DVITA project > Google > GWT compile
* Don't change anything, project should be "DVITA_WebApplication"
* Click "Compile"

Generate WAR file:
* Rightclick the item "deploy.xml" in the project explorer > Run As > Ant Build
* Output should look something like this:
```
Buildfile: {WORKSPACEFOLDER}\DVITA_WebApplication\deploy.xml
buildwar:
    [war] Building war: {WORKSPACEFOLDER}\DVITA_WebApplication\DVita.war
deploy:
default:
BUILD SUCCESSFUL
Total time: XX seconds
```

Deploy to tomcat:
* Rename the generated WAR file to a name which you want for the application. Default file name is DVita.war
* Go to your Tomcat Manager App 
*	In the section "Deploy", in subsection "WAR file to deploy", upload your WAR file and click "Deploy"

Don't forget the OpenID return URL in dvita_config.txt needs to point to the deployed app.

##More Notes and Information

This branch of D-VITA is quite experimental. The only publicly deployed version is available at http://monet.informatik.rwth-aachen.de/DVita2, which is mostly used for research purposes, not stable, and you will not find many topic models there. But it includes the new control panel GUI and tool server backend to conveniently build the topic models.

A stable, earlier release of D-VITA is deployed at http://monet.informatik.rwth-aachen.de/DVita. This earlier release does not feature the control panel GUI, and the code is not yet publicly available. However, there are many publicly available topic models avaialable to explore.

Please go see http://dbis.rwth-aachen.de/cms/research/ACIS/D-VITA for more information and contact details for D-VITA.
