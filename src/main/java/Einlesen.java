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

//-----------------------------------------------------------------------------------------------------------
//Created by:		Philipp Schlechter
//Date:				31/05/2017
//-----------------------------------------------------------------------------------------------------------
public class Einlesen {

    //	Methode zum Pruefen, ob ein Zeichen in einem String vorkommt
    private static boolean containsString( String s, String subString ) {
        return s.indexOf( subString ) > -1 ? true : false;
    }


    //	Methode zum Einfuegen der Daten in die DB
    public static void importToDB(BufferedReader reader) throws SQLException, ParseException, ClassNotFoundException, NumberFormatException, IOException{

        reader.readLine(); // ignore the header
        String l;

//      Datumsformat definieren
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//      Variablen für DB-Interaktion
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;
        PreparedStatement stmt5 = null;
        PreparedStatement stmt6 = null;
        PreparedStatement stmt7 = null;
        PreparedStatement stmt8 = null;
        Connection c = null;

//    	JDBC Treiber laden
        Class.forName("org.postgresql.Driver");

//        Verbindung zur Datenbank herstellen
        c = DriverManager
                .getConnection("jdbc:postgresql://localhost:8080/Election",
                        "testuser", "testpass");

        while ((l = reader.readLine()) != null){
//        	semicolon als Trennzeichen zum Zugriff auf einzelne Felder
            String[] ll = l.split(";");
            String candidate = ll[0];
            String text = ll[1];
            boolean isRetweet = Boolean.parseBoolean(ll[2]);
            String originalAuthor = ll[3];
            String hashtags = ll[4];
            java.sql.Date time = new java.sql.Date(df.parse(ll[5].replace("T"," ")).getTime());
            int retweetCount = Integer.parseInt(ll[6]);
            int favouriteCount = Integer.parseInt(ll[7]);
            String ID = ll[8];

//        	Einfuegen in Tabelle "Tweet"
            String insertTweet = 	"INSERT INTO \"Tweet\" (\"TweetID\", text, \"time\", retweet_count, favourite_count) " +
                    "VALUES (?,?,?,?,?)";
            stmt = c.prepareStatement(insertTweet);
            stmt.setString(1, ID);
            stmt.setString(2, text);
            stmt.setDate(3, time);
            stmt.setInt(4, retweetCount);
            stmt.setInt(5, favouriteCount);
            stmt.executeUpdate();

//        	Einfuegen in Tabelle "contains"
            if(hashtags.length() != 0){
                if(containsString(hashtags,"+")){
                    String[] tags = hashtags.split("\\+");
                    for (int i = 0; i < tags.length; i++) {
                        Statement checkContain = c.createStatement();
                        String sql = "SELECT * FROM public.contains WHERE \"TweetID\"='" + ID + "' AND htext='" + tags[i] + "'";
                        ResultSet rs = checkContain.executeQuery(sql);
                        if(!rs.next()){       // Falls es den Eintrag noch nicht gibt
                            String insertContain = "INSERT INTO public.contains (\"TweetID\", htext) VALUES (?, ?);";
                            System.out.println(insertContain);
                            stmt2 = c.prepareStatement(insertContain);
                            stmt2.setString(1, ID);
                            stmt2.setString(2, tags[i]);
                            stmt2.executeUpdate();
                        }
                    }
                }else{
                    String insertContain = "INSERT INTO public.contains (\"TweetID\", htext) VALUES (?, ?);";
                    stmt2 = c.prepareStatement(insertContain);
                    stmt2.setString(1, ID);
                    stmt2.setString(2, hashtags);
                    stmt2.executeUpdate();
                }

            }

//    	    Einfuegen in Tabelle User und Candidate
            Statement checkUser = c.createStatement();
            String sqlUser = "SELECT * FROM public.\"User\" WHERE \"UserName\"='" + candidate + "' OR \"UserName\"='" + originalAuthor + "'";
            ResultSet rs = checkUser.executeQuery(sqlUser);
            if(!rs.next()){
                String insertUser = "INSERT INTO public.\"User\" (\"UserName\") VALUES (?);";
                stmt4 = c.prepareStatement(insertUser);
                stmt4.setString(1, candidate);
                stmt4.executeUpdate();
                String insertCandidate = "INSERT INTO public.\"Candidate\" (\"UserName\") VALUES (?);";
                stmt5 = c.prepareStatement(insertCandidate);
                stmt5.setString(1, candidate);
                stmt5.executeUpdate();
            }

//		    Einfuegen in Tabelle User von Usern die keine Kandidaten sind
            Statement checkUser2 = c.createStatement();
            String sqlUser2 = "SELECT * FROM public.\"User\" WHERE \"UserName\"='" + originalAuthor + "'";
            ResultSet rs2 = checkUser2.executeQuery(sqlUser2);
            if(!rs2.next() && originalAuthor != ""){
                String insertUser2 = "INSERT INTO public.\"User\" (\"UserName\") VALUES (?);";
                stmt7 = c.prepareStatement(insertUser2);
                stmt7.setString(1, originalAuthor);
                stmt7.executeUpdate();
            }


//		    Einfuegen in Tabelle writes
            String insertWrites = "INSERT INTO public.writes (\"UserName\", \"TweetID\") VALUES (?,?);";
            stmt6 = c.prepareStatement(insertWrites);
            stmt6.setString(1, candidate);
            stmt6.setString(2, ID);
            stmt6.executeUpdate();

//		    Einfuegen in Tabelle retweet_from
            if(isRetweet){
                String insertRetweet = "INSERT INTO public.retweet_from (\"UserName\", \"TweetID\") VALUES (?,?);";
                stmt8 = c.prepareStatement(insertRetweet);
                stmt8.setString(1, originalAuthor);
                stmt8.setString(2, ID);
                stmt8.executeUpdate();
            }


        }

//    	Einfuegen in Tabelle Hashtags
        Statement distinctTags = c.createStatement();
        String sqlTags = "SELECT DISTINCT htext FROM public.contains";
        ResultSet rsTags = distinctTags.executeQuery(sqlTags);
        while(rsTags.next()){
            String insertHashtags = "INSERT INTO public.\"Hashtag\" (htext) VALUES (?);";
            stmt3 = c.prepareStatement(insertHashtags);
            stmt3.setString(1, rsTags.getString("htext"));;
            stmt3.executeUpdate();
        }



    }


    //	Main Method
//	Input: american-election-tweets-clean.csv
//	Output: Data inserted to Database
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, ParseException {

        System.out.println("********************************************************************************");
        System.out.println("2. Iteration: Datenimport");
        System.out.println("********************************************************************************");

//      Pruefen, ob Datei im Input vorhanden
        if (args.length != 1) {
            System.out.println("Only one object in input needed");
        } else {
//        	Daten aus Excel-Datei auslesen
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));

//        	Importfunktion aufrufen
            importToDB(reader);
        }
    }
}
