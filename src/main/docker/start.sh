#!/bin/sh

cd /home/service

MAILBOX=${MAILBOX:-rest}
RABBIT=${RABBIT:-host.docker.internal}
USER=${USER:-guest}
PASS=${PASS:-guest}
EXCHANGE=${EXCHANGE:-services}
LOGDIR=${LOGDIR:-.}
LOGFILE=${LOGFILE:-rest-service}
echo "Waiting for the RabbitMQ service to become available"
waiting=1
while [ "$waiting" -eq "1" ] ; do
    sleep 1
    nc -z $RABBIT 5672
    if [ "$?" = "0" ] ; then
        waiting=0
    fi
done

echo "RabbitMQ is online. Starting the REST service"
java -jar ampq-rest-service.jar -x $EXCHANGE -m $MAILBOX -r $RABBIT -u $USER -p $PASS -l $LOGDIR -f $LOGFILE
