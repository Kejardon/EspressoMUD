REM Make sure you are building with java v1.6 or higher
REM "C:\Program Files (x86)\Java\jdk1.6.0_26\bin\javac" -g -nowarn -deprecation com/planet_ink/coffee_mud/
SET Java_Home="C:\Program Files\Java\jdk1.7.0_04
set CLASSPATH=.;%Java_Home%\lib\dt.jar";%Java_Home%\lib\tools.jar";.\lib\js.jar;.\lib\jzlib.jar
REM SET JAVACPATH=%Java_Home%\bin\javac" -g -nowarn -deprecation -encoding ISO-8859-1
SET JAVACPATH=javac -g -nowarn -deprecation -encoding ISO-8859-1

IF "%1" == "docs" GOTO :DOCS

%JAVACPATH% com/planet_ink/coffee_mud/core/interfaces/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Effects/*.java
%JAVACPATH% com/planet_ink/coffee_mud/application/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Areas/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Behaviors/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Commands/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Common/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Common/Closeable/*.java
%JAVACPATH% com/planet_ink/coffee_mud/core/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Exits/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Libraries/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Locales/*.java
%JAVACPATH% com/planet_ink/coffee_mud/MOBS/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Items/Basic/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Races/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Races/Genders/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Effects/Languages/*.java
%JAVACPATH% com/planet_ink/coffee_mud/core/database/*.java
%JAVACPATH% com/planet_ink/coffee_mud/core/exceptions/*.java
%JAVACPATH% com/planet_ink/coffee_mud/core/http/*.java
%JAVACPATH% com/planet_ink/coffee_mud/core/threads/*.java
REM %JAVACPATH% com/planet_ink/siplet/applet/*.java
REM %JAVACPATH% com/planet_ink/siplet/support/*.java

GOTO :FINISH

:DOCS

%Java_Home%\bin\javadoc -d .\docs -J-Xmx256m -subpackages com.planet_ink.coffee_mud 

:FINISH
