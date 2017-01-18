#!/bin/sh
set -eo pipefail

if [ -z "${DATABASE_ADDRESS}" ] \
      || [ -z "${DATABASE_NAME}" ] \
      || [ -z "${DATABASE_USERNAME}" ] \
      || [ -z "${DATABASE_PASSWORD}" ] \
      || [ -z "${API_OAUTH2_CLIENT_ID}" ] \
      || [ -z "${API_OAUTH2_CLIENT_SECRET}" ] \
      || [ -z "${UID_PRIVATE_KEY}" ] \
      || [ -z "${SERVER_PROFILE}" ]; then
  echo "One of the following environment variables have not been set:

  DATABASE_ADDRESS
  DATABASE_NAME
  DATABASE_USERNAME
  DATABASE_PASSWORD
  API_OAUTH2_CLIENT_ID
  API_OAUTH2_CLIENT_SECRET
  UID_PRIVATE_KEY
  SERVER_PROFILE

Please specify them before starting the container, preferably in an environment file.
"
    exit 1
fi

exec "$@"
