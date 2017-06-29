import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;



/**
 * Created by maria on 24/06/17.
 */
public class Clustering {
    public static ArrayList<String> centers;
    public static ArrayList<String> oldCenters;
    public static ArrayList<String> hashtags;
    public static Integer K;
    public static Integer convergence = 2;
    public static ArrayList<ArrayList<String>> clusters;
    public static ArrayList<ArrayList<Integer>> distances;

    // Continue until old cluster and new cluster are the same (with convergence order)...


    public Clustering(String filename){}

    /*
     *
     *
     * Set hashtags from database (no repetitions)
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

            System.out.println(hashtags.get(arr[i]));

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
      *
      *
      * Print the clusters
      *
      */


    public static void getCluster() {
        for (int i = 0; i < clusters.size(); i++) {
            ArrayList<String> cl = clusters.get(i);

            System.out.print("{");
            for (int jj = 0; jj < cl.size(); jj++) {
                System.out.print(cl.get(jj));
                System.out.print(",");
            }
            System.out.println("}");
        }
    }

     /*
      *
      *
      * Print the centers
      */

    public static void getCenters() {

        for (int i = 0; i < centers.size(); i++) {
            String cl = centers.get(i);

            System.out.print("{");
            System.out.print(cl);
            System.out.println("}");
        }
    }

     /*
      *
      * Metric using Levenstein distance (modify?)
      * Calculates "distance" between two strings
      *
      */

    public static int Metric(String tag1, String tag2) {

        tag1 = tag1.toLowerCase();
        tag2 =tag2.toLowerCase();
        // i == 0
        int [] costs = new int [tag1.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= tag1.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= tag2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), tag1.charAt(i - 1) == tag2.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[tag2.length()];

    }

     /*
      *
      *
      * Calculate distances from cluster nodes to the center
      */

    public static void setDistances() {
        ArrayList<ArrayList<Integer>> dist = new ArrayList<ArrayList<Integer>>();

        for (int i = 0; i < clusters.size(); i++) {
            ArrayList<Integer> cluster_distances = new ArrayList<Integer>();
            ArrayList<String> cl = clusters.get(i);

            for (int j = 0; j < cl.size(); j++) {
                Integer d = 0;
                String a = centers.get(i);
                String b = cl.get(j);
                if (a.length() > b.length() == true) {d = d + Metric(a,b);}
                else {d = d + Metric(b,a);}
                cluster_distances.add(j,d);

            }
            dist.add(i,cluster_distances);

        }
        distances = dist;

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
        PrintStream out = new PrintStream("aet-js.html");
        out.print("<!doctype html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Hashtags</title>\n" +
                "\n" +
                "    <style>\n" +
                "        body {\n" +
                "            color: #d3d3d3;\n" +
                "            font: 12pt arial;\n" +
                "            background-color: #222222;\n" +
                "        }\n" +
                "\n" +
                "        #mynetwork {\n" +
                "            width: 800px;\n" +
                "            height: 800px;\n" +
                "            border: 1px solid #444444;\n" +
                "            background-color: #222222;\n" +
                "        }\n" +
                "    </style>\n" +
                "\n" +
                "    <script src=\"./vis-4.20.0/dist/vis.js\"></script>\n" +
                "    <link href=\"./vis-4.20.0/dist/vis.css\" rel=\"stylesheet\" type=\"text/css\" />\n" +
                "\n" +
                "\n" +
                "    \n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "\n" +
                "<div id=\"mynetwork\"></div>\n" +
                "<script type=\"text/javascript\">\n" +
                "    var color = 'gray';\n" +
                "    var len = undefined;\n" +
                "\n" +
                "    var nodes = [");
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
        for (int i =0; i < clusters.size(); i++) {
            for (int j = 0; j < clusters.get(i).size(); j++ ) {
                writeJSEdge(i, j,out);
                out.println(",");
            }
        }

        writeJSEdge(ii,jj,out);
        out.println();

        out.print("]\n" +
                "\n" +
                "    // create a network\n" +
                "    var container = document.getElementById('mynetwork');\n" +
                "    var data = {\n" +
                "        nodes: nodes,\n" +
                "        edges: edges\n" +
                "    };\n" +
                "    var options = {\n" +
                "        nodes: {\n" +
                "            shape: 'dot',\n" +
                "            size: 30,\n" +
                "            font: {\n" +
                "                size: 32,\n" +
                "                color: '#ffffff'\n" +
                "            },\n" +
                "            borderWidth: 2\n" +
                "        },\n" +
                "        edges: {\n" +
                "            width: 2\n" +
                "        }\n" +
                "    };\n" +
                "    network = new vis.Network(container, data, options);\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>");

    }

    public static void writeJSNode(Integer i, Integer j, PrintStream out) {
        out.print("{id: " + hashtags.indexOf(clusters.get(i).get(j)) + ", "
                + "label: " + '"'+clusters.get(i).get(j)+'"' + ", " + "group: " + i + "}");
    }

    public static void writeJSEdge(Integer i, Integer j, PrintStream out) {
        out.print("{from: " + hashtags.indexOf(centers.get(i)) + ", " + "to: " +
                hashtags.indexOf(clusters.get(i).get(j)) + "}");

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

        setDistances();
        writeJS();

    }
}
