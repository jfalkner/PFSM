#!/bin/sh

# make the header
echo "<Search>"
echo "  <!-- configuration for the cache, i.e gap bridging -->"
echo "  <Search:Parameters"
echo "    cacheMaxResidues=\"3\"/>"
echo ""
echo "  <!-- search the michrom data -->"
echo "  <Search:Sequences uri=\"example.fasta\"/>"
echo ""
echo "  <Search:Group name=\"Opal All\">"

for name in $( ls dta/* )
do
  echo "    <Search:PeakList uri=\"$name\"/>"
done

echo "</Search:Group>"
echo "</Search>"
