#! /bin/bash

DIR=`pwd`
TIMESTEP=$8
LOGFILE=$DIR/test/sample-out.log
PIDS=

rm -rf test
mkdir test
touch $LOGFILE

# Wait for a regular expression to appear in a file.
# $1 is the log to check
# $2 is the regex to wait for
# $3 is the optional output frequency. Messages will be output every n sleeps. Default 1.
# $4 is the optional sleep time. Defaults to 1 second.
function waitFor {
    SLEEP_TIME=10
    FREQUENCY=1
    if [ ! -z "$3" ]; then
        FREQUENCY=$3
    fi
    if [ ! -z "$4" ]; then
        SLEEP_TIME=$4
    fi
    F=$FREQUENCY
    echo "Waiting for '$1' to exist..."
    while [[ ! -e $1 ]]; do
        if (( --F == 0 )); then
            echo "Still waiting for '$1' to exist..."
            F=$FREQUENCY
        fi
        sleep $SLEEP_TIME
    done
    echo "Waiting for '$2'..."
    while [ -z "`grep \"$2\" \"$1\"`" ]; do
        if (( --F == 0 )); then
            echo "Still waiting for '$2'..."
            F=$FREQUENCY
        fi
        sleep $SLEEP_TIME
    done
}

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

sleep 0.2
echo "Start agents."
 
java -Xms4g -Xmx8g -server -cp $TEAM_CP csu.LaunchAgents csu.agent.fb.FireBrigadeAgent*$1 csu.agent.fb.FireStationAgent*$2 csu.agent.pf.PoliceForceAgent*$3 csu.agent.pf.PoliceOfficeAgent*$4 csu.agent.at.AmbulanceTeamAgent*$5 csu.agent.at.AmbulanceCentreAgent*$6 -h $7 -precompute --loadabletypes.inspect.dir=jars --random.seed=1 2>&1 | tee "$LOGFILE" &
PIDS="$PIDS $!"
waitFor $LOGFILE "current time is $TIMESTEP" 2

sleep 10

kill $PIDS
