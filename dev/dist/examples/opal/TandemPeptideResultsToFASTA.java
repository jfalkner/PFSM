import java.io.*;
import java.util.*;

public class TandemPeptideResultsToFASTA {

public static void main(String[] args) throws Exception {
  // make a new reader
  BufferedReader in = new BufferedReader(new FileReader(args[0]));

  for (String line=in.readLine();line!=null&&!line.equals("");line=in.readLine()) {
//    System.out.println("Full: "+line);

// get the id

    // split at the seq
    int startID = line.indexOf(" id=\"");
// skip all lines without the seq info
if (startID == -1) {
continue;
}
    String id = line.substring(startID+5, line.length());
    // find the end
    int stopID = id.indexOf(".");
    id = id.substring(0, stopID); 

    // split at the seq
    int start = line.indexOf(" seq=\"");
// skip all lines without the seq info
if (start == -1) {
continue;
}
    line = line.substring(start+6, line.length());
    // find the end 
    int stop = line.indexOf("\"");
    line = line.substring(0, stop);
    // print in FASTA form
    System.out.println(">o_"+id+".dta\n"+line);
  }

}
}
