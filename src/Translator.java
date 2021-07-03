import com.sun.org.apache.xml.internal.utils.XML11Char;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Translator {

    public static int totalSentence = 0;
    public static int progress = 0;

    public static void main(String args []) throws Exception {
        String path;
        if(args.length > 0){
            path = args[0];
        }else {
            path = new File(Translator.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getPath();
        }
        path = path.substring(0, path.lastIndexOf("/"));
        path = path + "/app/src/main/res";

        File[] allRes = new File(path).listFiles();
        ArrayList<ParentFolderModel> allLangFolders = new ArrayList<>();
        for (int i = 0; i < allRes.length; i++) {
            File f = new File(allRes[i].getPath());
            if(f.isDirectory() && f.getName().startsWith("values-")){
                String localCode = f.getName().replace("values-", "");
                //localCode = localCode.substring(0, localCode.indexOf("-") + 1);
                localCode = localCode.split("-")[0];
                System.out.println("Found Locale: " + localCode.toUpperCase());
                if(Constans.containsLocale(localCode)){
                    File f2 = new File(f.getPath());
                    allLangFolders.add(new ParentFolderModel(f2, localCode));
                }

            }
        }

        for (int i = 0; i < allLangFolders.size(); i++) {
            //System.out.println("Root element: " + allLangFolders.get(i).getName());
        }

        File sourceStrings = new File(path + "/values/strings.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(sourceStrings);

        doc.getDocumentElement().normalize();
        //System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

        ArrayList<StringModel> models = new ArrayList<>();
        NodeList nodeList = doc.getElementsByTagName("string");
        for (int itr = 0; itr < nodeList.getLength(); itr++) {
            Node node = nodeList.item(itr);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                if(!eElement.getAttribute("translatable").equals("false")){
                    models.add(new StringModel(eElement.getAttribute("name"), eElement.getTextContent()));
                    //System.out.println(itr + " " +eElement.getAttribute("name") + " = "+ eElement.getTextContent());
                }

            }
        }

        totalSentence = allLangFolders.size() * models.size();
        for (int i = 0; i < allLangFolders.size(); i++) {
            //if(allLangFolders.get(i).getLocale().equals("bn")){
                translate("en", allLangFolders.get(i).getLocale(), allLangFolders.get(i).getFile(), models);
            //}

        }
        System.out.println("Progress: " + progress + "/" + totalSentence + "\r");
        System.out.println("Completed");


    }

    private static String translate(String langFrom, String langTo, File parent, ArrayList<StringModel> models) throws Exception {
        //Root Element
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("resources");
        doc.appendChild(rootElement);

        String src = "";
        for (StringModel model : models) {
            src +=  "__________" + model.getValue();
        }

        String urlStr = "https://script.google.com/macros/s/AKfycbwp5mHDqxjSNaq9qj8-ylZ7A3G9fES3HIh7mKC89JavJ1KBkHkMWVr1LTTFJ66KqZM/exec" +
                "?q=" + URLEncoder.encode(src, "UTF-8") +
                "&target=" + langTo +
                "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String [] results = response.toString().split("__________");

        for (int i = 0; i < models.size(); i++) {
            Element string = doc.createElement("string");
            string.setAttribute("name", models.get(i).getName());
            string.setTextContent(results[i + 1]);
            rootElement.appendChild(string);
            progress++;
            System.out.print("Progress: " + progress + "/" + totalSentence + "\r");
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(parent.getPath() + "/strings.xml"));
        transformer.transform(source, result);
        return "";
    }

}
