#!/bin/bash
sleep 2s
nohup java -jar ./music_server-all.jar > ./log.log 2>&1 &
