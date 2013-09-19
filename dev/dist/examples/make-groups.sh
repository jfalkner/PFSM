#!/bin/sh

echo "<Search>" > search.xml
for ROW in "A" "B" "C" "D" "E" "F" "G" "H"
do
  for COL in "01" "02" "03" "04" "05" "06" "07" "08" "09" "10" "11" "12" "13" "14" "15" "16" "17" "18" "19" "20" "21" "22" "23"
  do
    echo "" >> search.xml
    echo "  <Search:Group name=\"$ROW$COL\">" >> search.xml
    # find all files
    for file in $( ls $1$ROW$COL* )
    do 
      echo "    <Search:PeakList uri=\"$file\" />" >> search.xml
    done    
    echo "  </Search:Group>" >> search.xml
  done
done
echo "</Search>" >> search.xml
 
