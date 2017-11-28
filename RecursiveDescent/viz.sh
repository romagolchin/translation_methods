#!/usr/bin/env bash
for f in answers/*.dot; do
	new_name=$(echo $f | sed -r "s/answers\/([^\.]+)\.dot/pics\/\1\.svg/")
	dot $f -Tsvg > $new_name
done
