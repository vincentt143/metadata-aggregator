#!/bin/sh +x

#
# Config stuff
#

BUILD_DIR="sydma-web/target"
STAGING_DIR="staging"
WAR_FILE_LOCAL="sydma-web-*.war"
WAR_FILE_REMOTE="sydma-web.war"
WEBAPP_DIR="sydma-web"

INSTALLER_DIR="sydma-install"

cd "$(dirname $0)"

echo "\nCurrent directory = $PWD"

#
# Command line args
#

USAGE="\n$0: [-u <remote user>] [-h <remote host>] [-s] [-b]\n"

while getopts "h:u:ksb" options; do
  case $options in
    u) REMOTE_USER=$OPTARG;;
    h) REMOTE_CONFIG=$OPTARG;;
    s) SKIP_TEST=skip;;
    b) BUILD=build;;
    *) echo $USAGE
       exit 1;;
  esac
done


ENV_FILE="../../${INSTALLER_DIR}/config/${REMOTE_CONFIG}/env.sh"
if [ -x $ENV_FILE ]; then
  . $ENV_FILE
else
  echo "Please define a env.sh file for remote host in $ENV_FILE"
  exit 1
fi
if [ -z "$PROFILE" ]; then
  echo "Please configure PROFILE in env.sh (either dc2d or dc2f)"
  exit 1
fi
if [ -z "$REMOTE_HOST" ]; then
  echo "Please configure REMOTE_HOST in env.sh
fi
if [ -z "$REMOTE_USER" ]; then
  echo "Please configure REMOTE_USER in env.sh
fi


echo "\nDeploying to $REMOTE_USER@$REMOTE_HOST"

# Parse research subject code csv into sql
#echo "\nParsing research subject code csv"
#./parse_research_subject_code.pl
# NO LONGER GENERATED, the sql is committed to SVN 

#
# Build war
#

cd ../../

MUST_BUILD=
if [ ! -f "`ls sydma-web/target/sydma-web-*.war`" -o -n "$BUILD" ]; then
  MUST_BUILD=yes
else 
  if [ -f "`ls sydma-web/target/sydma-web-*.war`" ]; then
    CHECK_BUILD=`grep "ENVIRONMENT==${PROFILE}" sydma-web/target/sydma-web-*/WEB-INF/classes/META-INF/spring/applicationContext.xml`
    if [ -z "$CHECK_BUILD" ]; then
      echo "WARNING: >> Found build with wrong profile >> MUST REBUILD"
      MUST_BUILD=yes
    fi
  fi
fi

if [ -n "$MUST_BUILD" ]; then

  echo "\nBuilding sydma-web.war file"

  OUT=`mvn clean`
  
  if [ -z "$SKIP_TEST" ]; then
    echo mvn package -P ${PROFILE}
    OUT=`mvn package -P ${PROFILE}`
  else
    echo "WARNING - BUILDING WAR BUT SKIPPING TESTS PHASE"
    echo mvn package -DskipTests -P ${PROFILE}
    OUT=`mvn package -DskipTests -P ${PROFILE}`
  fi
  
  if [ $? -ne 0 ]
  then
    echo $OUT
    echo "\nError building war file."
    exit 1
  fi

fi

echo "Zipping all files"

rm -rf /tmp/staging
rm -f /tmp/staging.zip
mkdir /tmp/staging

TMP_STAGING_DIR=/tmp/staging


#
# Copy war & script to QA server
#
echo "\nPreparing war file `ls $BUILD_DIR/${WAR_FILE_LOCAL}` to remote server as ${WAR_FILE_REMOTE}"
echo cp `ls $BUILD_DIR/${WAR_FILE_LOCAL}` ${TMP_STAGING_DIR}/${WAR_FILE_REMOTE}
cp `ls $BUILD_DIR/${WAR_FILE_LOCAL}` ${TMP_STAGING_DIR}/${WAR_FILE_REMOTE}

echo "\nPreparing solr configuration files to remote server"
mkdir -p ${TMP_STAGING_DIR}/solr_conf
cp ${INSTALLER_DIR}/resource/solr_conf/* ${TMP_STAGING_DIR}/solr_conf


echo "\nPreparing tomcat6 configuration files to remote server"
mkdir -p ${TMP_STAGING_DIR}/tomcat6-conf
cp ${INSTALLER_DIR}/resource/tomcat6-conf/* ${TMP_STAGING_DIR}/tomcat6-conf

echo "\nPreparing script file to remote server"
cp ${INSTALLER_DIR}/bin/${REMOTE_SCRIPT_NAME}.sh ${TMP_STAGING_DIR}/qa-installer-remote.sh
chmod u+x ${TMP_STAGING_DIR}/qa-installer-remote.sh


echo "\nPreparing dms.home to remote server"
mkdir ${TMP_STAGING_DIR}/dms.home.2d
mkdir ${TMP_STAGING_DIR}/dms.home.2f

cp -rp ${INSTALLER_DIR}/resource/dms.home.2d/*  ${TMP_STAGING_DIR}/dms.home.2d
cp -rp ${INSTALLER_DIR}/resource/dms.home.2f/*  ${TMP_STAGING_DIR}/dms.home.2f


echo "\nPreparing sql files to remote server"
mkdir ${TMP_STAGING_DIR}/sql
cp ${INSTALLER_DIR}/sql/* ${TMP_STAGING_DIR}/sql

echo "\nPreparing sql schemas to remote server"
cp -r ${INSTALLER_DIR}/resource/schemas ${TMP_STAGING_DIR}/dms.home.2d/
cp -r ${INSTALLER_DIR}/resource/schemas ${TMP_STAGING_DIR}/dms.home.2f/

echo "Clean up before zipping"
find ${TMP_STAGING_DIR}/ -name .svn -type d -exec rm -rf {} \;

# Zip and Copy all
cd /tmp && zip -r staging staging && cd -
echo scp /tmp/staging.zip ${REMOTE_USER}@${REMOTE_HOST}:~
scp /tmp/staging.zip ${REMOTE_USER}@${REMOTE_HOST}:~
if [ $? -ne 0 ]
then
  echo "\nError copying ZIP file."
  exit 1
fi

#
# Run remote script
#
echo "\nRunning installer in remote machine"
ssh -t ${REMOTE_USER}@${REMOTE_HOST} "unzip staging.zip && \
     sudo ./staging/qa-installer-remote.sh -h '$HOME' \
                                      -w '$WAR_FILE_REMOTE' \
                                      -a '$WEBAPP_DIR' \
                                      -j '$JAVA_HOME' \
                                      -t '$TOMCAT6_HOME' \
                                      -u '$DB_ROOT_USER' \
                                      -p '$DB_ROOT_PASSWORD'"
if [ $? -ne 0 ]
then
  echo "\nThere were errors running the installer."
  exit 1
fi

exit 0
