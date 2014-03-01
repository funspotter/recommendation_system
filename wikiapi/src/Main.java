import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import info.bliki.api.Connector;
import info.bliki.api.Page;
import info.bliki.api.PageInfo;
import info.bliki.api.SearchResult;
import info.bliki.api.User;
import info.bliki.api.XMLSearchParser;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;


public class Main {

	/**
	 * @param args
	 */
	 public static final String TEST = "This is a [[Hello World]] '''example'''";
	public static void main(String[] args) {
//		HashMap<String,List<String>> hm = new HashMap<String,List<String>>(); 
//		
//		 String akciofilmek = "A szállító,A nap könnyei,Bad Boys – Mire jók a rosszfiúk?,Halálos fegyver 4,Mad Max,Mission: Impossible, A sárkány útja (film),A Bourne-rejtély (film, 2002),Oroszországból szeretettel,Féktelenül,Az olasz meló,Con Air – A fegyencjárat,Halálos iramban: ötödik sebesség,Kill Bill 1.,Két tűz között,Rambo – Első vér,A szökevény,Ragadozók (film, 1996),A szikla (film),Nikita (film),Ronin (film),Léon a profi,Eredet (film),Drágán add az életed!,Szemtől szemben,Mátrix (film),A Gyűrűk Ura: A Gyűrű Szövetsége (film),Kill Bill 1.,A Gyűrűk Ura: A király visszatér (film),Sin City - A bűn városa,A sötét lovag,Kill Bill 2.,A Karib-tenger kalózai: A Fekete Gyöngy átka,Avatar (film),Gladiátor (film),A tégla,Drágán add az életed!,Ocean's Eleven,A Vasember (film, 2008),Ál/Arc,A sötét lovag,Die Hard 2,Elrabolva,Taxi (film),Indiana Jones és a végzet temploma,Transformers (film, 2007),Az acélember,Itt a vége,Star Trek - Sötétségben,Gengszterosztag";
//         List<String> akciofilmlist = new ArrayList<String>(Arrays.asList(akciofilmek.split(",")));
//         hm.put("akcio",akciofilmlist);
//         
//         String animaciosfilmek = "Szörny Rt,Kung Fu Panda,Aranyhaj,Oroszlánkirály,Hófehérke és a hét törpe,Toy Story,Toy Story 3,A kis hableány,Némó nyomában,Fel,Dumbo,WALL-E,Pinokkió,Gru,Herkules,Jégkorszak,Hihetetlen család,Croodék,Shrek,Macskafogó,Ice Age,Shrek 2,Finding Nemo,Vuk,L’ecsó,Az oroszlánkirály,Wall-E,Up,Madagaszkár,Egy bogár élete,Kung Fu Panda,Így neveld a sárkányodat,Aladdin,A Simpson család - A film,A szépség és a szörnyeteg,Túl a sövényen,Verdák,South Park: Nagyobb, hosszabb és vágatlan,Roger nyúl a pácban,Lúdas Matyi,Cápamese,Z a hangya,A dzsungel könyve,Pocahontas,Rango,Aranyhaj és a nagy gubanc,Tarzan,Bambi,Hamupipőke,Merida, a bátor,Megaagy,Star Wars: A klónok háborúja";
//         List<String> animaciosfilmeklist = new ArrayList<String>(Arrays.asList(animaciosfilmek.split(",")));
//         hm.put("animacio",animaciosfilmeklist);
//         
//         
//         String dokumentumfilmek = "Az öböl,Kijárat az ajándékbolton át,Sivatagi Show,Egy élet a film tükrében,Medvebarát,A Föld_(film),Super Size Me,Fahrenheit 9/11,Kóla puska sültkrumpli,Ember a magasban,Michael Jackson: This is it,Bennfentesek";
//         List<String> dokumentumfilmeklist = new ArrayList<String>(Arrays.asList(dokumentumfilmek.split(",")));
//         hm.put("dokumentum",dokumentumfilmeklist);
//         
//         String dramafilmek = "Z világháború,300,A nagy Gatsby,Stoker,Bosszútól fűtve,Csapda,Felhőatlasz,Napos oldal,Mellékhatások,Megtört város,Gengszterosztag,Lopom a sztárom,Halálos iramban - Tokiói hajsza,A nyomorultak,Menedék,Egy különc srác feljegyzései,Az elnök végveszélyben,Forrest Gumb,Harcosok klubja,Amerikai szépség,A remény rabjai,A Gyűrűk Ura: A király visszatér,A sötét lovag,Becstelen brigantyk,Amerikai história X,Titanic,A tökéletes trükk,Trainspotting,Kapj el,ha tudsz,12 majom,Fekete hattyú,Donnie Darko,Mementó,Hatodik érzék,Egy csodálatos elme,Nem vénnek való vidék,A függetlenség napja,Rekviem egy álomért,Tizenkét dühös ember,Egy makulátlan elme örök ragyogása,A rettenthetetlen,Pillangó-hatás,Az ördög ügyvédje,Számkivetett,Erőszakik,Ál/arc,Taxisofőr,A zongorista,A fülke,Az oroszlánkirály,A keresztapa, Social Network - A közösségi háló,Világok harca,Nagymenők,Mechanikus narancs,Volt egyszer egy Vadnyugat,Drive - Gázt!,A sebhelyesarcú,Jó reggelt,Vietnam!";
//         List<String> dramafilmeklist = new ArrayList<String>(Arrays.asList(dramafilmek.split(",")));
//         hm.put("drama",dramafilmeklist);
//         
//         String fantasyfilmek = "Az acélember,Itt a vége,A hobbit: Smaug pusztasága,Boszorkányvadászok,Óz, a hatalmas,Démoni álca,A zöld urai,A hobbit: Váratlan utazás,Az óriásölő,Az éhezők viadala,Superman visszatér,Éjsötét árnyék,Farkas_(film),Lenyűgöző teremtmények,300,A Gyűrűk Ura: A király visszatér,Shrek,A Gyűrűk Ura: A két torony,Halálsoron,Star Wars - Baljós árnyak,Benjamin Button különös élete,Star Wars - Egy új remény,Harry Potter és a bölcsek köve,A faun labirintusa,Ollókezű";
//         List<String> fantasyfilmeklist = new ArrayList<String>(Arrays.asList(fantasyfilmek.split(",")));
//         hm.put("fantasy",fantasyfilmeklist);
//         
//         String horrorfilmek = "A nyolcadik utas: a halál,Ragyogás,Az Álmosvölgy legendája,Legenda vagyok,A bolygó neve: Halál,Fűrész,Interjú a vámpírral,Alkonyattól pirkadatig,A kör,A múmia,A kör,Cápa,Penge,28 nappal később,Sikoly,Kocka,Pokolfajzat,A dolog,Azonosság,Anakonda,A gömb,A köd,Az ördögűző,Holtak hajnala,A pokolból,Hannibál ébredése,Tortúra,Motel,A légy,Szörnyecskék,Angyalszív,Antikrisztus,Ítéletnap,Tudom, mit tettél tavaly nyáron,Parajelenségek,Halálhajó,Egyenesen át,Temetetlen múlt,Viasztestek,Ómen";
//         List<String> horrorfilmeklist = new ArrayList<String>(Arrays.asList(horrorfilmek.split(",")));
//         hm.put("horror",horrorfilmeklist);
//         
//         String katasztrófafilmek = "Titanic,Armageddon,A függetlenség napja,Godzilla,Képlet,Vírus,Dante pokla,A mag,Fertőzés,Viharzóna,Száguldó bomba,Tűzhányó,A lehetetlen,Rajzás,Lavina,Földrengés,Másnap,Ne várd a csodát!,Hindenburg,Árvíz_(film)";
//         List<String> katasztrófafilmeklist = new ArrayList<String>(Arrays.asList(katasztrófafilmek.split(",")));
//         hm.put("katasztrofa",katasztrófafilmeklist);
//         
//         String krimifilmek = "Ponyvaregény,Harcosok klubja,Hetedik,Kill Bill,Blöff,A sötét lovag,A bárányok hallgatnak,Kapj el, ha tudsz,A keresztapa,Halálsoron,Batman: kezdődik!,A tégla,Kutyaszorítóban,Viharsziget,Mementó,Nem vénnek való vidék,Tizenkét dühös ember,Erőszakik,Különvélemény,Fűrész,A fülke,Elrabolva,Casino,Taxi,Halálos fegyver,A sebhelyesarcú,Szemtől szemben,A nagy Lebowski,Oscar,Ütközések,Zodiákus,Az olasz meló,Csupasz pisztoly,Tolvajtempó,Spíler,A vörös sárkány,Elemi ösztön";
//         List<String> krimifilmeklist = new ArrayList<String>(Arrays.asList(krimifilmek.split(",")));
//         hm.put("krimi",krimifilmeklist);
//         
//         String scififilmek = "Mátrix,Vissza a jövőbe,Eredet,12 majom,Avatar,Mátrix újratöltve,Jurassic Park,Az ötödik elem,Star Wars IV. rész: Egy új remény,Donnie Darko,Men in Black - Sötét zsaruk,Star Wars II. rész: A klónok támadása,Star Wars III. rész: A Sith-ek bosszúja,Star Wars VI. rész: A Jedi visszatér,Egy makulátlan elme örök ragyogása,Pillangó-hatás,A bolygó neve: Halál,A Vasember,Legenda vagyok,Mátriy: Forradalmak,Különvélemény,Világok harca,Holnapután,Mechanikus narancs,Jelek,Én, a robot,Az ember gyermeke,Szárnyas fejvadász";
//         List<String> scififilmeklist = new ArrayList<String>(Arrays.asList(scififilmek.split(",")));
//         hm.put("krimi",scififilmeklist);
//         
//         String romantikusfilmek = "Forrest Gump,Shrek,Titanic,Gettó milliomos,Amerikai pite,Trója,Az 50 első randi,Ollókezű Edward,A maszk,Igazából szerelem,Terminál,Ha eljön Joe Black,Bridget Jones naplója,Lesz ez még így se,Csokoládé,Alkonyat,Jumanji,A halott asszony,Micsoda nő!,Csillagpor,Csupasz pisztoly,Született gyilkosok,Mi kell a nőnek?,Annie Hall,Casablanca,Beépített szépség,Az angol beteg,A bölcsek kövére,Amerikába jöttem,Éjfélkor Párizsban,A randiguru,Az ördög Pradát visel,A felolvasó,Vanília égbolt";
//         List<String> romantikusfilmeklist = new ArrayList<String>(Arrays.asList(romantikusfilmek.split(",")));
//         hm.put("krimi",romantikusfilmeklist);
//         
//         String thrillerfilmek = "Harcosok klubja,Mátrix,Hetedik,Kill Bill,Blöff,A sötét lovag,Eredet,A tökéletes trükk,A bárányok hallgatnak,A tégla,Kutyaszorítóban,Viharsziget,Armageddon,Fekete hattyú,Mementó,Terminátor - A halálosztó,Nem vénnek való vidék,A függetlenség napja,Ragyogás,Pillangó-hatás,A bolygó neve: Halál,Az ördög ügyvédje,Mátrix: Forradalmak,Különvélemény,Ál/arc,Fűrész,Alkonyattól pirkadatig,A fülke,Pánikszoba,Elrabolva,Világok harca,Hannibal,Mechanikus narancs,Jelek,Halálos fegyver";
//         List<String> thrillerfilmeklist = new ArrayList<String>(Arrays.asList(thrillerfilmek.split(",")));
//         hm.put("krimi",thrillerfilmeklist);
//         
//         String vigjatekfilmek = "Forrest Gump,Ponyvaregény,Blöff,Vissza a jövőbe,Másnaposok,Kapj el, ha tudsz,Amerikai pite,Truman show,Jégkorszak,Esőember,Reszkessetek, betörők!,Alkonyattól pirkadatig,Oroszlánkirály,Terminál,Szörny Rt.,A bakancslista,Apádra ütök,Bridget Jones naplója,Halálos fegyver,Lesz ez még így se,Csokoládé,Nagy hal,A hihetetlen család,Fel,Madagaszkár,Támad a Mars!,A halott menyasszony,Nicsak, ki beszél?,Idétlen időkig";
//         List<String> vigjatekfilmeklist = new ArrayList<String>(Arrays.asList(vigjatekfilmek.split(",")));
//         hm.put("krimi",vigjatekfilmeklist);
//         
//         
//         try {
//			testQuerySearchResults(hm);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
         
         
//         TextCategorizationTest tdl = new TextCategorizationTest();
//         try {
//			tdl.test();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
		MyFilteredClassifier mfc = new MyFilteredClassifier();
		mfc.makeClassifier();
	
//		MyFilteredLearner mfl = new MyFilteredLearner();
//		mfl.makeLearner();
	}
	
    public static void testQuerySearchResults(HashMap <String,List<String>> hm) throws IOException {
    	
    	for (Map.Entry<String, List<String>> entry : hm.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            
          	File parentDir = new File("/Users/huszarcsaba/Desktop/funspotter_tag/"+entry.getKey());
        	parentDir.mkdir();
        	
        	List<String> newList = entry.getValue();
        	
        	for (int i=0;i<newList.size();i++){
            	String fileName = Integer.toString(i) + ".txt";
            	File file = new File(parentDir, fileName);
            	file.createNewFile(); // Creates file crawl_html/abc.txt
            	
            	String plainstr = downloadWikiPlainText(newList.get(i));
            	BufferedWriter out = new BufferedWriter(new FileWriter(file));
            	if(plainstr!=null){
            		 out.write(plainstr);
            	}
                out.close();
        	}           
        }	
    }
    public static String downloadWikiPlainText(String aName){
    	String ret = null;
        User user = new User("", "", "http://hu.wikipedia.org/w/api.php");
        user.login();
        // search for all pages which contain "forrest gump"
        String[] valuePairs = { "list", "search", "srsearch", aName,"srwhat","nearmatch" };
        String[] valuePairsContinue = new String[6];
        String srOffset = "0";
        for (int i = 0; i < valuePairs.length; i++) {
                valuePairsContinue[i] = valuePairs[i];
        }
        valuePairsContinue[4] = "sroffset";
        valuePairsContinue[5] = "";
        Connector connector = new Connector();
        List<SearchResult> resultSearchResults = new ArrayList<SearchResult>(1024);
        XMLSearchParser parser;
        try {
                // get all search results
                String responseBody = connector.queryXML(user, valuePairs);
                while (responseBody != null) {
                        parser = new XMLSearchParser(responseBody);
                        parser.parse();
                        srOffset = parser.getSrOffset();
                        System.out.println(">>>>> " + srOffset);
                        List<SearchResult> listOfSearchResults = parser.getSearchResultList();
                        resultSearchResults.addAll(listOfSearchResults);
                        for (SearchResult searchResult : listOfSearchResults) {
                                // print search result information
                                //System.out.println(searchResult.toString());
                        }
                        if (srOffset.length() > 0) {
                                // use the sroffset from the last query to get the next block of
                                // search results
                                valuePairsContinue[5] = srOffset;
                                responseBody = connector.queryXML(user, valuePairsContinue);
                        } else {
                                break;
                        }
                }
                // get the content of the category members with namespace==0
                int count = 0;
                List<String> strList = new ArrayList<String>();
                for (SearchResult searchResult : resultSearchResults) {
                        if (searchResult.getNs().equals("0")) {
                                // namespace "0" - all titles without a namespace prefix
                                strList.add(searchResult.getTitle());
                                if (++count == 10) {
                                        List<Page> listOfPages = user.queryContent(strList);
                                        for (Page page : listOfPages) {
                                                System.out.println(page.getTitle());
                                                // print the raw content of the wiki page:
                                                 //System.out.println(page.getCurrentContent());
                                        }
                                        count = 0;
                                        strList = new ArrayList<String>();
                                }
                        }
                }
                if (count != 0) {
                        List<Page> listOfPages = user.queryContent(strList);
                        for (Page page : listOfPages) {
                                System.out.println(page.getTitle());
                                // print the raw content of the wiki page:
                                 //System.out.println(page.getCurrentContent());
                                 
                                 WikiModel wikiModel = new WikiModel("http://www.mywiki.com/wiki/${text}", "http://www.mywiki.com/wiki/${title}");
                                 String plainStr = wikiModel.render(new PlainTextConverter(), page.getCurrentContent());
                                 //System.out.println(plainStr);
                                 ret = plainStr;
                                 
                        }
                }
        } catch (SAXException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
        return ret;
    }

}

