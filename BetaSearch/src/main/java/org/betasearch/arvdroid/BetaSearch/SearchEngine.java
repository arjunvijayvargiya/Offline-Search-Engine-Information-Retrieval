package org.betasearch.arvdroid.BetaSearch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Paths;

/**
 * @author avijayvargiy <br>
 * @version 1.0 <br>
 * SearchEngine class that incorporates the entire engine and is the gateway /\ <br>
 * <pre>
 * {@code
 *  SearchEngine se = new SearchEngine(directory full path like C:\\Users\\avijayvargiy\\Desktop\\..);
 *  se.build();
 *  se.query(querytext);
 * }
 * </pre>
 */
public class SearchEngine {

	/**
	 * @param dirPath
	 */
	public SearchEngine(String dirPath, String resultType) {
		this.dirPath = dirPath;
		this.resultType = resultType;
	}
	/**
	 * directory path where all the .txts are present.
	 */
	public String dirPath;

	/**
	 * set of words representing dictionary
	 */
	public LinkedList < Dictionary > words;

	/**
	 * number of documents in a directory
	 */
	public int N;

	/**
	 * file array holding the all the offline documents
	 */
	public File F[];

	/**
	 * result type: full path or just names of the documents etc. you can use the ResultType class to get valid types.
	 */
	public String resultType;

	public String getResultType() {
		return resultType;
	}

	public String getDirPath() {
		return dirPath;
	}

	public LinkedList < Dictionary > getWords() {
		return words;
	}

	public int getN() {
		return N;
	}

	public File[] getF() {
		return F;
	}

	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	public void setWords(LinkedList < Dictionary > words) {
		this.words = words;
	}

	public void setN(int n) {
		N = n;
	}

	public void setF(File[] f) {
		F = f;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

	/**
	 * builds the dictionary from the given set of documents in a directory<br>
	 * CONSTRUCTION OF DICTIONARY<br>
	 *  1)picking of single word form each and every doc.(Tokenization)<br>
	 *  2)Stemming that single word.<br>
	 *  3)Filtering for letters i.e. separating out non-alphabets out of original word formed.<br>
	 *  4)putting into words dictionary and maintaining dictionary.<br>
	 *  5)If word is already present than maintain its data structure of list of postings.<br>
	 */
	public void build() {
		words = new LinkedList < Dictionary > (); //that holds set of words in a dictionary
		Posting q1 = new Posting();
		q1.documentName = "data1";
		q1.termFrequency = 1;
		Dictionary q2 = new Dictionary();
		q2.term = "dfhjdfhdjfhdjf"; //dummy word to initialize in the dictionary.
		q2.documentFrequency++;
		q2.list = new LinkedList < Posting > ();
		q2.list.add(q1);
		words.add(q2);
		BufferedReader br = null;
		Filter f = new Filter(); // for doing the file picking of .txt files from the specified directory
		F = new File[1000]; //
		F = f.finder(dirPath);
		N = F.length; //is the number of documents in the directory
		for (int i = 0; i < N; i++) {
			System.out.println(F[i].getName());
		}

		/*
          CONSTRUCTION OF DICTIONARY
          1)picking of single word form each and every doc.(Tokenization)
          2)Stemming that single word.
          3)Filtering for letters i.e. separating out non-alphabets out of original word formed
          4)putting into words dictionary and maintaining dictionary
          5)If word is already present than maintain its data structure of list of postings
		 */
		for (int i = 0; i < N; i++) {
			try {

				String sCurrentLine;
				String path = F[i].getAbsolutePath();
				String name = F[i].getName();
				int posh = name.lastIndexOf(".");
				if (posh > 0) {
					name = name.substring(0, posh);
				}
				String currentDoc = name;
				br = new BufferedReader(new FileReader(path));

				while ((sCurrentLine = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(sCurrentLine);
					while (st.hasMoreTokens()) {
						String termtemp = st.nextToken();
						termtemp = termtemp.toLowerCase();
						Stemmer s = new Stemmer();
						char buffer[] = termtemp.toCharArray();
						int bufferlen = buffer.length;
						s.add(buffer, bufferlen);
						s.stem();
						termtemp = s.toString();
						int i1 = 0;
						int j = 0;
						char finalbuffer[] = termtemp.toCharArray();
						char tempbuffer[] = new char[100];
						while (i1 < finalbuffer.length) {
							if (Character.isLetter(finalbuffer[i1])) {
								tempbuffer[j++] = finalbuffer[i1];
							}
							i1++;
						}
						tempbuffer[j] = '\0';
						String l = new String(tempbuffer);
						termtemp = l;
						ListIterator < Dictionary > listIterator = words.listIterator();
						int flag = 0;
						while (listIterator.hasNext()) {     
							Dictionary dtemp = listIterator.next();
							if (dtemp.term.equals(termtemp)) {
								flag = 1;
								ListIterator < Posting > postinglist = (dtemp.list).listIterator();
								int flag2 = 0;
								while (postinglist.hasNext()) {
									Posting pos = postinglist.next();
									if (pos.documentName.equals(currentDoc)) {
										flag2 = 1;
										pos.termFrequency++;
										break;
									}
								}
								if (flag2 != 1) {
									Posting npos = new Posting();
									npos.documentName = currentDoc;
									npos.termFrequency++;
									postinglist.add(npos);
									dtemp.documentFrequency++;

								}
							}
						}
						if (flag != 1) {
							Posting np = new Posting();
							np.documentName = currentDoc;
							np.termFrequency = 1;
							Dictionary d = new Dictionary();
							d.term = termtemp;
							d.documentFrequency++;
							d.list = new LinkedList < Posting > ();
							d.list.add(np);
							words.add(d);
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null) br.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}

		}
		Collections.sort(words); //sorting the dictionary alphabetically
		//printing the posting lists
		ListIterator < Dictionary > li1 = words.listIterator();
		while (li1.hasNext()) {
			Dictionary d1 = li1.next();
			System.out.print("word=" + d1.term.trim() + " ");
			System.out.print("(" + d1.documentFrequency + ")" + "->");
			ListIterator < Posting > pl1 = (d1.list).listIterator();
			while (pl1.hasNext()) {
				Posting p2 = pl1.next();
				System.out.print(p2.documentName + "(" + p2.termFrequency + "),");
			}
			System.out.println();
		}
	}

	/**
	 * @param query user query
	 * @return queryresult containing arranged documents in sorted format
	 * @throws Exception 
	 */
	public String[] query(String query) throws Exception { 
		//Main Algorithm Cosin Similarity
		//String query="Hobbit baggins ring hobbit tolkein";
		//String query=querytext; //getting the qury from the user
		/*
                   processing the query
                   1)Maintaing the linked list of data structure query term.
                   2)tokenization of each and every term in the query
                   3)stemming of term
                   4)putting of that query term into the list along with calculating the no of occurences
                    of that particular term
		 */

		LinkedList < QueryTerm > qt = new LinkedList < QueryTerm > ();
		int flag4 = 0;
		int termcount = 0;
		StringTokenizer st = new StringTokenizer(query);
		while (st.hasMoreTokens()) {
			String termtemp = st.nextToken();
			termtemp = termtemp.toLowerCase();
			Stemmer s = new Stemmer();
			char buffer[] = termtemp.toCharArray();
			int bufferlen = buffer.length;
			s.add(buffer, bufferlen);
			s.stem();
			termtemp = s.toString();
			int i1 = 0;
			int j = 0;
			char finalbuffer[] = termtemp.toCharArray();
			char tempbuffer[] = new char[100];
			while (i1 < finalbuffer.length) {
				if (Character.isLetter(finalbuffer[i1])) {
					tempbuffer[j++] = finalbuffer[i1];
				}
				i1++;
			}
			tempbuffer[j] = '\0';
			String l = new String(tempbuffer);
			termtemp = l;
			if (flag4 == 0) {
				QueryTerm q = new QueryTerm();
				q.termname = termtemp;
				q.TermFrequency++;
				qt.add(q);
				termcount++;
				flag4 = 1;
			} else {
				ListIterator < QueryTerm > qli = qt.listIterator();
				int flag5 = 0;
				while (qli.hasNext()) {
					QueryTerm qlitemp = qli.next();
					if (qlitemp.termname.equals(termtemp)) {
						flag5 = 1;
						qlitemp.TermFrequency++;
						break;
					}
				}
				if (flag5 == 0) {
					QueryTerm nqt = new QueryTerm();
					nqt.termname = termtemp;
					nqt.TermFrequency++;
					qt.add(nqt);
					termcount++;
				}
			}
		}
		/*
                   Now the below processing calculates the query parameter
                   1)matrix[]=tf-wt for each term in the query
                   2)matrix2[]=iDF for each term
                   3)matrix3[]=wt-Q followed by normalization
		 */
		ListIterator < QueryTerm > qli2 = qt.listIterator();
		double matrix[] = new double[termcount];
		int i7 = 0;
		while (qli2.hasNext()) {
			QueryTerm qqt = qli2.next();
			matrix[i7] = 1 + Math.log(qqt.TermFrequency);
			i7++;
		}
		//traverse all documents
		double matrix2[] = new double[termcount];
		i7 = 0;
		ListIterator < QueryTerm > qit = qt.listIterator();
		while (qit.hasNext()) {
			String s = qit.next().termname;
			ListIterator < Dictionary > lid = words.listIterator();
			while (lid.hasNext()) {
				Dictionary dt = lid.next();
				if (dt.term.equals(s)) {
					matrix2[i7] = Math.log((double) N / dt.documentFrequency);
					i7++;
					break;
				}
			}
		}
		double matrix3[] = new double[termcount];
		for (int i = 0; i < termcount; i++) {
			matrix3[i] = matrix[i] * matrix2[i];
			System.out.println("matrix[" + i + "]=" + matrix3[i]);
		}
		//Normalization
		double norm = 0.0;
		for (int i = 0; i < termcount; i++) {
			norm = norm + (matrix3[i] * matrix3[i]);
		}
		norm = Math.sqrt(norm);
		for (int i = 0; i < termcount; i++) {
			matrix3[i] = matrix3[i] / norm;
		}
		//matrix3 contains normalized values for queries.
		double normdoc[] = new double[N]; //normdoc conatins the normalization values of each and every doc
		for (int i = 0; i < N; i++) {
			normdoc[i] = 0.0;
		}
		for (int i = 0; i < N; i++) {
			String name = F[i].getName();
			int pos = name.lastIndexOf(".");
			if (pos > 0) {
				name = name.substring(0, pos);
			}
			String currentdoc = name;
			ListIterator < Dictionary > ddt = words.listIterator();
			while (ddt.hasNext()) {
				Dictionary dq = ddt.next();
				ListIterator < Posting > tft = dq.list.listIterator();
				while (tft.hasNext()) {
					Posting p = tft.next();
					if (p.documentName.equals(currentdoc)) {
						double d = 1 + Math.log(p.termFrequency);
						d = d * d;
						normdoc[i] = normdoc[i] + d;
						break;
					}
				}
			}
		}
		for (int i = 0; i < N; i++) {
			normdoc[i] = Math.sqrt(normdoc[i]);
		}
		double score[] = new double[N]; //array for scoring each and every doc
		for (int qq = 0; qq < N; qq++) {
			score[qq] = 0.0;
		}
		for (int i = 0; i < N; i++) {
			String name = F[i].getName();
			int pos = name.lastIndexOf(".");
			if (pos > 0) {
				name = name.substring(0, pos);
			}
			String curdoc = name;
			double termmatrix[] = new double[termcount];
			for (int k = 0; k < termcount; k++) {
				termmatrix[k] = 0.0;
			}
			double finalmatrix[] = new double[termcount];
			for (int k = 0; k < termcount; k++) {
				finalmatrix[k] = 0.0;
			}
			int termcounter = 0;
			ListIterator < QueryTerm > qa = qt.listIterator();
			while (qa.hasNext()) {
				QueryTerm q5 = qa.next();
				String stx = q5.termname;
				ListIterator < Dictionary > li = words.listIterator();
				while (li.hasNext()) {
					Dictionary lp = li.next();
					if (lp.term.equals(stx)) {
						ListIterator < Posting > ltt = lp.list.listIterator();
						int found = 0;
						while (ltt.hasNext()) {
							Posting docn = ltt.next();
							if (docn.documentName.equals(curdoc)) {
								found = 1;
								termmatrix[termcounter] = docn.termFrequency / normdoc[i];
								finalmatrix[termcounter] = termmatrix[termcounter] * matrix3[termcounter];
								score[i] = score[i] + finalmatrix[termcounter];
								termcounter++;
								break;
							}

						}
						if (found == 0) {
							termcounter++;
						}
					}
				}

			}

		}

		for (int i = 0; i < N; i++) {
			score[i] = score[i] / normdoc[i];
			System.out.println("normdoc[" + i + "]" + "=" + normdoc[i]);
		}
		/*
                        Bubble sort the documents list based on their scores.
                        and also manipulating the file array.

		 */
		for (int i = 0; i < N - 1; i++) {
			for (int j = 0; j < N - i - 1; j++) {
				if (score[j] < score[j + 1]) {
					double temp = score[j];
					score[j] = score[j + 1];
					score[j + 1] = temp;
					File ftemp = F[j];
					F[j] = F[j + 1];
					F[j + 1] = ftemp;
				}
			}
		}
		for (int i = 0; i < N; i++) {
			System.out.println("Score[" + i + "]" + "=" + score[i]);
			System.out.println("F[" + i + "]" + "=" + F[i].getAbsolutePath());
		}

		/*
            Collecting all the result and putting into proper format
		 */

		String result[] =new String[N];
		if(resultType.equals(ResultType.FULL_PATH)){
			for (int i = 0; i < N; i++) {
				 try {
					result[i] = Paths.get(F[i].getAbsolutePath()).toRealPath().toUri().toURL().toString();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else if(resultType.equals(ResultType.DOCUMENT_NAME)){
			for(int i=0; i<N;i++){
				result[i] = F[i].getName();
			}
		}else{
			throw new Exception("result type not valid");
		}
		return result;
	}
}