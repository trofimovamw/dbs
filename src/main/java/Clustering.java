import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * Created by maria on 24/06/17.
 */
public class Clustering {
    public static ArrayList<String> centers;
    public static ArrayList<String> oldCenters;
    public static ArrayList<String> hashtags;
    public static Integer K;
    public static Integer convergence = 3;
    public static ArrayList<ArrayList<String>> clusters;
    public static ArrayList<ArrayList<Integer>> distances;

    // Continue until old cluster and new cluster are the same...


    public Clustering(String filename){}

    /*
     *
     *
     * Set hashtags from database
     */
    public static void getHashtagsSQL() throws Exception{

        Class.forName("org.postgresql.Driver");
        Connection c;
        //ResultSet result;
        List<String> tags = new ArrayList<String>();


        try {
            String query = "SELECT * FROM hashtag";
            c = DriverManager
                    .getConnection("database");
            Statement stmt = c.createStatement();
            ResultSet result  = stmt.executeQuery(String.format(query));

            while (result.next()) {
                tags.add(result.getString(1));
            }
        }
        catch (SQLException e){}
    }

    /*
     *
     *
     *
     */
    public static void setHashtags(String filename) throws IOException{

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        ArrayList<String> ht = new ArrayList<String>();

        while ((line = reader.readLine()) != null) {

            String[] l = line.split(";");

            String hash = l[4];
            String[] tags = hash.split(",");
            for (int i = 0; i < tags.length; i++) {
                String tag = tags[i].replace("https","");
                if (ht.contains(tag) == false) {
                    //String tag = tags[i].replace("https","");
                    ht.add(tag);
                    //System.out.println(tag);
                } else {
                    continue;
                }
            }


        }
        hashtags = ht;
    }

     /*
      *
      *
      * Number of clusters (set at runtime)
      */
    public static void setInitialK(Integer k) {K = k;}


    /*
     *
     *
     * Set initial random cluster centers
     */
    public static void setInitialCenters(ArrayList<String> hashtags) {
        ArrayList<String> c = new ArrayList<String>();
        ArrayList<Integer> randoms = new ArrayList<Integer>();

        Integer[] arr = new Integer[hashtags.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
        Collections.shuffle(Arrays.asList(arr));

        for (int i=0; i < K; i++) {

            String a = hashtags.get(arr[i]);

            //System.out.println(hashtags.get(arr[i]));

            c.add(a);
        }

        centers = c;
        ArrayList<ArrayList<String>> cl = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < centers.size(); i++ ) {
            ArrayList<String> astr = new ArrayList<String>();
            astr.add(centers.get(i));
            //System.out.println(centers.get(i));
            cl.add(i,astr);
        }
        clusters = cl;

    }


     /*
      *
      * Set centers using this function starting from 2. iteration
      */
    public static void setCenter(ArrayList<String> cluster) {

        ArrayList<Integer> distances = new ArrayList<Integer>();
        for (int i = 0; i < cluster.size(); i++) {
            Integer dist = 0;
            for (int j = 0; j < cluster.size(); j++) {
                String a = cluster.get(i);
                String b = cluster.get(j);
                if (a.length() > b.length() == true) {dist = dist + Metric(a,b);}
                else {dist = dist + Metric(b,a);}
            }

            distances.add(i,dist);
        }

        Integer distance = Collections.min(distances);
        int index = distances.indexOf(distance);
        String newCenter = cluster.get(index);


        Integer index_of_center = clusters.indexOf(cluster);
        centers.set(index_of_center,newCenter);
        ArrayList<String> cl = new ArrayList<String>();
        cl.add(0,newCenter);
        clusters.set(index_of_center,cl);


    }


     /*
      *
      *
      *
      * Set clusters around initialized cluster centers
      *
      */
    public static void assignToCluster(String hashtag) {
        //calculate distance from object to each cluster center
        //assign object to cluster with shortest distance
        ArrayList<Integer> comp_dist = new ArrayList<Integer>();
        for (int i = 0; i < centers.size(); i++) {
            String a = centers.get(i);
            String b = hashtag;
            Integer n = 0;
            if (a.length() >= b.length() == true) {n = Metric(a,b);}
            else {n = Metric(b,a);}
            comp_dist.add(n);
        }

        Integer distance = Collections.min(comp_dist);

        int index = comp_dist.indexOf(distance);
        //String center = centers.get(index);
        ArrayList<String> cl =  clusters.get(index);
        clusters.remove(index);
        //cl.add(center);
        if (cl.contains(hashtag)==false) {cl.add(hashtag);}
        clusters.add(index,cl);


    }


     /*
      *
      *
      *
      * Save old centers for comparison during iteration
      *
      */
    public static void setOldCenter() {
        ArrayList<String> oc = new ArrayList<String>();
        for (int i = 0; i < centers.size(); i++) {
            String center = centers.get(i);
            oc.add(i,center);
        }
        oldCenters = oc;
    }

     /*
      *
      * Metric using Levenstein distance (modify?)
      * Calculates "distance" between two strings
      *
      */

    public static int Metric(String tag1, String tag2) {

        tag1 = tag1.toLowerCase();
        tag2 = tag2.toLowerCase();
        int [] cost = new int [tag1.length() + 1];
        for (int j = 0; j < cost.length; j++)
            cost[j] = j;
        for (int i = 1; i <= tag1.length(); i++) {
            cost[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= tag2.length(); j++) {
                int cj = Math.min(1 + Math.min(cost[j], cost[j - 1]), tag1.charAt(i - 1) == tag2.charAt(j - 1) ? nw : nw + 1);
                nw = cost[j];
                cost[j] = cj;
            }
        }
        return cost[tag2.length()];

    }

     /*
      *
      *
      * Check if centers of clusters changed from last iteration considering convergence order
      */

    public static boolean Convergence(ArrayList<String> a, ArrayList<String> b) {
        Boolean result = true;
        for (int i = 0; i < a.size(); i++) {
            String A = a.get(i);
            String B = b.get(i);
            Integer n = 0;
            if (A.length() >= B.length() == true) {n = Metric(A,B);}
            else {n = Metric(B,A);}


            if(n > convergence) {
                result = result && false;
            }
            else {
                result = result && true;
            }
        }
        return result;
    }



    public static void writeJS() throws Exception{
        //{id: 0, label: "0", group: 0}
        PrintStream out = new PrintStream("AmericanElection.js");
        out.print("    var nodes = [");
        for (int i = 0; i < clusters.size(); i++) {
            for (int j = 0; j < clusters.get(i).size()-1; j++ ) {
                writeJSNode(i, j,out);
                out.println(",");
            }
        }
        int ii = clusters.size()-1;
        int jj = clusters.get(ii).size()-1;
        writeJSNode(ii,jj,out);
        out.println();

        out.print("];\n" +
                "    var edges = [");
        for (int i = 0; i < clusters.size(); i++) {
            for (int j = 0; j < clusters.get(i).size()-1; j++ ) {
                writeJSEdge(i, j,out);
                out.println(",");

            }
        }
        for (int i = 0; i < clusters.size()-1; i++) {
            for (int j = i+1; j < clusters.size(); j++ ) {
                if(distance(centers.get(i),centers.get(j)) < 5) {
                    writeJSCenters(i, j, out);
                    out.println(",");
                }
            }
        }
        for (int i = 0; i < hashtags.size()-1; i++) {
            for (int j = i+1; j < hashtags.size(); j++) {
                if (distance(hashtags.get(i),hashtags.get(j)) < 3) {
                    out.println("{from: " + hashtags.indexOf(hashtags.get(i)) + ", " + "to: " +
                            hashtags.indexOf(hashtags.get(j)) + "," + "label: " +
                            distance(hashtags.get(i),hashtags.get(j)) + ", "  + "font: {align: 'middle'}},");
                }
            }
        }

        writeJSEdge(ii,jj,out);
        out.println();

        out.print("];");
    }

    public static void writeJSNode(Integer i, Integer j, PrintStream out) {
        out.print("{id: " + hashtags.indexOf(clusters.get(i).get(j)) + ", "
                + "label: " + '"'+clusters.get(i).get(j)+'"' + ", " + "group: " + i + "}");
    }

    public static void writeJSEdge(Integer i, Integer j, PrintStream out) {
        //{from: 1, to: 2, label: 'middle',     font: {align: 'middle'}},
        out.print("{from: " + hashtags.indexOf(centers.get(i)) + ", " + "to: " +
                hashtags.indexOf(clusters.get(i).get(j)) + "," + "label: " +
                distance(centers.get(i),clusters.get(i).get(j)) + ", "  + "font: {align: 'middle'}}");

    }

    public static void writeJSCenters(Integer i, Integer j, PrintStream out) {
        out.print("{from: " + hashtags.indexOf(centers.get(i)) + ", " + "to: "
                                      + hashtags.indexOf(centers.get(j))+ "," + "label: " +
                distance(centers.get(i),centers.get(j)) + ", "  + "font: {align: 'middle'}}");
    }


    public static Integer distance(String A, String B) {
        Integer n = 0;
        if (A.length() >= B.length() == true) {n = Metric(A,B);}
        else {n = Metric(B,A);}
        return n;
    }


     /*
      *
      *
      * Main method (clean up!)
      */

    public static void main(String[] args) throws Exception {

        setHashtags("american-election-tweets-clean.csv");

        setInitialK(50);
        setInitialCenters(hashtags);

        setOldCenter();


        for (int i = 0; i < hashtags.size(); i++) {
            assignToCluster(hashtags.get(i));
        }

        for (int i = 0; i < clusters.size(); i++) {
            int number = i;
            setCenter(clusters.get(number));
        }

        for (int i = 0; i < hashtags.size(); i++) {
            assignToCluster(hashtags.get(i));
        }
        int count = 0;

        while(Convergence(oldCenters,centers) == false){
            setOldCenter();
            count += 1;
            for (int i = 0; i < clusters.size(); i++) {
                int number = i;
                setCenter(clusters.get(number));
            }


            for (int i = 0; i < hashtags.size(); i++) {
                assignToCluster(hashtags.get(i));
            }

        }

        writeJS();

    }
}