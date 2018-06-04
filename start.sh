#! /bin/bash

docker build -t slackbotimg .
docker run -it slackbotimg
