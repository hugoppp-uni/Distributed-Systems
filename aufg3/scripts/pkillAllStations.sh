#!/bin/bash
#
# (c) by H. Schulz 2013-16
# This programme is provided 'As-is', without any guarantee of any kind, implied or otherwise and is wholly unsupported.
# You may use and modify it as long as you state the above copyright.

#
# Stops all stations started by startStations.sh
# 

########################################################################################################
# TODO: Enter your station's start command or the significant part thereof.
#
# Example: stationCmd="java aufgabe4.MyStation"
########################################################################################################
stationCmd="/home/hugop/.jdks/openjdk-19/bin/java -javaagent:/home/hugop/.local/share/JetBrains/Toolbox/apps/IDEA-U/ch-0/223.8214.52/lib/idea_rt.jar=44437:/home/hugop/.local/share/JetBrains/Toolbox/apps/IDEA-U/ch-0/223.8214.52/bin -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /home/hugop/git/vs-ws22/aufg3/target/classes org.example.Main"

########################################################################################################
# TODO: Enter data source programme with full path, but WITHOUT parameters 
#
# Example:    dataSource="~/somewhere/Vessel3"
#         or  dataSource="java -cp . vessel3.Vessel"
########################################################################################################
dataSource="/home/hugop/git/vs-ws22/aufg3/src/main/java/org/example/Vessel3"


if [ "$1" = "--help" ]; then
	echo "Script that terminates datasources and stations."
	echo "The following commands will be issued:"
	echo 
	echo "pkill -f \"$dataSource\""
	echo "pkill -f \"$stationCmd\""
else
	if [ "$dataSource" = "" -o "$stationCmd" = "" ]; then
		echo "One of the variables has not been set."
		exit 1
	else
		pkill -f "$dataSource"
		pkill -f "$stationCmd"
	fi
fi