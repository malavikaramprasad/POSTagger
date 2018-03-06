import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections.iterators.UniqueFilterIterator;
import org.apache.commons.collections.map.MultiValueMap;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

import edu.stanford.nlp.sentiment.SentimentPipeline;

public class POSTagger {
	
	 public static List<String> tokenize(String text) {
		    Properties props = new Properties();
		    props.setProperty("annotators", "tokenize");
		    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		    // create an empty Annotation just with the given text
		    Annotation document = new Annotation(text);

		    // run all Annotators on this text
		    pipeline.annotate(document);
		    List<CoreLabel> tokens = document.get(TokensAnnotation.class);

		    List<String> result = new ArrayList<String>();
		    for (CoreLabel token : tokens) {
		      // this is the text of the token
		      String word = token.get(TextAnnotation.class);
		      result.add(word);
		    }

		    return result;
		  }
		  
		  public static List<String> posTagging(String text) {
			    Properties props = new Properties();
			    props.setProperty("annotators", "tokenize, ssplit, pos");
			    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			    // create an empty Annotation just with the given text
			    Annotation document = new Annotation(text);

			    // run all Annotators on this text
			    pipeline.annotate(document);
			    List<CoreLabel> tokens = document.get(TokensAnnotation.class);

			    List<String> result = new ArrayList<String>();
			    for (CoreLabel token : tokens) {
			      // this is the text of the token
			      String pos = token.get(PartOfSpeechAnnotation.class);
			      result.add(pos);
			    }

			    return result;
			  }
	
	public static void main(String[] args) {
	    String text ="$ My school has a very big library. \r\n" + 
	    		"$ It is on the ground floor of the school building. \r\n" + 
	    		"$ It has hundreds of books. \r\n" + 
	    		"$ It has books on all subjects. \r\n" + 
	    		"$ It also has many storybooks and magazines.\r\n" + 
	    		"$ Inside the library, there are rows of tables and chairs. \r\n" + 
	    		"$ Big cabinets are kept all around the library. \r\n" + 
	    		"$ Books are alphabetically arranged in these cabinets. \r\n" + 
	    		"$ Students of all classes visit the library as per schedule. \r\n" + 
	    		"$ We all have library cards which we carry to school every day. \r\n" + 
	    		"$ We can borrow one book at a time from the school library.\r\n" + 
	    		"$ Our librarian is a very nice lady. She has a lot of knowledge about books. \r\n" + 
	    		"$ She makes sure all books are returned to the library on time. \r\n" + 
	    		"$ She maintains the library very well. \r\n" + 
	    		"$ Many a time she helps us in locating and selecting books.";
	    text = text.toLowerCase();
	    List<String> tokens = tokenize(text);
	    List<String> posTags = posTagging(text);
	    List<String> posTagsList = new ArrayList<String>();
	    MultiValueMap map = new MultiValueMap();
	    for(int index = 0; index < tokens.size(); index++) {
	    	String token = tokens.get(index);
	    	String posTag = posTags.get(index);	    	
	    	map.put(token, posTag);
	    	posTagsList.add(posTag);
	    }
	    ArrayList<String> al = new ArrayList<String>();
	    PrintWriter writer = null;
	    PrintWriter writer1 = null;
		try {
			writer = new PrintWriter("lex-likelihood-prob.txt", "UTF-8");
			writer1 = new PrintWriter("tag-bigram-prob.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    for(int index = 0; index < tokens.size(); index++) {
	    	double tagWordCount = 0.0;
	    	double tagWordCount1 = 0.0;
		    double tagCount = 0.0;
		    double prevTagCount = 0.0;
	    	String token = tokens.get(index);
	    	String posTag = posTags.get(index);
	    	String prevPosTag="";
			if(index>0) {
	    		prevPosTag = posTags.get(index-1);
			}
	    	tagCount = Collections.frequency(new ArrayList<String>(map.values()), posTag);
			prevTagCount = Collections.frequency(new ArrayList<String>(map.values()), prevPosTag);
	    	if(index ==0){
	    		prevTagCount = 15;
	    	}
	    	al = (ArrayList<String>) map.getCollection(token);
	    	for(int i=0;i<al.size();i++)
	    	{
	    		if(al.get(i) == posTag)
	    			tagWordCount++;
	    	}
	    	for(int j=1; j<posTagsList.size(); j++)
	    	{
	    		if(posTagsList.get(j).equals(posTag) && posTagsList.get(j-1).equals(prevPosTag) && !prevPosTag.equals(".")) {
	    			tagWordCount1++;
	    		}
	    	}
	    	double prob = tagWordCount / tagCount;
	    	double biGramprob = tagWordCount1 / prevTagCount;
	    	if(prob!=0)
	    		writer.println(token + "/" +  posTag + ": " + prob);
	    	if(biGramprob!=0)
	    		writer1.println(posTag + "/" +  prevPosTag + ": " + biGramprob);
	    }
	    writer.close();
	    writer1.close();
	}
}
