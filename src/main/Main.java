package main;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONObject;

public class Main {

	private static String indexLocation;

	static int i = 0;
	static String clipboard = "";
	static int count = 0;

	public static void main(String[] args) throws IOException, ParseException {
		try {
			JSONObject json = new JSONObject(args[0]);
			indexLocation = "C:\\Users\\Justkunas\\Documents\\Projects\\Index\\" + json.getString("path");
			// *
			String arg = json.getString("query");

			String workingJson = json.get("filters").toString();
			JSONObject filterObj = new JSONObject(workingJson);

			HashMap<String, Float> scores = new HashMap<String, Float>();

			// *
			StandardAnalyzer analyser = new StandardAnalyzer();
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
			

			try {
				String filters = generateFilterText(filterObj);
				for (String bookQuery : generateQueries(arg)) {
					
					IndexSearcher search = new IndexSearcher(reader);
					TopScoreDocCollector collector = TopScoreDocCollector.create(2781403);
					
					QueryParser parser;
					String subXML = bookQuery.split("/:/g")[0];
					String fullQuery = bookQuery + filters;
					
					switch (subXML) {
					case "place":
						parser = new QueryParser("places", analyser);
						break;

					case "character":
						parser = new QueryParser("characters", analyser);
						break;

					case "seriesitem":
						parser = new QueryParser("series", analyser);
						break;

					case "firstwordsitem":
						parser = new QueryParser("firstwords", analyser);
						break;

					case "lastwordsitem":
						parser = new QueryParser("lastwords", analyser);
						break;

					case "epigraph":
						parser = new QueryParser("epigraph", analyser);
						break;

					case "quotation":
						parser = new QueryParser("quotations", analyser);
						break;

					case "browseNode":
						parser = new QueryParser("browseNodes", analyser);
						break;

					case "subject":
						parser = new QueryParser("subject", analyser);
						break;

					default:
						parser = new QueryParser("content", analyser);
						break;
					}

					parser.setAllowLeadingWildcard(true);
					try {
						Query query = parser.parse(fullQuery);
						//System.out.println(fullQuery);
						// pause.nextLine();
						//System.out.println("Searching");
						search.search(query, collector);
						//System.out.println("Searched");
						ScoreDoc[] results = collector.topDocs().scoreDocs;

						for (ScoreDoc scoreDoc : results) {
							Document document = search.doc(scoreDoc.doc);
							if (scores.containsKey(document.get("path"))) {
								scores.put(document.get("path"), scores.get(document.get("path")) + scoreDoc.score);
							} else {
								scores.put(document.get("path"), scoreDoc.score);
							}
						}
					} catch (Exception err) {
						err.printStackTrace();
					}
				}
			} catch (Exception err) {
				err.printStackTrace();
			}
			//*
			scores.forEach((key, value) -> {
				count++;
				System.out.println(key);// + " - " + count + "/" + scores.size() + " - " + (int)(((double)count/(double)scores.size())*100) + "%" );
			});
			
			//*/
		} catch (Exception error) {
			error.printStackTrace();
		}finally {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}


		// */
	}

	public static String generateFilterText(JSONObject filters) {

		String returnValue = "";

		/*
		JSONObject listprice = new JSONObject(filters.get("listprice").toString());
		JSONObject numberofpages = new JSONObject(filters.get("numberofpages").toString());

		if (listprice.getBoolean("enabled"))
			returnValue += " AND listprice:[" + listprice.getInt("min") + " TO " + listprice.getInt("max") + "]";

		if (numberofpages.getBoolean("enabled"))
			returnValue += " AND numberofpages:[" + numberofpages.getInt("min") + " TO " + numberofpages.getInt("max")+ "]";

		//*/
		
		return returnValue;
	}

	public static String[] generateQueries(String query) {

		ArrayList<String> criteria = new ArrayList<String>();
		
		//*
		criteria.add("isbn:\"" + query + "\"^8");
		criteria.add("ean:\"" + query + "\"^8");
		criteria.add("dewey:\"" + query + "\"^8");

		criteria.add("title:\"" + query + "\"^7");

		criteria.add("subject:\"" + query + "\"^6");
		criteria.add("place:\"" + query + "\"^6");
		criteria.add("character:\"" + query + "\"^6");
		criteria.add("seriesitem:\"" + query + "\"^6");
		criteria.add("browseNode:\"" + query + "\"^6");

		criteria.add("edition:\"" + query + "\"^5");

		criteria.add("firstwordsitem:\"" + query + "\"^4");
		criteria.add("lastwordsitem:\"" + query + "\"^4");
		criteria.add("epigraph:\"" + query + "\"^4");
		
		criteria.add("manufacturer:\"" + query + "\"^3");
		criteria.add("publisher:\"" + query + "\"^3");

		//*/
		criteria.add("label:\"" + query + "\"^2");
		//*
		criteria.add("readinglevel:\"" + query + "\"^2");
		criteria.add("studio:\"" + query + "\"^2");
		criteria.add("quotation:\"" + query + "\"^2");

		criteria.add("binding:\"" + query + "\"");
		//*/

		return criteria.toArray(new String[0]);
	}
}
