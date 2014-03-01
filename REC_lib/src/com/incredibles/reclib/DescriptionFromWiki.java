package com.incredibles.reclib;

import info.bliki.api.Connector;
import info.bliki.api.Page;
import info.bliki.api.SearchResult;
import info.bliki.api.User;
import info.bliki.api.XMLSearchParser;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

public class DescriptionFromWiki {
	
	    public String downloadWikiPlainText(String aName){
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
	                        //System.out.println(">>>>> " + srOffset);
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
	                                                //System.out.println(page.getTitle());
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
	                                //System.out.println(page.getTitle());
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
