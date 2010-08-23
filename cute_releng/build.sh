#!/bin/bash

Xvfb -ac -extension RANDR &
export DISPLAY=:0


ant -f cute-build.xml

killall Xvfb