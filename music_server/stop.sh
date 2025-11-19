#!/bin/bash
# 停止脚本
apiID=$(ps -ef |grep 'music_server-all.jar'|grep -v 'grep'| awk '{print $2}')
# 如果进程不存在
if test $apiID ;
then
    # 杀死进程
    kill -9 $apiID
    echo "进程已杀死"
else
    # 提示不进程不存在
    echo "进程不存在"
fi