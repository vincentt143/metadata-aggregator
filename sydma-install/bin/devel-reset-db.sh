#!/bin/sh +x

DB_ROOT=root
DB_USER=sydma
DB_PASSWORD=sydma
DB_NAME=sydma
DB_ROOT_PASSWORD=$1
here=`dirname $0`

cat <<EOT | mysql -u $DB_ROOT -p$DB_ROOT_PASSWORD

CREATE DATABASE $DB_NAME CHARACTER SET UTF8;

CREATE USER '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';

GRANT all privileges on ${DB_NAME}.* to '$DB_USER'@'localhost' with grant option;

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, 
RELOAD, SHOW DATABASES, GRANT OPTION, CREATE USER, REFERENCES, ALTER, CREATE TEMPORARY TABLES, EXECUTE
ON *.* TO '$DB_USER'@'localhost' with grant option;

GRANT CREATE ROUTINE, ALTER ROUTINE, TRIGGER, INDEX, CREATE VIEW, SHOW VIEW, EVENT, LOCK TABLES 
ON *.* TO '$DB_USER'@'localhost' with grant option;

EOT