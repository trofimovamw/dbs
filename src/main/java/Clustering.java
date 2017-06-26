import com.sun.deploy.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Object;
import java.util.regex.Pattern;


/**
 * Created by maria on 24/06/17.
 */
public class Clustering {
    public static ArrayList<String> centers;
    public static ArrayList<String> old_centers;
    public static ArrayList<String> hashtags;
    public static Integer K;
    public static ArrayList<ArrayList<String>> clusters;

    // Continue until old cluster and new cluster are the same...


    public Clustering(String filename){}

    public void setHashtags(String filename) throws IOException{

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String[] line = reader.readLine().split(";");

        String hash = line[4];
        String[] tags = hash.split(",");
         for(int i = 0 ; i < tags.length; i++) {
             if (hashtags.contains(tags[i])==false) {
                 hashtags.add(tags[i]);
             }
             else {
                 continue;
             }
         }



    }

    public static void setInitialK(Integer k) {K = k;}


    public static void setInitialCenters(ArrayList<String> hashtags) {
        ArrayList<String> c = new ArrayList<String>();

        for (int i=0; i < K; i++) {
            Random rand = new Random();
            int  n = rand.nextInt(hashtags.size()-1);
            String a = hashtags.get(n);

            //System.out.println(hashtags.get(n));

            c.add(a);
        }

        centers = c;
        ArrayList<ArrayList<String>> cl = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < centers.size(); i++ ) {
            ArrayList<String> astr = new ArrayList<String>();
            astr.add(0,centers.get(i));
            System.out.println(centers.get(i));
            cl.add(i,astr);
        }
        clusters = cl;

    }


    public static void setCenters(ArrayList<String> cluster) {

        //choose k cluster centers
        ArrayList<Integer> distances = new ArrayList<Integer>();
        for (int i = 0; i < cluster.size(); i++) {
            Integer dist = 0;
            for (int j = 0; j < cluster.size(); j++) {
                String a = cluster.get(i);
                String b = cluster.get(j);
                if (a.length() > b.length() == true) {dist = dist + Metric(a,b);}
                else {dist = dist+ Metric(b,a);}
            }
            distances.add(i,dist);
        }

        Integer distance = Collections.min(distances);
        System.out.println(distance);
        int index = distances.indexOf(distance);
        String newCenter = cluster.get(index);
        System.out.println(cluster.get(index));


        Integer index_of_center = clusters.indexOf(cluster);
        centers.set(index_of_center,newCenter);


    }

    public static void assignToCluster(String hashtag) {
        //calculate distance from object to each cluster center
        //assign object to cluster with shortest distance
        ArrayList<Integer> comp_dist = new ArrayList<Integer>();
        for (int i = 0; i < centers.size(); i++) {
            String a = centers.get(i);
            String b = hashtag;
            Integer n = 0;
            if (a.length() > b.length() == true) {n = Metric(a,b);}
            else {n = Metric(b,a);}
            comp_dist.add(n);
        }

        Integer distance = Collections.min(comp_dist);
        int index = comp_dist.indexOf(distance);
        String center = centers.get(index);
        ArrayList<String> cl =  clusters.get(index);
        //cl.add(center);
        if (cl.contains(hashtag)==false) {cl.add(hashtag);}
        clusters.set(index,cl);



    }

    public static void getCluster() {
        for (int i = 0; i < clusters.size(); i++) {
            ArrayList<String> cl = clusters.get(i);

            System.out.print("{");
            for (int jj = 0; jj < cl.size(); jj++) {
                System.out.print(cl.get(jj));
                System.out.print(",");
            }
            System.out.print("}");
        }
    }
    // Metric using Levenstein distance (modify?)

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

    public static void main(String[] args) throws IOException {
        String a = "MaGreatAgafffffin";
        String b = "makeamericagre";
        System.out.println(Metric(a,b));

        ArrayList<String> test = new ArrayList<String>();
        test.add("abc");
        test.add("bcbc");
        test.add("abec");
        test.add("bcbeec");
        test.add("abcc");
        test.add("bcsdfc");
        test.add("abcCCC");
        test.add("bcbcdsd");
        test.add("abedsc");
        test.add("bcbeecds");
        test.add("abccss");
        test.add("bccsss");

        setInitialK(2);
        setInitialCenters(test);

        for (int i = 0; i < test.size(); i++) {
            assignToCluster(test.get(i));
        }
        getCluster();
        System.out.println(" ");
        for (int i = 0; i < K; i++) {
            setCenters(clusters.get(i));
        }
        for (int i = 0; i < test.size(); i++) {
            assignToCluster(test.get(i));
        }
        getCluster();

    }
}
