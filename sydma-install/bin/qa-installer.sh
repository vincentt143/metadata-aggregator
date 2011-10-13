#!/bin/sh +x

#
# Config stuff
#

REMOTE_USER="devel"
REMOTE_HOST="gsw1-sydma01-vm"

BUILD_DIR="sydma-web/target"
STAGING_DIR="staging"
WAR_FILE_LOCAL="sydma-web-*.war"
WAR_FILE_REMOTE="sydma-web.war"
WEBAPP_DIR="sydma-web"

TUNNEL_BUILD_DIR="sydma-httptunnel/target"
TUNNEL_WAR_FILE_LOCAL="sydma-httptunnel-*.war"
TUNNEL_WAR_FILE_REMOTE="sydma-httptunnel.war"

INSTALLER_DIR="sydma-install"

cd "$(dirname $0)"

echo "\nCurrent directory = $PWD"

#
# Command line args
#

USAGE="\n$0: [-u <remote user>] [-h <remote host>]\n"

while getopts "h:u:" options; do
  case $options in
    u) REMOTE_USER=$OPTARG;;
    h) REMOTE_HOST=$OPTARG;;
    *) echo $USAGE
       exit 1;;
  esac
done

if [ "${REMOTE-HOST:0:4}" = "ca1-" ]; then
  echo "USING SPECIFIC WAR NAME FOR DC2D (sydae-web.war)"
  WAR_FILE_REMOTE="sydae-web.war"
  WEBAPP_DIR="sydae-web"
fi

echo "\nDeploying to $REMOTE_USER@$REMOTE_HOST"

# Check dms config home exists
if [ -d "../../${INSTALLER_DIR}/config/${REMOTE_HOST}" ]
then
	echo "DMS home taken from ${INSTALLER_DIR}/config/${REMOTE_HOST}"
else
	echo "Cannot find DMS home configuration at ${INSTALLER_DIR}/config/${REMOTE_HOST}"
	exit 1
fi

# Parse research subject code csv into sql
#echo "\nParsing research subject code csv"
#./parse_research_subject_code.pl
# NO LONGER GENERATED, the sql is committed to SVN 

#
# Build war
#

cd ../../

echo "\nBuilding war file"
OUT=`mvn clean`

OUT=`mvn package -P ${REMOTE_HOST}`

if [ $? -ne 0 ]
then
  echo $OUT
  echo "\nError building war file."
  exit 1
fi

#
# Copy war & script to QA server
#
echo "\nCopying war file to remote server"
scp ${BUILD_DIR}/${WAR_FILE_LOCAL} ${REMOTE_USER}@${REMOTE_HOST}:${STAGING_DIR}/${WAR_FILE_REMOTE}
if [ $? -ne 0 ]
then
  echo "\nError copying war file."
  exit 1
fi

echo "\nCopying tunnel war file to remote server"
scp ${TUNNEL_BUILD_DIR}/${TUNNEL_WAR_FILE_LOCAL} ${REMOTE_USER}@${REMOTE_HOST}:${STAGING_DIR}/${TUNNEL_WAR_FILE_REMOTE}
if [ $? -ne 0 ]
then
  echo "\nError copying war file."
  exit 1
fi


echo "\nCopying script file to remote server"
sed "s/__WAR_FILE__/$WAR_FILE_REMOTE/g
s/__WEBAPP_DIR__/$WEBAPP_DIR/g" ${INSTALLER_DIR}/bin/qa-installer-remote.sh | ssh ${REMOTE_USER}@${REMOTE_HOST} "cat > ${STAGING_DIR}/qa-installer-remote.sh"
if [ $? -ne 0 ]
then
  echo "\nError copying script file."
  exit 1
fi
ssh ${REMOTE_USER}@${REMOTE_HOST} "chmod u+x ${STAGING_DIR}/qa-installer-remote.sh"

echo "\nCopying dms home directory to remote server"
ssh ${REMOTE_USER}@${REMOTE_HOST}  rm -rf ${STAGING_DIR}/dms.home
scp -rp ${INSTALLER_DIR}/config/${REMOTE_HOST}/  ${REMOTE_USER}@${REMOTE_HOST}:${STAGING_DIR}/dms.home
if [ $? -ne 0 ]
then
  echo "\nError copying dms config file."
  exit 1
fi

echo "\nCopying sql file to remote server"
scp ${INSTALLER_DIR}/sql/create_users.sql ${REMOTE_USER}@${REMOTE_HOST}:${STAGING_DIR}/
if [ $? -ne 0 ]
then
  echo "\nError copying sql file."
  exit 1
fi

scp ${INSTALLER_DIR}/sql/create_access_rights.sql ${REMOTE_USER}@${REMOTE_HOST}:${STAGING_DIR}/
if [ $? -ne 0 ]
then
  echo "\nError copying sql file."
  exit 1
fi

scp ${INSTALLER_DIR}/sql/create_buildings.sql ${REMOTE_USER}@${REMOTE_HOST}:${STAGING_DIR}/
if [ $? -ne 0 ]
then
  echo "\nError copying sql file."
  exit 1
fi


scp ${INSTALLER_DIR}/sql/create_research_subject_code.sql ${REMOTE_USER}@${REMOTE_HOST}:${STAGING_DIR}/
if [ $? -ne 0 ]
then
  echo "\nError copying sql file."
  exit 1
fi

scp ${INSTALLER_DIR}/sql/create_dataset_schema.sql ${REMOTE_USER}@${REMOTE_HOST}:${STAGING_DIR}/
if [ $? -ne 0 ]
then
  echo "\nError copying sql file."
  exit 1
fi

# Copy the schemas
scp -r ${INSTALLER_DIR}/resource/schemas ${REMOTE_USER}@${REMOTE_HOST}:${STAGING_DIR}/dms.home/
if [ $? -ne 0 ]
then
  echo "\nError copying db instance schema directory."
  exit 1
fi

#
# Run remote script
#
echo "\nRunning installer"
ssh ${REMOTE_USER}@${REMOTE_HOST} 'staging/qa-installer-remote.sh'
if [ $? -ne 0 ]
then
  echo "\nThere were errors running the installer."
  exit 1
fi

exit 0
