#!/bin/sh


check_env(){
    if [ "$2" = "" ]; then
	echo "$1 is not defined"
	return -1
    fi
    if [ ! -d "$2" ]; then
	echo "$1 '$2' is not a directory"
	return -1
    fi
    return 0
}

err=0

check_env GOLGI_WORK_DIR "$GOLGI_WORK_DIR"
err=`expr $err + $?`

check_env GOLGI_PKG_DIR "$GOLGI_PKG_DIR"
err=`expr $err + $?`

check_env GOLGI_HDR_DIR "$GOLGI_HDR_DIR"
err=`expr $err + $?`

check_env GOLGI_LIB_DIR "$GOLGI_LIB_DIR"
err=`expr $err + $?`

if [ $err -ne 0 ]; then
    exit -1
fi

GJAR="$GOLGI_PKG_DIR/common/garrick_combined.jar"

SRC="$GOLGI_PKG_DIR/iOS"
for f in libGolgi.a libGolgi.h libGolgiLite.a; do
    echo $f | egrep '.h$' >/dev/null 2>&1
    
    if [ $? -eq 0 ]; then
	TGT="$GOLGI_HDR_DIR"
    else
	TGT="$GOLGI_LIB_DIR"
    fi
    CP=0
    if [ ! -f "$TGT/$f" ]; then
        CP=1
    else
        M1=`cat "$TGT/$f" | md5`
        M2=`cat "$SRC/$f" | md5`
        if [ "$M1" != "$M2" ]; then
            CP=1
        fi
    fi
    if [ $CP -ne 0 ]; then
        echo "REPLACING $TGT/$f"
        cp -f "$SRC/$f" "$TGT/$f"
        if [ $? -ne 0 ]; then
            exit -1
        fi
    fi
done

FILES=`find . -name '*.thrift' -print`

if [ "$FILES" = "" ]; then
    echo "No thrift files in directory"
    exit -1
fi

ODIR=.

find . -name '*.thrift' -print | (
    while read TF; do
        SVCDIR=`dirname "$TF"`
        SVC=`basename "$TF" | sed -e 's/\.thrift//'`
        HFILE="$SVC""SvcGen.h"
        MFILE="$SVC""SvcGen.m"
        (
            cd "$SVCDIR";
            rm -f "$HFILE" "$MFILE"

            if [ "$1" != "clean" ]; then
                echo "  TF: '$TF'"
                /bin/echo -n "Generating Code for $SVC: "
                java -classpath $GJAR  com.openmindnetworks.golgi.garrick.Garrick -i `basename "$TF"` -ocdir . -ocdh "$HFILE" -ocdm "$MFILE"
                rc=$?
                if [ $rc -ne 0 ]; then
                    echo "Code Generation For $TF Failed: $rc"
                    exit -1
                fi
                echo "Done "
            fi
        )
        rc=$?
        if [ $rc -ne 0 ]; then
            exit -1
        fi
    done
)

exit 0
