#!/bin/sh

if [ $# -ne "7" ]; then
	echo "Usage:"
	echo " $0 [FB] [FS] [PF] [PO] [AT] [AC] [HOST]"
	echo " Use n for \"all agents\""
	echo " example: $0 n n n n n n 127.0.0.1"
	exit 1
fi

DIR=`pwd`
TEAM_CP="bin:\
jars/clear.jar:\
jars/collapse.jar:\
jars/commons-logging-1.1.1.jar:\
jars/dom4j.jar:\
jars/gis2.jar:\
jars/handy.jar:\
jars/human.jar:\
jars/ignition.jar:\
jars/javaGeom-0.9.0.jar:\
jars/jaxen-1.1.1.jar:\
jars/jcommon-1.0.16.jar:\
jars/jfreechart-1.0.13.jar:\
jars/jscience-4.3.jar:\
jars/jsi-1.0b2p1.jar:\
jars/jts-1.11.jar:\
jars/junit-4.5.jar:\
jars/kernel.jar:\
jars/log4j-1.2.15.jar:\
jars/maps.jar:\
jars/misc.jar:\
jars/rescuecore2.jar:\
jars/resq-fire.jar:\
jars/sample.jar:\
jars/standard.jar:\
jars/traffic3.jar:\
jars/trove-0.1.8.jar:\
jars/uncommons-maths-1.2.jar:\
jars/rescuecore.jar:\
jars/xml-0.0.6.jar"

echo "*****************************************************************"
echo "*****************************************************************"
echo "**                                                             **"
echo "**                                                             **"
echo "**            ****             ***         **        **        **"
echo "**          ********         *******       **        **        **"
echo "**         ***    ***       **     **      **        **        **"
echo "**         **      **      **              **        **        **"
echo "**         **      **      **              **        **        **"
echo "**         **               **             **        **        **"
echo "**         **                **            **        **        **"
echo "**         **                  ***         **        **        **"
echo "**         **      **             **       **        **        **"
echo "**         **      **              **      **        **        **"
echo "**         **      **              **      **        **        **"
echo "**         ***    ***      **     **        **      **         **"
echo "**          ********        *******          ********          **"
echo "**            ****            ***              ****            **"
echo "**                                                             **"
echo "**                                                             **"
echo "**               Central       South       University          **"
echo "**        The production team:       CSU_Yunlu                 **"
echo "**        Name  of  our  team:       Accumulate steadily       **"
echo "**        Copyright 2009-2015:       CSU_Yunlu                 **"
echo "**                                                             **"
echo "*****************************************************************"
echo "*****************************************************************"
sleep 0.2
echo "Start agents."

rm -rf test
mkdir test
 
java -Xms4g -Xmx8g -server -cp $TEAM_CP csu.LaunchAgents csu.agent.fb.FireBrigadeAgent*$1 csu.agent.fb.FireStationAgent*$2 csu.agent.pf.PoliceForceAgent*$3 csu.agent.pf.PoliceOfficeAgent*$4 csu.agent.at.AmbulanceTeamAgent*$5 csu.agent.at.AmbulanceCentreAgent*$6 -h $7 -precompute --loadabletypes.inspect.dir=jars --random.seed=1 | tee "$DIR/test/sample-out.log"
