#!/bin/bash
#set -x

REMOTE="/Users/dengels/Documents/EPFL/PDM/Projects/mine-hyperdsl/trace.json"
LOCAL="compilation.json"
TOREMOVE="js/target/scala-2.11/classes/org/lmsviz/Main"


MINIFIED="compilation.min.json"

function copy(){
  cp "$REMOTE" "$LOCAL";
}

# Need to do that to avoid crazy huge class files
function minify(){
  cat "$LOCAL" | \
    sed 's%virtualization-lms-core%lms%g' | \
    sed 's%/Users/dengels/Documents/EPFL/PDM/Projects/mine-hyperdsl/%%g' | \
    json-minify > "$MINIFIED";

  echo "Minify results:"
  du -sh "$LOCAL" "$MINIFIED";
}

function compile(){
  rm -rf "$TOREMOVE"*;
  sbt "; lmsvizJS/fastOptJS; lmsvizJVM/run";
}

# Check is file is not empty and has changed
CHANGE=$(diff -q "$REMOTE" "$LOCAL")
if [ -s "$REMOTE" -a -n "$CHANGE" ]; then
  echo "File changed, recompile...";
  copy && minify && compile;
else
  echo "File hasn't changed, do nothing...";
fi
