#!/bin/bash

Xvfb -ac &
export DISPLAY=:0


ant -f cute-build.xml

killall Xvfb