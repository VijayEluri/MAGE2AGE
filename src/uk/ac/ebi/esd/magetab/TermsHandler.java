package uk.ac.ebi.esd.magetab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import moda2.SDRFparser;
import moda2.SDRFparser.Value;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;


public class TermsHandler
{
 static final String valueTSR="TSR";
 static final String unitType="UnitType";
 static final String unitValue="UnitValue";
 static final String unitTSR="UnitTSR";
 
 public static void main(String[] args) throws IOException
 {
  if( args.length != 2 )
  {
   System.err.println("Please, provide working directory and id file");
   System.exit(1);
  }
  
  File wDir = new File( args[0] );
  
  if( ! wDir.isDirectory() )
  {
   System.err.println("Working directory doesn't exist");
   System.exit(1);
  }
 
  File idFile = new File(wDir,args[1]);
  
  List<String> ids = new ArrayList<String>(1000);
  
  FileReader idRd = new FileReader(idFile);
  BufferedReader in = new BufferedReader(idRd);
  
  String str=null;
  
  while( (str = in.readLine()) != null )
  {
   str=str.trim();
   
   if( str.length() > 0)
    ids.add(str);
  }
  
  in.close();
  
  System.out.println("Loaded "+ids.size()+" IDs");
  
  HttpClient httpclient = new DefaultHttpClient();


  ResponseHandler<String> responseHandler = new BasicResponseHandler();

  Map<String,Collection<String>> hdrs = new HashMap<String,Collection<String>>();
  
  int i=1;
  
  for(String id : ids)
  {
   
   try
   {
    String url = "http://www.ebi.ac.uk/microarray-as/ae/files/"+id+"/"+id+".sdrf.txt";
    
    System.out.println(" Trying: "+url);
    
    HttpGet httpget = new HttpGet(url); 
    String responseBody = httpclient.execute(httpget, responseHandler);
    
    System.out.println(String.valueOf(i++)+" Got "+id+" size: "+responseBody.length());

    SDRFparser parser = new SDRFparser( new StringReader(responseBody) );
    
    
    for( HashMap<String,HashMap<String,HashSet<Value>>> srcMap : parser.getNodes().get("Source Name").values() )
    {
     for( Map.Entry<String,HashMap<String,HashSet<Value>>> me : srcMap.entrySet() )
     {
      for( String k : me.getValue().keySet() )
      {
       String name = null;
       
       if( k == null )
        name = me.getKey() ;
       else
        name = k;
       
       Collection<String> expLst = hdrs.get(name);
       
       if( expLst == null )
       {
        expLst = new ArrayList<String>();
        hdrs.put(name, expLst);
       }
       
       expLst.add(id);
      }
     }
    }
    
   }
   catch(Exception e)
   {
    System.out.println(id+ " failured: ("+e.getMessage()+")");
    e.printStackTrace();
   }
   
  }
  
  MGEDTerms mged = new MGEDTerms();
  
  HashSet<String> standard = new HashSet<String>();
  
  System.out.println("Headers: ");
  for(Map.Entry<String,Collection<String>> me : hdrs.entrySet() )
  {
   String h = me.getKey();
   
   if( mged.contains(h))
   {
    System.out.print("[MGED] ");
    standard.add(h);
   }
   
   System.out.println(h);
   
   System.out.print("   ");
   for( String exp : me.getValue() )
    System.out.print("{"+exp+"}");
   
   System.out.println("\n");
  }
  
  System.out.println("Standard: ");
  for(String h : standard)
  {
   System.out.println(h);
  }

  
  return;
 }

 static class Path
 {
  String first;
  String second;
  
  String qual;
 }
 
 static class Block
 {
  String name;
  Path path;
  
  List<Path> qualifiers = new ArrayList<Path>(10);
 }
}