#!/bin/sh
set +x

USAGE="installer-remote-2f.sh -h home -w war -a appname -k -j java_home -t tomcat_dir -u db_root_user -p db_root_password"
while getopts "h:w:a:kj:t:u:p:" options; do
  case $options in
    h) HOME_DIR=$OPTARG;;
    w) WAR_FILE=$OPTARG;;
    a) WEBAPP_DIR=$OPTARG;;
    k) DB_KEEP=keep;;
    j) JAVA_HOME=$OPTARG;;
    t) TOMCAT6_HOME=$OPTARG;;
    u) DB_ROOT_USER=$OPTARG;;
    p) DB_ROOT_PASSWORD=$OPTARG;;
    *) echo $USAGE
       exit 1;;
  esac
done


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

	cd ${HOME_DIR}/staging/sql

        echo "Creating database tables"
	OUT=`/usr/bin/mysql ${DB_NAME} -u $DB_USER -p$DB_PASSWORD <create.sql`
	if [ -n "$OUT" ]
	then
	    /bin/echo "Problem creating tables :\n$OUT"
	    exit 1
	fi


	/bin/echo "Creating users and roles for DC2F..."
	OUT=`/usr/bin/mysql ${DB_NAME} -u $DB_USER -p$DB_PASSWORD <create_roles_f.sql`
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
  if [ -f "${TOMCAT6_HOME}/dms.home/worker.properties" ]; then
      workerPath=`cat ${TOMCAT6_HOME}/dms.home/worker.properties | grep 'dms.wn.localRootPath' | awk -F '=' '{print $2}'`
      filePath=`unzip -c ${HOME_DIR}/staging/${WAR_FILE} WEB-INF/classes/META-INF/spring/fileServer.properties | grep 'sydma.localFileServer' | awk -F '=' '{print $2}'`
      if [ -n "${workerPath}" -a -n "${filePath}" ]; then
        echo "REMOVING FILES/FOLDERS FROM ${workerPath}/${filePath}"
        rm -rf ${workerPath}/${filePath}/*
      fi
  fi
}

#
# -- main --
#

#
# Config stuff
#


DB_NAME="sydma"
DB_USER="sydma"
DB_PASSWORD="sydma"

CREATE_DB_SQL="CREATE DATABASE $DB_NAME CHARACTER SET UTF8;"
CREATE_USER_SQL="CREATE USER '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';"
# Access to all database so user can also create and grant on created databases
USER_PRIVS_SQL="GRANT all privileges on ${DB_NAME}.* to '$DB_USER'@'localhost' with grant option;" 
DROP_DB_SQL="DROP DATABASE IF EXISTS $DB_NAME;"
DROP_USER_SQL="DROP USER '$DB_USER'@'localhost';"

cd ${HOME_DIR}/staging

#
# Check for dependencies
#

if [ -z "$JAVA_HOME" ]
then
    /bin/echo "JAVA_HOME is not set."
    exit 1
fi

if [ -z "$TOMCAT6_HOME" ]
then
    /bin/echo "TOMCAT6_HOME is not set."
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

service tomcat6 stop

#
# Create database
#

service mysqld start

if [ -z "$DB_KEEP" ];
then
    /bin/echo "WIPING OUR CURRENT DATA..."
    databaseCleanup
    workerCleanup
    /bin/echo "POPULATING STANDARD DATA..."
    databasePopulate
else
    /bin/echo "KEEPING CURRENT DATA ..."
fi    

#
# Install new war file
#

if [ -d ${TOMCAT6_HOME}/webapps ]
then
    /bin/echo "Deploying war file..."
    /bin/cp -p ${HOME_DIR}/staging/${WAR_FILE} ${TOMCAT6_HOME}/webapps/
    chown tomcat ${TOMCAT6_HOME}/webapps/${WAR_FILE}

    cd ${TOMCAT6_HOME}/webapps
    if [ -d ${WEBAPP_DIR} ]
    then
        /bin/rm -rf ${WEBAPP_DIR}
    fi
else
    /bin/echo "No tomcat webapps folder..."
fi

#
# copy tomcat6 configuration
#
echo "Copying new tomcat6 configuration"
cp ${HOME_DIR}/staging/tomcat6-conf/* ${TOMCAT6_HOME}/conf
chown -R tomcat ${TOMCAT6_HOME}/conf

echo "Copying system configuration"
mkdir -p ${TOMCAT6_HOME}/mda-data/dms.home
mkdir -p ${TOMCAT6_HOME}/mda-data/solr/conf
mkdir -p ${TOMCAT6_HOME}/mda-data/rifcs
cp -r ${HOME_DIR}/staging/dms.home.2d/* ${TOMCAT6_HOME}/mda-data/dms.home
cp -r ${HOME_DIR}/staging/solr_conf/* ${TOMCAT6_HOME}/mda-data/solr/conf
chown -R tomcat ${TOMCAT6_HOME}/mda-data

#
# Start Tomcat
#

service tomcat6 start

exit 0
