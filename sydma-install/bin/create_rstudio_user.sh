#!/bin/sh

USAGE="\n$0: -u <rstudio user name> -p <user password>\n"

while getopts "u:p:" options; do
  case $options in
    u) USERNAME=$OPTARG;;
    p) PASSWORD=$OPTARG;;
    *) echo $USAGE
       exit 1;;
  esac
done

HOME_DIR=/rhome/${USERNAME}
REMOTE_SSH_DIR=${HOME_DIR}/.ssh
REMOTE_SSH_AUTH_KEYS=${REMOTE_SSH_DIR}/authorized_keys
REMOTE_MY_DATA_DIR=${HOME_DIR}/MyData

DC2D_PUBLIC_KEY=/home/devel/.ssh/authorized_keys

# Create user and set password
sudo useradd --home-dir ${HOME_DIR} ${USERNAME}
echo ${PASSWORD} | sudo passwd --stdin ${USERNAME}

# Create data directory
sudo mkdir ${REMOTE_MY_DATA_DIR}
sudo chmod 700 ${REMOTE_MY_DATA_DIR}
sudo chown ${USERNAME}:${USERNAME} ${REMOTE_MY_DATA_DIR}
 
# Create R Studio user ssh directory
sudo mkdir "${REMOTE_SSH_DIR}"
sudo chmod 700 ${REMOTE_SSH_DIR}
sudo chown ${USERNAME}:${USERNAME} ${REMOTE_SSH_DIR}

# Set the autorized keys to authorize DC2D web app to SFTP without password
sudo cp ${DC2D_PUBLIC_KEY} ${REMOTE_SSH_DIR}
sudo chmod 600 ${REMOTE_SSH_AUTH_KEYS}
sudo chown ${USERNAME}:${USERNAME} ${REMOTE_SSH_AUTH_KEYS}
