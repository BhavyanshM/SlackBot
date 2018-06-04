#! /bin/bash

docker build -t slackbotimg .
docker run -dt slackbotimg
