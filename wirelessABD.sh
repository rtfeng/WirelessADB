#!/bin/bash

while [[ 1 ]]; do
	adbcheck=$(echo $(adb devices))
	read -r -a devices <<< "$adbcheck"
	for element in "${devices[@]}"
	do
		if [[ $element = 192.168.0.183:5555 ]]; then
			myflag=1
		fi
	done
	if [[ $myflag = 1 ]]; then
		myflag=0
	else
		date
		adb connect 192.168.0.183:5555
	fi
	sleep 10
done