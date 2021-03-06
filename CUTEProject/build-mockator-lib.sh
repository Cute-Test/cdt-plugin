#!/bin/sh
MOCKATOR_LIB_DIR=ch.hsr.ifs.cute.mockator.lib
MOCKATOR_PLUGIN_DIR=ch.hsr.ifs.cute.mockator

init_path() {
  THIS=$(readlink -f $0)
  cd "`dirname $THIS`"
}

execute_mockator_unit_tests() {
  echo "[INFO] Starting Mockator lib tests for ${1}"
  $MOCKATOR_LIB_DIR/mockator_tests ${1}
}

mockator_unit_tests_for_cpp11() {
  make USE_STD11=1 -s -C $MOCKATOR_LIB_DIR clean all
  execute_mockator_unit_tests "C++11"
}

mockator_unit_tests_for_cpp03() {
  make -s -C $MOCKATOR_LIB_DIR clean all
  execute_mockator_unit_tests "C++03"
}

copy_mockator_header_file() {
  cp -p $MOCKATOR_LIB_DIR/mockator/mockator.h $MOCKATOR_PLUGIN_DIR/headers/
}

init_path && mockator_unit_tests_for_cpp03 && mockator_unit_tests_for_cpp11 && copy_mockator_header_file
