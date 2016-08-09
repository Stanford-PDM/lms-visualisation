#!/bin/bash
#set -x

WATCHDIR="/Users/dengels/Documents/EPFL/PDM/Projects/mine-hyperdsl"
RUNSCRIPT="./recompile.sh"

echo "Watching directory $WATCHDIR for changes ..."
fswatch -o "$WATCHDIR" | xargs -n1 "$RUNSCRIPT"
