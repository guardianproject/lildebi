#!/bin/sh

# Policy-rc.d is mentioned in manpage invoke-rc.d(8) and documented at
# http://people.debian.org/~hmh/invokerc.d-policyrc.d-specification.txt

set -e

script=$1
command=$2

# "exit 101" means policy forbids the execution of that script
forbid() {
    echo >&2 "Execution of \"$script $command\" forbidden by policy-rc.d"
    exit 101
}

case "$script" in
    halt) forbid ;;
    hwclock.sh) forbid ;;
    networking) forbid ;;
    ifupdown) forbid ;;
    reboot) forbid ;;
    sendsigs) forbid ;;
    umountfs) forbid ;;
    umountroot) forbid ;;
    urandom) forbid ;;
esac

exit 0
