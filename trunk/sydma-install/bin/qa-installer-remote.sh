#!/bin/sh
set +x

WAR_FILE=$1
WEBAPP_DIR=$2
DB_KEEP=$3
if [ -n "$4" ]; then
  JAVA_HOME=$4
fi
if [ -n "$5" ]; then
  CATALINA_HOME=$5
fi




#
## clean up the custom database instances and users
#
function dropUserIfOurs {
    dbuser=$1
    case "$dbuser" in
        sydma_user_*)
            OUT=`/usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD -B -e "DROP USER '$dbuser'"`
            if [ $? -ne 0 ]
            then
                /bin/echo "Problem dropping user $dbuser:\n$OUT"
                exit 1
            fi 
            /bin/echo -n "$dbuser "
            ;;
    esac
}

function dropDbIfOurs {
    dbname=$1
    case "$dbname" in
        dataset_*)
            OUT=`/usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD -B -e "DROP DATABASE $dbname"`
            if [ $? -ne 0 ]
            then
                /bin/echo "Problem dropping database instance $dbname:\n$OUT"
                exit 1
            else
                /bin/echo -n "$dbname "
            fi 
            ;;
   esac
}


function databaseCleanup {
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
    
    OUT=`/usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD -e "$USER_DB_INSTANCE_PRIVS_SQL1"`
    if [ -n "$OUT" ]
    then
        /bin/echo "Problem setting user dbinstance privileges:\n$OUT"
        exit 1
    fi
    
    OUT=`/usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD -e "$USER_DB_INSTANCE_PRIVS_SQL2"`
    if [ -n "$OUT" ]
    then
        /bin/echo "Problem setting user dbinstance privileges:\n$OUT"
        exit 1
    fi

    CLEANUP_FOUND=$(
        /usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD --raw -B -e "select distinct User from mysql.user" | while read dbuser; do
            dropUserIfOurs $dbuser
    done
        )
    if [ -z "$CLEANUP_FOUND" ]
    then
        echo "Db users clean-up: Nothing to do"
    else
        echo "Dropped users $CLEANUP_FOUND"
    fi

    CLEANUP_FOUND=$(
        /usr/bin/mysql -u $DB_ROOT_USER -p$DB_ROOT_PASSWORD --raw -B -e "SHOW DATABASES" | while read dbname; do
           dropDbIfOurs $dbname
        done
        )
    if [ -z "$CLEANUP_FOUND" ]
    then
        echo "Db instances clean-up: Nothing to do"
    else
        echo "Dropped $CLEANUP_FOUND"
    fi

}

function databasePopulate {
	#
	# Run SQL script to create users & roles
	#

	/bin/echo "Creating users and roles..."
	cd ${HOME}/staging
	sleep 30
	OUT=`/usr/bin/mysql ${DB_NAME} -u $DB_USER -p$DB_PASSWORD <create_users.sql`
	if [ -n "$OUT" ]
	then
	    /bin/echo "Problem creating users and roles :\n$OUT"
	    /bin/echo "Try running the script manually when Tomcat has fully started!"
	    exit 1
	fi
	OUT=`/usr/bin/mysql ${DB_NAME} -u $DB_USER -p$DB_PASSWORD <create_access_rights.sql`
	if [ -n "$OUT" ]
	then
	    /bin/echo "Problem creating access roles :\n$OUT"
	    /bin/echo "Try running the script manually when Tomcat has fully started!"
	    exit 1
	fi
	OUT=`/usr/bin/mysql ${DB_NAME} -u $DB_USER -p$DB_PASSWORD <create_buildings.sql`
	if [ -n "$OUT" ]
	then
	    /bin/echo "Problem creating buildings :\n$OUT"
	    /bin/echo "Try running the script manually when Tomcat has fully started!"
	    exit 1
	fi
	OUT=`/usr/bin/mysql ${DB_NAME} -u $DB_USER -p$DB_PASSWORD <create_research_subject_code.sql`
	if [ -n "$OUT" ]
	then
	    /bin/echo "Problem creating research subject code:\n$OUT"
	    /bin/echo "Try running the script manually when Tomcat has fully started!"
	    exit 1
	fi
	OUT=`/usr/bin/mysql ${DB_NAME} -u $DB_USER -p$DB_PASSWORD <create_dataset_schema.sql`
	if [ -n "$OUT" ]
	then
	    /bin/echo "Problem creating dataset database schema :\n$OUT"
	    /bin/echo "Try running the script manually when Tomcat has fully started!"
	    exit 1
	fi
	OUT=`/usr/bin/mysql ${DB_NAME} -u $DB_USER -p$DB_PASSWORD <create_vocabulary.sql`
	if [ -n "$OUT" ]
	then
	    /bin/echo "Problem creating vocabulary templates :\n$OUT"
	    /bin/echo "Try running the script manually when Tomcat has fully started!"
	    exit 1
	fi
	#
	# End
	#
}

function workerCleanup {
  # something
  workerPath=`cat ${HOME}/staging/dms.home/worker.properties | grep 'dms.wn.localRootPath' | awk -F '=' '{print $2}'`
  filePath=`unzip -c ${HOME}/staging/${WAR_FILE} WEB-INF/classes/META-INF/spring/fileServer.properties | grep 'sydma.localFileServer' | awk -F '=' '{print $2}'`
  if [ -n "${workerPath}" -a -n "${filePath}" ]; then
    echo "REMOVING FILES/FOLDERS FROM ${workerPath}/${filePath}"
    rm -rf ${workerPath}/${filePath}/*
  fi
}


#
# Config stuff
#

DB_ROOT_USER="root"
DB_ROOT_PASSWORD="root"

DB_NAME="sydma"
DB_USER="sydma"
DB_PASSWORD="sydma"

CREATE_DB_SQL="CREATE DATABASE $DB_NAME CHARACTER SET UTF8;"
CREATE_USER_SQL="CREATE USER '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';"
# Access to all database so user can also create and grant on created databases
USER_PRIVS_SQL="GRANT all privileges on ${DB_NAME}.* to '$DB_USER'@'localhost' with grant option;" 
USER_DB_INSTANCE_PRIVS_SQL1="GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, 
RELOAD, SHOW DATABASES, GRANT OPTION, CREATE USER, REFERENCES, ALTER, CREATE TEMPORARY TABLES, EXECUTE
ON *.* TO '$DB_USER'@'localhost' with grant option;"
USER_DB_INSTANCE_PRIVS_SQL2="GRANT CREATE ROUTINE, ALTER ROUTINE, TRIGGER, INDEX, CREATE VIEW, SHOW VIEW, EVENT, LOCK TABLES 
ON *.* TO '$DB_USER'@'localhost' with grant option;"

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
        /bin/echo "Couldn't stop Tomcat, continuing anyway..."
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
    if [ -z "$DB_KEEP" ];
    then
        /bin/echo "WIPING OUR CURRENT DATA..."
        databaseCleanup
        workerCleanup
    else
        /bin/echo "KEEPING CURRENT DATA ..."
    fi    
fi

#
# Install new war file
#

if [ -d ${CATALINA_HOME}/webapps ]
then
    /bin/echo "Deploying war file..."
    /bin/cp -p ${HOME}/staging/${WAR_FILE} ${CATALINA_HOME}/webapps/

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

if [ -z "$DB_KEEP" ];
then
    /bin/echo "POPULATING STANDARD DATA..."
    databasePopulate
fi    

exit 0
