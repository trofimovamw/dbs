    import java.io.BufferedReader;
    import java.io.FileReader;
    import java.io.IOException;
    import java.io.PrintStream;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.UUID;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;

/**
 * Created by maria on 29/05/17.
 *
 *
 *
 *
 *
 * Method to clean and build new data for the databank
 * ER model uses columns handle, text, is_retweet, original_author, hashtags(extracted with this program),
 * number of retweets, number of favorites and unique IDs(also generated by this program).
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
    public static String ID;


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

        //ignore all not ASCII characters (better encoding/writing)

        String txt = str.replaceAll("[^\\p{ASCII}]", " ");
        text = txt;
    }


    public static void checkTime(String str) {
        //Check time format 2016-09-27T21:25:31
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

        //Extract hashtags based on pattern "#{hashtag}"

        String tag_pattern = "(#\\w+)";

        Pattern p = Pattern.compile(tag_pattern);
        Matcher m = p.matcher(str);
        List<String> htags = new ArrayList<String>();

        while (m.find()) {
            String hashtag = m.group(1).replace("#","");

            //"#1" not a hashtag?
            htags.add(hashtag);
        }

        hashtags = htags;
    }

    public static void createID() {

        //Generates random IDs for tweets (based on ER model)

        String uniqueID = UUID.randomUUID().toString();
        ID = uniqueID;
    }

    public static void writeHeader(PrintStream out) {
        out.println("handle;text;is_retweet;original_author;hashtags;time;retweet_count;favorite_count;ID");
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
                    out.print(",");
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
            out.print(favorite);
            out.print(";");
            out.println(ID);
        }
    }

    /*
     *
     *
     *
     *
     * Main method
     * Input: file "american-election-tweets.csv" (usage also specified inside)
     * Output: clean new file
     */


    public static void main(String[] args) throws IOException {
        System.out.println("********************************************************************************");
        System.out.println("2. Iteration: Datenbereinigung");
        System.out.println("********************************************************************************");

        if (args.length != 1) {
            System.out.println("Only one object in input needed");

        } else {


            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            PrintStream out = new PrintStream("american-election-tweets-clean.csv");
            reader.readLine(); // ignore the header
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
                    createID();

                    writeLine(out);

                }

                else {

                    //Checks if whole line has standard length of 11
                    //If not, assuming that additional delimiters are inside the tweet,
                    //delete delimiters inside this tweet using iteration and merge.

                    damaged_line = damaged_line + l.replace("/n"," ");
                    String[] sp = damaged_line.split(";");

                    if (sp.length >= 11) {
                        String merge = "";
                        int count = 0;
                        if (sp.length > 11) {
                            for (int i = 1; i < sp.length - 9; i++) {
                                merge += sp[i];
                                count++;
                            }
                            //System.out.println(merge);
                            merge = merge.replace(";"," ");
                            damaged_line = sp[0] + ";" + merge;
                            for (int j = count+1; j < sp.length; j++) {
                                damaged_line = damaged_line + ";" + sp[j];
                            }
                        }
                        String[] split = damaged_line.split(";");

                        checkCandidate(split[0]);
                        checkTweet(split[1]);
                        checkTime(split[4]);
                        checkOriginal(split[3]);
                        checkIsRetweet(split[2]);
                        checkRetweetCount(split[7]);
                        checkFavoriteCount(split[8]);
                        getHashtag(split[1]);
                        createID();

                        writeLine(out);

                        damaged_line = "";

                    }
                }



            }
        }

    }
}