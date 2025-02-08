#!/bin/sh
if [ -z "$MONGODB_URI" ]; then
  echo "ERROR: MONGODB_URI not set."
  echo "Set it using: -e MONGODB_URI=<your-mongodb-uri>"
  exit 1
fi

exec java -jar /application.jar