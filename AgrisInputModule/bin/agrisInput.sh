#!/bin/sh
# Script to generate XML AGRIS AP files without duplicates from other XML AGRIS AP files

thisfile=`which $0`
thisdir=`dirname $thisfile`
JAVA_HOME="/usr/local/jdk1.6.0_22"
PATH="/usr/local/jdk1.6.0_22/bin"
OPT_JARS="../lib/*.jar"
CP=""
for thing in $OPT_JARS
do
    if [ -f $thing ]; then       
        CP="${CP}:$thing"
    else if [ -d $thing ]; then  
        CP="$CP:$thing"
     fi
    fi
done
CP="$CP:."

if [ $# -lt 6 ]  # number of arguments passed to script
then
    echo "****************************************************"
    echo "Missing arguments. You need to specify following parameters: source directory, records year, country code, subcenter code, true or false to perform a duplicates removal based on titles, source XML format"
    echo "Example of usage: ./agrisInput.sh /work/agris/input/agrisap/ 2012 DJ 0 true agrisap"
    echo "****************************************************"
else
    exec java -classpath $CP -XX:-UseGCOverheadLimit -Xss64M -Xmx4096M org.fao.oekc.agris.inputRecords.MainInputRecords $1 $2 $3 $4 $5 $6 $7
    # exec java -classpath $CP -XX:-UseGCOverheadLimit -Xss64M -Xmx2048M org.fao.oekc.agris.inputRecords.ValidateDTD $1/OUTPUT
fi

exit