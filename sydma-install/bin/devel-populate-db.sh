#!/bin/sh +x

DB_ROOT=root
DB_USER=sydma
DB_PASSWORD=sydma
DB_NAME=sydma
DB_ROOT_PASSWORD=$1
here=`dirname $0`
SQL_D=""
SQL_F=##

for sql_file in create_users.sql create_access_rights.sql create_buildings.sql create_research_subject_code.sql \
                create_dataset_schema.sql create_vocabulary.sql; do
   echo mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME < $here/../sql/$sql_file
   sed "s/SQL_D/$SQL_D/g
s/SQL_F/$SQL_F/g" $here/../sql/$sql_file | mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME
done
