package Kuba;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Main {

    public static void main(String[] args) throws Exception {

/*
        Application creates SQL table from XML file which is automatically downloading from website.
*/

        // 1.creating a list of <Currencies>.

        List<Currencies> currenciesList = new ArrayList<>();

        // 2. Getting and printing URL of XML.

        String UrlXml = getUrlXml();
        System.out.println("Data source: " + UrlXml);

        // 3. Parsing XML, converting String to double and adding <Currencies> to currenciesList.

        addingCurrienciesToCurrenciesList(currenciesList,UrlXml);

        // 4. Adding <Currencies> to SQL database.

        addingCurrenciesToSql(currenciesList);

        }

        private static String getUrlXml() throws IOException {

            final String url = "https://www.nbp.pl/Kursy/KursyA.html";
            final org.jsoup.nodes.Document document = Jsoup.connect(url).get();

            //Iterating over Hyperlinks

            Elements links = document.select("a[href]");
            String part1 = "https://www.nbp.pl/";
            String part2 = links.attr("href").toString();
            return part1+part2;
        }

        private static void addingCurrienciesToCurrenciesList(List<Currencies> currenciesList, String UrlXml) throws IOException, ParserConfigurationException, SAXException {

            // parsing XML

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            URL url = new URL(UrlXml);
            InputStream stream = url.openStream();
            Document doc = builder.parse(stream);
            NodeList nazwyWaluty = doc.getElementsByTagName("nazwa_waluty");
            NodeList kodyWaluty = doc.getElementsByTagName("kod_waluty");
            NodeList kursWaluty = doc.getElementsByTagName("kurs_sredni");
            String dataPublikacji = doc.getElementsByTagName("data_publikacji").item(0).getTextContent();

            System.out.println("Data publikacji: " + dataPublikacji);

            for (int i = 0; i < nazwyWaluty.getLength(); i++) {

                Node node = nazwyWaluty.item(i);
                Node node1 = kodyWaluty.item(i);
                Node node2 = kursWaluty.item(i);

                //converting String to double

                String originalKurs = node2.getTextContent().replace(',', '.');
                double kurs = Double.parseDouble(originalKurs);

                Currencies c = new Currencies(node.getTextContent(), node1.getTextContent(), kurs);

                //adding <Currencies> to currenciesList

                currenciesList.add(c);

            }
        }

        private static void addingCurrenciesToSql(List<Currencies> currenciesList) throws SQLException, ClassNotFoundException {

            String dburl = "jdbc:mysql://localhost:3306/trening";
            String uname = "root";
            String pass = "";

            String query = "INSERT INTO kursy VALUES (?,?,?)";
            String query2 = "DELETE FROM kursy";

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(dburl, uname, pass);

            PreparedStatement statement = con.prepareStatement(query);
            PreparedStatement statement2 = con.prepareStatement(query2);

            statement2.executeUpdate();

            for (Currencies c:currenciesList
            ) {statement.setString(1, c.getNazwa());
                statement.setString(2, c.getKod());
                statement.setDouble(3, c.getKurs());

                statement.executeUpdate();
            }
            con.close();
        }
    }
