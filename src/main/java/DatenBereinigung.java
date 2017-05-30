/**
 * Created by maria on 30/05/17.
 */


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by maria on 29/05/17.
 */
public class DatenBereinigung {


    public static String candidate;
    public static String text;
    public static String originalAuthor;
    public static String time;
    public static String isRetweet;
    public static String retweet;
    public static String favorite;
    public static List<String> hashtags = new ArrayList<String>();
    public static String damaged_line;


    public DatenBereinigung(String filename) {}


    public static void checkCandidate(String str) {

        if (str.length() == 14) {
            candidate = "HillaryClinton";
        }

        else if (str.length() == 15) {
            candidate = "DonaldTrump";
        }
        else {
            candidate = " ";
        }
    }

    public static void checkTweet(String str) {
        String txt = str.replaceAll("[^\\p{ASCII}]", " ");
        text = txt;
    }

    //Format 2016-09-27T21:25:31

    public static void checkTime(String str) {
        if (str.matches("^\\d{4}[-]{1}\\d{2}[-]{1}\\d{2}[T]{1}\\d{2}[:]{1}\\d{2}[:]{1}\\d{2}") == true) {
            time = str;
        } else {
            time = " ";
        }
    }

    public static void checkIsRetweet(String str) {
        isRetweet = str;
    }

    public static void checkOriginal(String str) {

        originalAuthor = str;

    }

    public static void checkRetweetCount(String count) {
        retweet = count;
    }

    public static void checkFavoriteCount(String count) {
        favorite = count;
    }


    public static void getHashtag(String str) {
        String tag_pattern = "(#\\w+)";

        Pattern p = Pattern.compile(tag_pattern);
        Matcher m = p.matcher(str);
        List<String> htags = new ArrayList<String>();

        while (m.find()) {
            String hashtag = m.group(1).replace("#","");

            //System.out.println(hashtag);
            //"#1" not a hashtag?
            htags.add(hashtag);
        }

        hashtags = htags;
    }

    public static void writeHeader(PrintStream out) {
        out.println("handle;text;is_retweet;original_author;hashtags;time;retweet_count;favorite_count");
    }


    public static void writeLine(PrintStream out) {
        if (candidate == " ") {
            out.print("");
        }
        else {
            out.print(candidate);
            out.print(";");
            out.print(text);
            out.print(";");
            out.print(isRetweet);
            out.print(";");
            out.print(originalAuthor);
            out.print(";");
            if (hashtags.size() != 0) {
                for (int i = 0; i < hashtags.size() - 1; i++) {
                    String hashtag = hashtags.get(i);
                    out.print(hashtag);
                    out.print("+");
                }
                out.print(hashtags.get(hashtags.size() - 1));
                out.print(";");
            }
            else {
                out.print(";");
            }
            out.print(time);
            out.print(";");
            out.print(retweet);
            out.print(";");
            out.println(favorite);
        }
    }


    public static void main(String[] args) throws IOException {
        System.out.println("********************************************************************************");
        System.out.println("2. Iteration: Datenbereinigung");
        System.out.println("********************************************************************************");

        if (args.length != 1) {
            System.out.println("Only one object in input needed");

        } else {


            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            PrintStream out = new PrintStream("american-election-tweets-clean.csv");
            String l;

            writeHeader(out);


            while ((l = reader.readLine()) != null) {
                String[] ll = l.split(";");


                if (ll.length == 11) {

                    checkCandidate(ll[0]);
                    checkTweet(ll[1]);
                    checkTime(ll[4]);
                    checkOriginal(ll[3]);
                    checkIsRetweet(ll[2]);
                    checkRetweetCount(ll[7]);
                    checkFavoriteCount(ll[8]);
                    getHashtag(ll[1]);

                    writeLine(out);


                }

                /*else {
                    damaged_line += l.replace("/n"," ");
                    String[] split = damaged_line.split(";");

                    if (split.length == 11) {
                        checkCandidate(ll[0]);
                        checkTweet(ll[1]);
                        checkTime(ll[4]);
                        checkOriginal(ll[3]);
                        checkIsRetweet(ll[2]);
                        checkRetweetCount(ll[7]);
                        checkFavoriteCount(ll[8]);
                        getHashtag(ll[1]);

                        writeLine(out);

                        damaged_line = "";

                    }
                }*/

                /*else if (ll.length != 11){
                    if (ll[1].contains("\n")) {
                        String fix = ll[1].replace("\n"," ");
                        if (damaged_line == "") {
                            damaged_line = damaged_line + l;
                        }
                        else {
                            damaged_line = damaged_line + ";" + l;
                        }

                    }
                }*/



            }
        }

    }
}
