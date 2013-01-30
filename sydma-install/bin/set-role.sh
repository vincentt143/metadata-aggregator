#!/bin/bash +x

#
# -- config
#
CONFIG=/usr/share/tomcat6/mda-data/dms.home/database.properties
DBUSER=`awk -F '=' '/database.username/{print $2}' $CONFIG`
DBPASS=`awk -F '=' '/database.password/{print $2}' $CONFIG`
DBHOST=`awk -F '=' '/database.url/{match($2,"//([^:])+"); if (RLENGTH>0) {print substr($2,RSTART+2,RLENGTH-2);}}' $CONFIG`
DBNAME=`awk -F '=' '/database.url/{match($2,"/[a-zA-Z0-9]+[?]"); if (RLENGTH>0) {print substr($2,RSTART+1,RLENGTH-2);}}' $CONFIG`

function mysql_command {
  ## echo mysql -u $DBUSER --password="*******" -h $DBHOST $DBNAME -e "$1"
  mysql -u $DBUSER --password="$DBPASS" -h $DBHOST $DBNAME -e "$1"
}

USAGE="\n$0: [-h] [-u <username>] [-r <role>] [-l]\n\n-h: usage help and exit\n-l without -u: list all roles\n-l with -u: lst roles of a user\n-u username (unikey)\n-r :assign give role to user\n(Using CONFIG=$CONFIG)\n"

while getopts "u:r:lh" options; do
  case $options in
    u) USERNAME=$OPTARG;;
    r) ROLE=$OPTARG;;
    l) LIST=list;;
    h) echo -e $USAGE
       echo Connection parameters: u/p=$DBUSER/$DBPASS host=$DBHOST, dbname=$DBNAME
       exit 0;;
    *) echo -e $USAGE
       exit 1;;
  esac
done

if [ -n "$ROLE" -a -z "$USERNAME" ]; then
   echo "Must provide a username to assign a role"
   echo -e $USAGE
   exit 1
fi

if [ -n "$LIST" ]; then
   if [ -n "$USERNAME" ]; then
      mysql_command "SELECT r.name, r.display_name FROM roles r JOIN users_roles ur ON r.id=ur.roles JOIN users u ON ur.users=users WHERE u.username='$USERNAME' ORDER BY r.name"
      exit 0
   else
      mysql_command "SELECT r.name, r.display_name FROM roles r ORDER BY r.name" 
      exit 0
   fi
fi

if [ -z "$ROLE" ]; then
   echo -e $USAGE
   exit 1
fi

echo "assign $ROLE to $USERNAME"
mysql_command "SELECT id FROM users WHERE username='$USERNAME'"
mysql_command "SELECT id FROM roles WHERE name='$ROLE'"
echo "INSERT INTO users_roles (users,roles) values($USERID, $ROLEID)"
