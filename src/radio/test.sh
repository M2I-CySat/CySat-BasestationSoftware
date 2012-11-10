total=0
for dir in $(ls -d */ .)
do
	for i in $(ls $dir/*.java)
	do
		total=$((total + $(wc $i | head -c 6)))
	done
done
echo $total
