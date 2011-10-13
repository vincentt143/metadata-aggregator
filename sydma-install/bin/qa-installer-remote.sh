#!/bin/sh
set +x

#
# Config stuff
#

DB_ROOT_USER="root"
DB_ROOT_PASSWORD="root"

DB_NAME="sydma"
DB_USER="sydma"
DB_PASSWORD="sydma"

WAR_FILE="__WAR_FILE__"
WEBAPP_DIR="__WEBAPP_DIR__"

TUNNEL_WAR_FILE="sydma-httptunnel.war"
TUNNEL_WEBAPP_DIR="sydma-httptunnel"


CREATE_DB_SQL="CREATE DATABASE $DB_NAME CHARACTER SET UTF8;"
CREATE_USER_SQL="CREATE USER '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';"
USER_PRIVS_SQL="GRANT all privileges on ${DB_NAME}.* to '$DB_USER'@'localhost' with grant option;"

DROP_DB_SQL="DROP DATABASE IF EXISTS $DB_NAME;"
DROP_USER_SQL="DROP USER '$DB_USER'@'localhost';"

cd ${HOME}/staging

#
# Check for dependencies
#

if [ -z "$JAVA_HOME" ]
then
    /bin/echo "JAVA_HOME is not set."
    exit 1
fi

if [ -z "$CATALINA_HOME" ]
then
    /bin/echo "CATALINA_HOME is not set."
    exit 1
fi

if [ ! -s ${WAR_FILE} ]
then
    /bin/echo "The war file is not available."
    exit 1
fi

#
# Stop tomcat
#

TOMCAT_PS=`/bin/ps -eo pid,args | grep $CATALINA_HOME | grep java`

if  [ -n "$TOMCAT_PS" ]
then
    /bin/echo "Stopping Tomcat..."
    cd ${CATALINA_HOME}/bin
    OUT=`./shutdown.sh`
 
    /bin/sleep 5s
    TOMCAT_PS=`/bin/ps -eo pid,args | grep $CATALINA_HOME | grep java`

    if [ -n "$TOMCAT_PS" ]
    then
        /bin/echo "Killing Tomcat..."
        PID=`echo $TOMCAT_PS | /usr/bin/cut -f1 -d" "`
        /usr/bin/kill -9 $PID
        /bin/sleep 5s
    fi

    TOMCAT_PS=`/bin/ps -eo pid,args | grep $CATALINA_HOME | grep java`

    if [ -n "$TOMCAT_PS" ]
    then
        /bin/echo "Couldnt stop Tomcat, continuing anyway..."
    fi
else
    /bin/echo "Tomcat is not running..."
fi

#
# Create database
#

MYSQL_PS=`/bin/ps -eo pid,args | grep [m]ysqld`
if [ -z "$MYSQL_PS" ]
then
    /bin/echo "mysql server is not running, ignoring database setup..."
else
    /bin/echo "Dropping database..."

    OUT=`/usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD -e "$DROP_DB_SQL"`
    if [ -n "$OUT" ]
    then
        /bin/echo "Problem dropping database:\n$OUT"
        exit 1
    fi

    OUT=`/usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD -e "$DROP_USER_SQL"`
    if [ -n "$OUT" ]
    then
        /bin/echo "Problem dropping user:\n$OUT"
        /bin/echo "Continuing..."
    fi

    /bin/echo "Creating database..."

    OUT=`/usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD -e "$CREATE_DB_SQL"`
    if [ -n "$OUT" ]
    then
        /bin/echo "Problem creating database:\n$OUT"
        exit 1
    fi

    OUT=`/usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD -e "$CREATE_USER_SQL"`
    if [ -n "$OUT" ]
    then
        /bin/echo "Problem creating user:\n$OUT"
        exit 1
    fi

    OUT=`/usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD -e "$USER_PRIVS_SQL"`
    if [ -n "$OUT" ]
    then
        /bin/echo "Problem setting user privileges:\n$OUT"
        exit 1
    fi
fi

#
# Install new war file
#

if [ -d ${CATALINA_HOME}/webapps ]
then
    /bin/echo "Deploying war file..."
    /bin/cp -p ${HOME}/staging/${WAR_FILE} ${CATALINA_HOME}/webapps/
    /bin/cp -p ${HOME}/staging/${TUNNEL_WAR_FILE} ${CATALINA_HOME}/webapps/

    cd ${CATALINA_HOME}/webapps
    if [ -d ${WEBAPP_DIR} ]
    then
        /bin/rm -rf ${WEBAPP_DIR}
    fi
    if [ -d ${TUNNEL_WEBAPP_DIR} ]
    then
        /bin/rm -rf ${TUNNEL_WEBAPP_DIR}
    fi
else
    /bin/echo "No tomcat webapps folder..."
fi

#
# Start Tomcat
#

TOMCAT_PS=`/bin/ps -eo pid,args | grep $CATALINA_HOME | grep java`

if  [ -n "$TOMCAT_PS" ]
then
    /bin/echo "Tomcat already running..."
else
    /bin/echo "Starting Tomcat..."
    cd ${CATALINA_HOME}/bin
    export CATALINA_OPTS="-Ddms.config.home=/home/devel/staging/dms.home -Ddms.worker.profile=worker-external.xml -Xmx1024m -XX:MaxPermSize=256m "
    OUT=`./startup.sh`
fi

#
# Run SQL script to create users & roles
#

/bin/echo "Creating users and roles..."
cd ${HOME}/staging
sleep 30
OUT=`/usr/bin/mysql sydma -u $DB_USER -p$DB_PASSWORD <create_users.sql`
if [ -n "$OUT" ]
then
    /bin/echo "Problem creating users and roles :\n$OUT"
    /bin/echo "Try running the script manually when Tomcat has fully started!"
    exit 1
fi
OUT=`/usr/bin/mysql sydma -u $DB_USER -p$DB_PASSWORD <create_access_rights.sql`
if [ -n "$OUT" ]
then
    /bin/echo "Problem creating access roles :\n$OUT"
    /bin/echo "Try running the script manually when Tomcat has fully started!"
    exit 1
fi
OUT=`/usr/bin/mysql sydma -u $DB_USER -p$DB_PASSWORD <create_buildings.sql`
if [ -n "$OUT" ]
then
    /bin/echo "Problem creating buildings :\n$OUT"
    /bin/echo "Try running the script manually when Tomcat has fully started!"
    exit 1
fi
OUT=`/usr/bin/mysql sydma -u $DB_USER -p$DB_PASSWORD <create_research_subject_code.sql`
if [ -n "$OUT" ]
then
    /bin/echo "Problem creating research subject code:\n$OUT"
    /bin/echo "Try running the script manually when Tomcat has fully started!"
    exit 1
fi
OUT=`/usr/bin/mysql sydma -u $DB_USER -p$DB_PASSWORD <create_dataset_schema.sql`
if [ -n "$OUT" ]
then
    /bin/echo "Problem creating dataset database schema :\n$OUT"
    /bin/echo "Try running the script manually when Tomcat has fully started!"
    exit 1
fi
#
# End
#

exit 0
