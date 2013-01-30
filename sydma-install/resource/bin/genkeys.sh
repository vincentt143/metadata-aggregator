#!/bin/bash
KEYNAME=$1
if [ -z "$KEYNAME" ]; then
  echo "Please provide name for key (no spaces)"
  echo "Generates pub_, priv_ keys in current directory"
  exit 1
fi
priv_pem=~/priv.pem
openssl genrsa -out $priv_pem 2048
openssl rsa -in $priv_pem -pubout -outform DER -out pub$KEYNAME.der
openssl rsa -in $priv_pem -outform DER -out priv$KEYNAME.der
rm -f $priv_pem
echo "Generated pub$KEYNAME.der and priv$KEYNAME.der"

