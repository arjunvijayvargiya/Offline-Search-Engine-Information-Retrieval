import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
/**
 *
 * @author Arjun Vijayvargiya & Bikkumala Karthik
 */
//Stemmer class for implementing porter's algorithm
/*
   Porter stemmer in Java. The original paper is in

       Porter, 1980, An algorithm for suffix stripping, Program, Vol. 14,
       no. 3, pp 130-137,

   See also http://www.tartarus.org/~martin/PorterStemmer

   History:

   Release 1

   Bug 1 (reported by Gonzalo Parra 16/10/99) fixed as marked below.
   The words 'aed', 'eed', 'oed' leave k at 'a' for step 3, and b[k-1]
   is then out outside the bounds of b.

   Release 2

   Similarly,

   Bug 2 (reported by Steve Dyrdahl 22/2/00) fixed as marked below.
   'ion' by itself leaves j = -1 in the test for 'ion' in step 5, and
   b[j] is then outside the bounds of b.

   Release 3

   Considerably revised 4/9/00 in the light of many helpful suggestions
   from Brian Goetz of Quiotix Corporation (brian@quiotix.com).

   Release 4

*/
/**
  * Stemmer, implementing the Porter Stemming Algorithm
  *
  * The Stemmer class transforms a word into its root form.  The input
  * word can be provided a character at time (by calling add()), or at once
  * by calling one of the various stem(something) methods.
  */
class Stemmer
{  private char[] b;
   private int i,     /* offset into b */
               i_end, /* offset to end of stemmed word */
               j, k;
   private static final int INC = 50;
                     /* unit of size whereby b is increased */
   public Stemmer()
   {  b = new char[INC];
      i = 0;
      i_end = 0;
   }
   /**
    * Add a character to the word being stemmed.  When you are finished
    * adding characters, you can call stem(void) to stem the word.
    */
   public void add(char ch)
   {  if (i == b.length)
      {  char[] new_b = new char[i+INC];
         for (int c = 0; c < i; c++) new_b[c] = b[c];
         b = new_b;
      }
      b[i++] = ch;
   }
   /** Adds wLen characters to the word being stemmed contained in a portion
    * of a char[] array. This is like repeated calls of add(char ch), but
    * faster.
    */
   public void add(char[] w, int wLen)
   {  if (i+wLen >= b.length)
      {  char[] new_b = new char[i+wLen+INC];
         for (int c = 0; c < i; c++) new_b[c] = b[c];
         b = new_b;
      }
      for (int c = 0; c < wLen; c++) b[i++] = w[c];
   }
   /**
    * After a word has been stemmed, it can be retrieved by toString(),
    * or a reference to the internal buffer can be retrieved by getResultBuffer
    * and getResultLength (which is generally more efficient.)
    */
   public String toString() { return new String(b,0,i_end); }
   /**
    * Returns the length of the word resulting from the stemming process.
    */
   public int getResultLength() { return i_end; }
   /**
    * Returns a reference to a character buffer containing the results of
    * the stemming process.  You also need to consult getResultLength()
    * to determine the length of the result.
    */
   public char[] getResultBuffer() { return b; }
   /* cons(i) is true <=> b[i] is a consonant. */
   private final boolean cons(int i)
   {  switch (b[i])
      {  case 'a': case 'e': case 'i': case 'o': case 'u': return false;
         case 'y': return (i==0) ? true : !cons(i-1);
         default: return true;
      }
   }
   /* m() measures the number of consonant sequences between 0 and j. if c is
      a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
      presence,

         <c><v>       gives 0
         <c>vc<v>     gives 1
         <c>vcvc<v>   gives 2
         <c>vcvcvc<v> gives 3
         ....
   */
   private final int m()
   {  int n = 0;
      int i = 0;
      while(true)
      {  if (i > j) return n;
         if (! cons(i)) break; i++;
      }
      i++;
      while(true)
      {  while(true)
         {  if (i > j) return n;
               if (cons(i)) break;
               i++;
         }
         i++;
         n++;
         while(true)
         {  if (i > j) return n;
            if (! cons(i)) break;
            i++;
         }
         i++;
       }
   }
   /* vowelinstem() is true <=> 0,...j contains a vowel */
   private final boolean vowelinstem()
   {  int i; for (i = 0; i <= j; i++) if (! cons(i)) return true;
      return false;
   }
   /* doublec(j) is true <=> j,(j-1) contain a double consonant. */
   private final boolean doublec(int j)
   {  if (j < 1) return false;
      if (b[j] != b[j-1]) return false;
      return cons(j);
   }
   /* cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
      and also if the second c is not w,x or y. this is used when trying to
      restore an e at the end of a short word. e.g.

         cav(e), lov(e), hop(e), crim(e), but
         snow, box, tray.
   */
   private final boolean cvc(int i)
   {  if (i < 2 || !cons(i) || cons(i-1) || !cons(i-2)) return false;
      {  int ch = b[i];
         if (ch == 'w' || ch == 'x' || ch == 'y') return false;
      }
      return true;
   }
   private final boolean ends(String s)
   {  int l = s.length();
      int o = k-l+1;
      if (o < 0) return false;
      for (int i = 0; i < l; i++) if (b[o+i] != s.charAt(i)) return false;
      j = k-l;
      return true;
   }
   /* setto(s) sets (j+1),...k to the characters in the string s, readjusting
      k. */
   private final void setto(String s)
   {  int l = s.length();
      int o = j+1;
      for (int i = 0; i < l; i++) b[o+i] = s.charAt(i);
      k = j+l;
   }
   /* r(s) is used further down. */
   private final void r(String s) { if (m() > 0) setto(s); }
   /* step1() gets rid of plurals and -ed or -ing. e.g.

          caresses  ->  caress
          ponies    ->  poni
          ties      ->  ti
          caress    ->  caress
          cats      ->  cat

          feed      ->  feed
          agreed    ->  agree
          disabled  ->  disable

          matting   ->  mat
          mating    ->  mate
          meeting   ->  meet
          milling   ->  mill
          messing   ->  mess

          meetings  ->  meet

   */
   private final void step1()
   {  if (b[k] == 's')
      {  if (ends("sses")) k -= 2; else
         if (ends("ies")) setto("i"); else
         if (b[k-1] != 's') k--;
      }
      if (ends("eed")) { if (m() > 0) k--; } else
      if ((ends("ed") || ends("ing")) && vowelinstem())
      {  k = j;
         if (ends("at")) setto("ate"); else
         if (ends("bl")) setto("ble"); else
         if (ends("iz")) setto("ize"); else
         if (doublec(k))
         {  k--;
            {  int ch = b[k];
               if (ch == 'l' || ch == 's' || ch == 'z') k++;
            }
         }
         else if (m() == 1 && cvc(k)) setto("e");
     }
   }
   /* step2() turns terminal y to i when there is another vowel in the stem. */
   private final void step2() { if (ends("y") && vowelinstem()) b[k] = 'i'; }

   /* step3() maps double suffices to single ones. so -ization ( = -ize plus
      -ation) maps to -ize etc. note that the string before the suffix must give
      m() > 0. */
   private final void step3() { if (k == 0) return; /* For Bug 1 */ switch (b[k-1])
   {
       case 'a': if (ends("ational")) { r("ate"); break; }
                 if (ends("tional")) { r("tion"); break; }
                 break;
       case 'c': if (ends("enci")) { r("ence"); break; }
                 if (ends("anci")) { r("ance"); break; }
                 break;
       case 'e': if (ends("izer")) { r("ize"); break; }
                 break;
       case 'l': if (ends("bli")) { r("ble"); break; }
                 if (ends("alli")) { r("al"); break; }
                 if (ends("entli")) { r("ent"); break; }
                 if (ends("eli")) { r("e"); break; }
                 if (ends("ousli")) { r("ous"); break; }
                 break;
       case 'o': if (ends("ization")) { r("ize"); break; }
                 if (ends("ation")) { r("ate"); break; }
                 if (ends("ator")) { r("ate"); break; }
                 break;
       case 's': if (ends("alism")) { r("al"); break; }
                 if (ends("iveness")) { r("ive"); break; }
                 if (ends("fulness")) { r("ful"); break; }
                 if (ends("ousness")) { r("ous"); break; }
                 break;
       case 't': if (ends("aliti")) { r("al"); break; }
                 if (ends("iviti")) { r("ive"); break; }
                 if (ends("biliti")) { r("ble"); break; }
                 break;
       case 'g': if (ends("logi")) { r("log"); break; }
   } }
   /* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */
   private final void step4() { switch (b[k])
   {
       case 'e': if (ends("icate")) { r("ic"); break; }
                 if (ends("ative")) { r(""); break; }
                 if (ends("alize")) { r("al"); break; }
                 break;
       case 'i': if (ends("iciti")) { r("ic"); break; }
                 break;
       case 'l': if (ends("ical")) { r("ic"); break; }
                 if (ends("ful")) { r(""); break; }
                 break;
       case 's': if (ends("ness")) { r(""); break; }
                 break;
   } }
   /* step5() takes off -ant, -ence etc., in context <c>vcvc<v>. */
   private final void step5()
   {   if (k == 0) return; /* for Bug 1 */ switch (b[k-1])
       {  case 'a': if (ends("al")) break; return;
          case 'c': if (ends("ance")) break;
                    if (ends("ence")) break; return;
          case 'e': if (ends("er")) break; return;
          case 'i': if (ends("ic")) break; return;
          case 'l': if (ends("able")) break;
                    if (ends("ible")) break; return;
          case 'n': if (ends("ant")) break;
                    if (ends("ement")) break;
                    if (ends("ment")) break;
                    /* element etc. not stripped before the m */
                    if (ends("ent")) break; return;
          case 'o': if (ends("ion") && j >= 0 && (b[j] == 's' || b[j] == 't')) break;
                                    /* j >= 0 fixes Bug 2 */
                    if (ends("ou")) break; return;
                    /* takes care of -ous */
          case 's': if (ends("ism")) break; return;
          case 't': if (ends("ate")) break;
                    if (ends("iti")) break; return;
          case 'u': if (ends("ous")) break; return;
          case 'v': if (ends("ive")) break; return;
          case 'z': if (ends("ize")) break; return;
          default: return;
       }
       if (m() > 1) k = j;
   }
   /* step6() removes a final -e if m() > 1. */
   private final void step6()
   {  j = k;
      if (b[k] == 'e')
      {  int a = m();
         if (a > 1 || a == 1 && !cvc(k-1)) k--;
      }
      if (b[k] == 'l' && doublec(k) && m() > 1) k--;
   }
   /** Stem the word placed into the Stemmer buffer through calls to add().
    * Returns true if the stemming process resulted in a word different
    * from the input.  You can retrieve the result with
    * getResultLength()/getResultBuffer() or toString().
    */
   public void stem()
   {  k = i - 1;
      if (k > 1) { step1(); step2(); step3(); step4(); step5(); step6(); }
      i_end = k+1; i = 0;
   }
}
/*
   Query term is a data structure that holds termname and its frequency in a query.
   for example, we have a query like "my game is game"
   a linked list of query term will hold list-><"my",1>-><"game",2>->"is",1>
   This is helpful in calculating one of the component of cosine similarity algorithm that is Wt(q)
*/
class QueryTerm{
    String termname;
    int TermFrequency=0;
}
/*
  Posting represents individual postings in a posting list of dictionary words
  it constitutes two variables document name and term frequency.
*/
class Posting{
    String documentName; //document name which represents doc name 
    int termFrequency; //term frequency represents the no of times that particular word occurs in that document
}
/*
  Dictionary represents the individual word ina Dictionary and its posting list containing variables 
  of type posting.

*/
class Dictionary implements Comparable<Dictionary>{
    LinkedList<Posting> list; //It represents postings of that particular word in a dictionary.
    String term; //It represents word or the term.
    int documentFrequency=0; //It represents how many documents that particular word has occured
    @Override
    public int compareTo(Dictionary o) //function for sorting the dictionary in lexicographic manner
    {
		String comparedSize = o.term;
		if (this.term.compareTo(comparedSize)>0) 
                {
			return 1;
		} 
                else if (this.term.compareTo(comparedSize)<0) 
                {
			return -1;
		} 
                else 
                {
			return 0;
		}
    }
}
/*
  Class Filter helps in keeping track of various documents present in a directory. It is basically a file picker.
*/
class Filter {

    public File[] finder( String dirName) //It helps in picking all files of .txt extention in adirectory dirName.
    {
    	File dir = new File(dirName);

    	return dir.listFiles(new FilenameFilter() { 
    	         public boolean accept(File dir, String filename)
    	              { return filename.endsWith(".txt"); }
    	} );

    }

}
/*
   The main class that implements the vector space model and represents it in a GUI format to user.
*/
public class InformationRetreivalBeta extends Frame implements ActionListener{
    /*
         Labels/textFields etc. are all awt components related to GUI of the software.
    */
    static Label l1,l2,l3,l4,l5; 
    static TextField tf1,tf2,tf3;
    static Button b1,b2;
    static TextArea ta1;
    static String dirPath;//directory path where all the .txts are present.
    static boolean dirset=false;
    public void actionPerformed(ActionEvent ae) //action method for search and set button carries out search/set.
    {   
        String comp=ae.getActionCommand();
        if(comp=="search")
        {
        if(tf1.getText().toString().isEmpty()) //for checking empty query
        {
          tf2.setText("SEARCH QUERY EMPTY !!!");
          
        }
        else if(dirset==false) //for checking directory path missing
        {
          tf2.setText("DIRECTORY PATH MISSING !!!");    
        }
        else
        {
        tf2.setText("LOADING ...");
	LinkedList<Dictionary> words=new LinkedList<Dictionary>(); //that holds set of words in a dictionary
        Posting q1=new Posting();
        int N;
        q1.documentName="data1";
        q1.termFrequency=1;
        Dictionary q2=new Dictionary();
        q2.term="dfhjdfhdjfhdjf"; //dummy word to initialize in the dictionary.
        q2.documentFrequency++;
        q2.list=new LinkedList<Posting>();
        q2.list.add(q1);
        words.add(q2);
        BufferedReader br = null;
        Filter f=new Filter();// for doing the file picking of .txt files from the specified directory
        File F[]=new File[1000];//
        //F=f.finder("F:\\IR");
        F=f.finder(dirPath);
        N=F.length;  //is the number of documents in the directory
        for(int i=0;i<N;i++)
        {
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
        for(int i=0;i<N;i++)
        {//System.out.println("FILE@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+i);
		try {

			String sCurrentLine;
                        //String path="F:\\IR\\data"+i+".txt";
                          String path=F[i].getAbsolutePath();
                        //String path="C:\\Users\\ravi\\Desktop\\IRTest\\data"+i+".txt";
                        String name = F[i].getName();
                        int posh = name.lastIndexOf(".");
                        if (posh > 0) 
                        {
                          name = name.substring(0, posh);
                        }
                        String currentDoc=name;
			br = new BufferedReader(new FileReader(path));
                        
			while ((sCurrentLine = br.readLine()) != null) 
                        {
				//System.out.println(sCurrentLine);
			     StringTokenizer st=new StringTokenizer(sCurrentLine);
                             while(st.hasMoreTokens())
                             {
                                  //System.out.println(st.nextToken()+ " ");
                                  String termtemp=st.nextToken();
                                  termtemp=termtemp.toLowerCase();
                                  Stemmer s=new Stemmer();
                                  char buffer[]=termtemp.toCharArray();
                                  int bufferlen=buffer.length;
                                  s.add(buffer,bufferlen);
                                  s.stem();
                                  termtemp=s.toString();
                                  int i1=0;
                                  int j=0;
                                  char finalbuffer[]=termtemp.toCharArray();
                                  char tempbuffer[]=new char[100];
                                  while(i1<finalbuffer.length)
                                  {
                                      if(Character.isLetter(finalbuffer[i1]))
                                      {
                                          tempbuffer[j++]=finalbuffer[i1];
                                      }
                                      i1++;
                                  }
                                  tempbuffer[j]='\0';
                                  String l=new String(tempbuffer);
                                  termtemp=l;
                                  //System.out.println("getting buffer after stem:"+termtemp);
                                  ListIterator<Dictionary> listIterator = words.listIterator();
                                  int flag=0;
                                  while(listIterator.hasNext()) 
                                  {
	                              //System.out.println("inside");             
                                      Dictionary dtemp=listIterator.next();
                                       //System.out.println(dtemp.term);
                                      if(dtemp.term.equals(termtemp))
                                      { flag=1;
                                          ListIterator<Posting> postinglist= (dtemp.list).listIterator();
                                          int flag2=0; 
                                          while(postinglist.hasNext())
                                          { Posting pos=postinglist.next();
                                             if(pos.documentName.equals(currentDoc))
                                             {
                                                flag2=1;
                                                pos.termFrequency++;
                                                break;
                                             }
                                          }
                                          if(flag2!=1)
                                          {
                                            Posting npos=new Posting();
                                            npos.documentName=currentDoc;
                                            npos.termFrequency++;
                                            postinglist.add(npos);
                                            dtemp.documentFrequency++;
                                            
                                          }
                                      }
                                  }
                                      if(flag!=1)
                                      {
                                          Posting np=new Posting();
                                          np.documentName=currentDoc;
                                          np.termFrequency=1;
                                          Dictionary d=new Dictionary();
                                          d.term=termtemp;
                                          d.documentFrequency++;
                                          d.list=new LinkedList<Posting>();
                                          d.list.add(np);
                                          words.add(d);
                                          //System.out.println("added term");
                                         
                                      }
	                          
                                 
                             }
                        }

		} catch (IOException e) {
		     e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
                
                }
            
        }
        //System.out.println("--------------------------------------------------------------------------");
        Collections.sort(words);  //sorting the dictionary alphabetically
            //printing the posting lists
            ListIterator<Dictionary> li1 = words.listIterator();
             while(li1.hasNext())
             {
                 Dictionary d1=li1.next();
                 System.out.print("word="+d1.term);
                 System.out.print("("+d1.documentFrequency+")"+"->");
                 ListIterator<Posting> pl1=(d1.list).listIterator();
                 while(pl1.hasNext())
                 {
                     Posting p2=pl1.next();
                     System.out.print(p2.documentName+"("+p2.termFrequency+"),");
                 }
                 System.out.print("\n");
             }
        //System.out.println("--------------------------------------------------------------------------");     
        //Main Algorithm Cosin Similarity
        //String query="Hobbit baggins ring hobbit tolkein";
        String query=tf1.getText().toString(); //getting the qury from the user
        /*
           processing the query
           1)Maintaing the linked list of data structure query term.
           2)tokenization of each and every term in the query
           3)stemming of term
           4)putting of that query term into the list along with calculating the no of occurences
            of that particular term
        */
        String querydoc="querydoc";
        LinkedList<QueryTerm> qt=new LinkedList<QueryTerm>();
        int flag4=0;
        int termcount=0;
        StringTokenizer st=new StringTokenizer(query);
                             while(st.hasMoreTokens())
                             {
                                  //System.out.println(st.nextToken()+ " ");
                                  String termtemp=st.nextToken();
                                  termtemp=termtemp.toLowerCase();
                                  Stemmer s=new Stemmer();
                                  char buffer[]=termtemp.toCharArray();
                                  int bufferlen=buffer.length;
                                  s.add(buffer,bufferlen);
                                  s.stem();
                                  termtemp=s.toString();
                                  int i1=0;
                                  int j=0;
                                  char finalbuffer[]=termtemp.toCharArray();
                                  char tempbuffer[]=new char[100];
                                  while(i1<finalbuffer.length)
                                  {
                                      if(Character.isLetter(finalbuffer[i1]))
                                      {
                                          tempbuffer[j++]=finalbuffer[i1];
                                      }
                                      i1++;
                                  }
                                  tempbuffer[j]='\0';
                                  String l=new String(tempbuffer);
                                  termtemp=l;
                                  if(flag4==0)
                                  {
                                      QueryTerm q=new QueryTerm();
                                      q.termname=termtemp;
                                      q.TermFrequency++;
                                      qt.add(q);
                                      termcount++;
                                      flag4=1;
                                  }
                                  else
                                  {
                                      ListIterator<QueryTerm> qli = qt.listIterator();
                                      int flag5=0;
                                      while(qli.hasNext())
                                      {
                                          QueryTerm qlitemp=qli.next();
                                          if(qlitemp.termname.equals(termtemp))
                                          {
                                              flag5=1;
                                              qlitemp.TermFrequency++;
                                              break;
                                          }
                                      }
                                      if(flag5==0)
                                      {
                                          QueryTerm nqt=new QueryTerm();
                                          nqt.termname=termtemp;
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
        ListIterator<QueryTerm> qli2 = qt.listIterator();
        double matrix[]=new double[termcount];
        int i7=0;
        while(qli2.hasNext())
        {
            QueryTerm qqt=qli2.next();
            matrix[i7]=1+Math.log(qqt.TermFrequency);
            //System.out.println("termname:"+qqt.termname+"termfreq:"+qqt.TermFrequency+"wt="+matrix[i7]);
            i7++;
        }
        //System.out.println(termcount);
        //traverse all documents
        double matrix2[]=new double[termcount];
        i7=0;
        ListIterator<QueryTerm> qit=qt.listIterator();
        while(qit.hasNext())
        {
            String s=qit.next().termname;
            ListIterator<Dictionary> lid=words.listIterator();
            while(lid.hasNext())
            {
              Dictionary dt=lid.next();
              if(dt.term.equals(s))
              {
                  matrix2[i7]=Math.log((double)N/dt.documentFrequency);
                  //System.out.println(s+"docfreq="+matrix2[i7]);
                  i7++;
                  break;
              }
            }
        }
        double matrix3[]=new double[termcount];
        for(int i=0;i<termcount;i++)
        {
            matrix3[i]=matrix[i]*matrix2[i];
            System.out.println("matrix["+i+"]="+matrix3[i]);
        }
        //Normalization
        double norm=0.0;
        for(int i=0;i<termcount;i++)
        {
            norm=norm+(matrix3[i]*matrix3[i]);
        }
        norm=Math.sqrt(norm);
        for(int i=0;i<termcount;i++)
        {
            matrix3[i]=matrix3[i]/norm;
            //System.out.println("matrix3["+i+"]="+matrix3[i]);
        }
        //matrix3 contains normalized values for queries.
        double normdoc[]=new double[N]; //normdoc conatins the normalization values of each and every doc
        for(int i=0;i<N;i++)
        {
            normdoc[i]=0.0;
        }
        for(int i=0;i<N;i++)
        {int j=i+1;
         String name = F[i].getName();
         int pos = name.lastIndexOf(".");
         if (pos > 0) {
            name = name.substring(0, pos);
         }
         String currentdoc=name;
         ListIterator<Dictionary> ddt=words.listIterator();
         while(ddt.hasNext())
         {
             Dictionary dq=ddt.next();
             ListIterator<Posting> tft=dq.list.listIterator();
             while(tft.hasNext())
             {Posting p=tft.next();
                 if(p.documentName.equals(currentdoc))
                 {   double d=1+Math.log(p.termFrequency);
                     d=d*d;
                     normdoc[i]=normdoc[i]+d;
                     break;
                 }
             }
         }
        }
        for(int i=0;i<N;i++)
        {   
            normdoc[i]=Math.sqrt(normdoc[i]);
            //System.out.println("Norm doc:"+i+"->"+normdoc[i]);
        }
        //System.out.println("-------------------------------------------------------------------------");
        double score[]=new double[N]; //array for scoring each and every doc
        for(int qq=0;qq<N;qq++)
        {
            score[qq]=0.0;
        }
        /*
           termmatrix[]=tf-wt doc/norm doc[i]
           finalmatrix[i]=termmatrix[i]*matrix3[i]
           score[i]=score[i]+finalmatrix[i]
        */
        for(int i=0;i<N;i++)
        {int j=i+1;
         String name = F[i].getName();
         int pos = name.lastIndexOf(".");
         if (pos > 0) {
            name = name.substring(0, pos);
         }
         String curdoc=name;
         //System.out.println(curdoc);
         double termmatrix[]=new double[termcount];
         for(int k=0;k<termcount;k++)
         {
             termmatrix[k]=0.0;
         }
         double finalmatrix[]=new double[termcount];
         for(int k=0;k<termcount;k++)
         {
             finalmatrix[k]=0.0;
         }
         int termcounter=0;
         ListIterator<QueryTerm> qa=qt.listIterator();
         while(qa.hasNext())
         {
            QueryTerm q5=qa.next();
            String stx=q5.termname;
            ListIterator<Dictionary> li=words.listIterator();
            while(li.hasNext())
            {
                Dictionary lp=li.next();
                if(lp.term.equals(stx))
                {
                  ListIterator<Posting> ltt=lp.list.listIterator();
                  int found=0;
                  while(ltt.hasNext())
                  {
                      Posting docn=ltt.next();
                      if(docn.documentName.equals(curdoc))
                      { found=1;
                        termmatrix[termcounter]=docn.termFrequency/normdoc[i];
                        //System.out.println("termmatrix["+termcounter+"]:"+termmatrix[termcounter]);
                        //System.out.println("matrix3["+termcounter+"]:"+matrix3[termcounter]);
                        finalmatrix[termcounter]=termmatrix[termcounter]*matrix3[termcounter];
                        //System.out.println("finalmatrix["+termcounter+"]:"+finalmatrix[termcounter]);
                        score[i]=score[i]+finalmatrix[termcounter];
                        termcounter++;
                        break;
                      }
                      
                  }
                  if(found==0)
                  {
                      termcounter++;
                  }
                }
            }
               
         }
         
        }
        
        for(int i=0;i<N;i++)
        {   
            score[i]=score[i]/normdoc[i];
            System.out.println("normdoc["+i+"]"+"="+normdoc[i]);
            //System.out.println("F["+i+"]"+"="+F[i].getAbsolutePath());
            //res=res+"Score["+(i+1)+"]"+"="+score[i]+"\n";
        }
        /*
                Bubble sort the documents list based on their scores.
                and also manipulating the file array.
                
        */
        for(int i=0;i<N-1;i++)
        {
            for(int j=0;j<N-i-1;j++)
            {
               if(score[j]<score[j+1])
               {
                   double temp=score[j];
                   score[j]=score[j+1];
                   score[j+1]=temp;
                   File ftemp=F[j];
                   F[j]=F[j+1];
                   F[j+1]=ftemp;
               }
            }
        }
        for(int i=0;i<N;i++)
        {   
            //score[i]=score[i]/normdoc[i];
            System.out.println("Score["+i+"]"+"="+score[i]);
            System.out.println("F["+i+"]"+"="+F[i].getAbsolutePath());
            //res=res+"Score["+(i+1)+"]"+"="+score[i]+"\n";
        }
        /*
           Collecting all the result and putting the result into the Text area in the GUI
        */
        String res="";
        for(int i=0;i<N;i++)
        {   
            try{
            URL u=Paths.get(F[i].getAbsolutePath()).toRealPath().toUri().toURL();
            res=res+u+"\n";
            }
            catch(IOException e)
            {
              e.printStackTrace();
            }          
            //res=res+F[i].getAbsolutePath()+"\n";
        }
        tf2.setText("DONE !!!");
        ta1.setText(res);
        }
      }
      else
      {   dirset=true;
          dirPath=tf3.getText().toString();
      }
    }
    InformationRetreivalBeta(String str)
    {
        super(str);
    }
    public static void main(String[] args)
    {    /*
             Graphical user Interface has been created here:
         */
          InformationRetreivalBeta irb=new InformationRetreivalBeta("Beta Search");
	  irb.setSize(1000,700);
	  irb.setVisible(true);
	  irb.setLayout(null);
          irb.setBackground(Color.lightGray);
          l5=new Label("Directory");
          l5.setForeground(Color.BLUE);
          Font fon9=new Font(Font.SANS_SERIF,Font.ITALIC,15);
          l5.setFont(fon9);
          l5.setBounds(50,40,100,40);
          tf3=new TextField();
          tf3.setBounds(50,90,190,35);
          tf3.setFont(fon9);
          b2=new Button("set");
          b2.setBounds(250,95,100,40);
          l1=new Label("Beta Search");
          l1.setForeground(Color.BLUE);
	  Font fon1=new Font(Font.SANS_SERIF,Font.BOLD,30);
          l1.setFont(fon1);
          l1.setBounds(420,50,200,100);
          tf1=new TextField();
          Font fon2=new Font(Font.SANS_SERIF,Font.PLAIN,30);
          tf1.setFont(fon2);
          tf1.setBounds(100,200,800,50);
          tf2=new TextField("STATUS");
          tf2.setEditable(false);
          tf2.setBounds(665,100,300,50);
          Font fon5=new Font(Font.SANS_SERIF,Font.ITALIC,15);
          tf2.setFont(fon5);
          tf2.setForeground(Color.red);
          b1=new Button("search");
          b1.setForeground(Color.BLUE);
          Font fon3=new Font(Font.SANS_SERIF,Font.PLAIN,30);
          b1.setFont(fon3);
          b1.setBounds(430,300,125,50);
          ta1=new TextArea();
          ta1.setEditable(false);
          ta1.setBounds(100,400,800,250);
          Font fon4=new Font(Font.SANS_SERIF,Font.PLAIN,20);
          ta1.setFont(fon4);
          l2=new Label("Developed By Arjun Vijayvargiya and Bikkumala Karthik");
          Font fon6=new Font(Font.SANS_SERIF,Font.ITALIC,15);
          l2.setForeground(Color.BLUE);
          l2.setBounds(350,660,800,40);
          l2.setFont(fon6);
          l3=new Label("Query");
          l3.setForeground(Color.BLUE);
          Font fon7=new Font(Font.SANS_SERIF,Font.ITALIC,15);
          l3.setFont(fon7);
          l3.setBounds(100,160,200,40);
          l4=new Label("Result");
          l4.setForeground(Color.BLUE);
          Font fon8=new Font(Font.SANS_SERIF,Font.ITALIC,15);
          l4.setFont(fon8);
          l4.setBounds(100,360,200,40);
          irb.setResizable(false);
          irb.add(tf1);
          irb.add(l1);
          irb.add(b1);
          irb.add(ta1);
          irb.add(tf2);
          irb.add(l2);
          irb.add(l3);
          irb.add(l4);
          irb.add(l5);
          irb.add(tf3);
          irb.add(b2);
          b1.addActionListener(irb);
          b2.addActionListener(irb);
          irb.addWindowListener(new WindowAdapter()
	  {

           public void windowClosing(WindowEvent we)
           {
                 System.exit(0);
           }
	  });    
    }    
}
