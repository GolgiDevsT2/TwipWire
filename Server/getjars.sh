#!/bin/sh

/bin/echo -n "Downloading Needed Apache JARs: "
rm -rf libs;
mkdir -p libs; 
(cd libs;
    curl -s http://ftp.heanet.ie/mirrors/www.apache.org/dist//httpcomponents/httpclient/binary/httpcomponents-client-4.3.3-bin.tar.gz | tar -xzf -;
    curl -s http://ftp.heanet.ie/mirrors/www.apache.org/dist/commons/io/binaries/commons-io-2.4-bin.tar.gz | tar -xzf -;
    curl -s http://ftp.heanet.ie/mirrors/www.apache.org/dist/commons/lang/binaries/commons-lang3-3.3.2-bin.tar.gz | tar -xzf -;
    curl -s http://ftp.heanet.ie/mirrors/www.apache.org/dist/logging/log4j/1.2.17/log4j-1.2.17.tar.gz | tar -xzf - 
    curl -s https://quick-json.googlecode.com/files/quick-json-1.0.2.3.jar >quick-json-1.0.2.3.jar
    curl -s http://twitter4j.org/archive/twitter4j-4.0.2.zip >twitter4j-4.0.2.zip    

    find . -name httpclient-4.3.3.jar -print | (while read j; do cp -f $j .; done)
    find . -name httpcore-4.3.2.jar -print | (while read j; do cp -f $j .; done)
    find . -name commons-logging-1.1.3.jar -print | (while read j; do cp -f $j .; done)
    find . -name commons-io-2.4.jar -print | (while read j; do cp -f $j .; done)
    find . -name commons-lang3-3.3.2.jar | (while read j; do cp -f $j .; done)
    find . -name log4j-1.2.17.jar | (while read j; do cp -f $j .; done)

    find . -type d -maxdepth 1 | grep / | (while read d; do rm -rf $d; done)

    mkdir twitter4j; (cd twitter4j; unzip ../twitter4j-4.0.2.zip; mv ./lib/twitter4j-core-4.0.2.jar ../)
    rm -rf twitter4j

)
echo "DONE"

    