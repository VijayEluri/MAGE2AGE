package uk.ac.ebi.esd.pride;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ebi.age.util.StringUtil;


public class ReadPrideData2
{

 static final String experimentId     = "Project Name";//"PRIDE Experiment Accession";
 static final String sampleId         = "Sample Name";//"DOI (Digital Object Identifier)";
 static final String sampleName       = "Sample Name";
 static final String pubMedId         = "PubMed ID (CiteXplore)";
 static final String description      = "Experiment Short Label";
 static final String expTitle         = "Experiment Title";
 static final String refLine          = "Reference Line";
 static final String contactName      = "Contact Name";
 static final String brendaID         = "BRENDA ID (Tissue)";
 static final String doi              = "DOI (Digital Object Identifier)";
 /**
  * @param args
  * @throws IOException
  */
 static Set<String>  sampleAttribures = new HashSet<String>();
 static Set<String>  contactAttribures = new HashSet<String>();
 static Set<String>  publicationAttribures = new HashSet<String>();

 static
 {
//  sampleAttribures.add("Sample Name");
  sampleAttribures.add("Sample Description Comment");
  sampleAttribures.add("Taxonomy Term (NEWT / NCBI Taxon)");
  sampleAttribures.add("Taxonomy ID (NEWT / NCBI Taxon)");
  sampleAttribures.add("Tissue Ontology Term (BRENDA)");
  sampleAttribures.add(brendaID);
  sampleAttribures.add("Cell Type Term (CL)");
  sampleAttribures.add("CL ID (Cell Type)");
  sampleAttribures.add("Gene Ontology Term (GO)");
  sampleAttribures.add("GO ID (Gene Ontology)");
  sampleAttribures.add("Human Disease Term (DOID)");
  sampleAttribures.add("DOID ID (Human Disease)");
  
  contactAttribures.add(contactName);
  contactAttribures.add("Institution");
  contactAttribures.add("Contact Details (Email)");
  
  publicationAttribures.add(pubMedId);
  publicationAttribures.add(doi);
  publicationAttribures.add(refLine);
 }

 static class Experiment
 {
  String id;
  
  Map<String,String> attributes = new LinkedHashMap<String,String>();
  Map<String,Sample> samples = new LinkedHashMap<String,Sample>();
  Map<String,Map<String,String>> publications = new LinkedHashMap<String,Map<String,String>>();
  Map<String,Map<String,String>> contacts = new LinkedHashMap<String,Map<String,String>>();
  
 }
 
 static class Sample
 {
  String id;
  String name;
  
  Map<String,String> attributes = new LinkedHashMap<String,String>();
 }
 
 static Map<String, Experiment> experiments = new HashMap<String, Experiment>();
 
 public static void main(String[] args) throws IOException
 {
  File wDir = new File( args[0] );
  
  File prideFile = new File( wDir, args[1] );
  
  BufferedReader rd = new BufferedReader(new FileReader(prideFile));

  String str;

  List<String> header = new ArrayList<String>(30);

  str = rd.readLine();
  StringUtil.splitExcelString(str, ",", header);
  
  List<String> parts = new ArrayList<String>(30);

  List<String> sampleAttributes = new ArrayList<String>();

  for(String h : header )
   if( sampleAttribures.contains(h) )
    sampleAttributes.add(h);

  Map<String,String> valMap = new HashMap<String, String>();
  
  while((str = rd.readLine()) != null)
  {
   valMap.clear();
   parts.clear();
   StringUtil.splitExcelString(str, ",", parts);

   for(int i = 0; i < header.size(); i++)
   {
    if(i >= parts.size())
     break;

    String hdr = header.get(i);

    String val = parts.get(i).trim();

    if(val.length() == 0)
     continue;

    valMap.put(hdr, val);
   }

   String projId = valMap.get(experimentId);
   String sampName = valMap.get(sampleId);

   if(projId == null || sampName == null)
    continue;

   String sampId = sampName+valMap.get(brendaID);
   
   Experiment eExp = experiments.get(projId);

   String prideID = valMap.get("PRIDE Experiment Accession");

   if(eExp == null)
   {
    experiments.put(projId, eExp = new Experiment());
    eExp.id = projId;
    
    eExp.attributes.put("Data Source", "Pride");
    
    if( prideID != null )
     eExp.attributes.put("Link","http://www.ebi.ac.uk/pride/directLink.do?experimentAccessionNumber="+prideID);
    
    eExp.attributes.put("Description", valMap.get(description));
    
    if( valMap.containsKey(expTitle) )
     eExp.attributes.put("{"+expTitle+"}", valMap.get(expTitle) );
     
   }

   Sample cSamp = eExp.samples.get(sampId);

   if(cSamp == null)
   {
    eExp.samples.put(sampId, cSamp = new Sample());
    cSamp.id = sampId;
    cSamp.name = sampName;

    cSamp.attributes.put("Name", sampName);
    
    for(String sa : sampleAttributes)
    {
     String av = valMap.get(sa);

     if(av != null)
     {
       cSamp.attributes.put("{"+sa+"}", av);
     }
    }
    
    if( prideID != null )
     cSamp.attributes.put("Pride ID", prideID);
   }
   String contId = valMap.get(contactName);

   if(contId != null)
   {
    Map<String, String> cont = eExp.contacts.get(contId);

    if(cont == null)
    {
     eExp.contacts.put(contId, cont = new HashMap<String, String>());

     for(String sa : contactAttribures)
     {
      String av = valMap.get(sa);

      if(av != null)
       cont.put("{"+sa+"}", av);
     }
    }
   }

   String pubId = valMap.get(refLine);

   if(pubId != null)
   {
    Map<String, String> pub = eExp.publications.get(pubId);

    if(pub == null)
    {
     eExp.publications.put(pubId, pub = new HashMap<String, String>());

     for(String sa : publicationAttribures)
     {
      String av = valMap.get(sa);

      if(av != null)
      {
       if( sa.equals(pubMedId))
        pub.put("PubMed ID", av);
       else if( sa.equals(doi))
        pub.put("DOI", av);
       else
        pub.put("{"+sa+"}", av);
      }
     }
    }
   }

   
 /*    
   Experiment cExp = new Experiment();

   for(int i = 0; i < header.size(); i++)
   {
    if(i >= parts.size())
     break;

    String hdr = header.get(i);

    String val = parts.get(i).trim();

    if(val.length() == 0)
     continue;

    if(experimentId.equals(hdr))
     cExp.id = val;
    else if(sampleId.equals(hdr))
     cSamp.id = val;
    else if(sampleName.equals(hdr))
     cSamp.name = val;
    else if(sampleAttribures.contains(hdr))
     cSamp.attributes.put(hdr, val);
    else if(pubMedId.equals(hdr))
     cExp.attributes.put("PubMed ID", val);
    else if(description.equals(hdr))
     cExp.attributes.put("Description", val);
    else
     cExp.attributes.put("{" + hdr + "}", val);
   }

   cExp.attributes.put("Data Source", "Pride");
   cExp.attributes.put("Link", "http://www.ebi.ac.uk/pride/directLink.do?experimentAccessionNumber=" + cExp.id);
  */  
  }

  Map<String, String> m = new LinkedHashMap<String, String>();
  
  Set<String> expAttr = new HashSet<String>();
  Set<String> smpAttr = new HashSet<String>();
  
  PrintStream out = System.out;
  
  out.println("Found "+experiments.size()+" experiments");
  
  
  for( Experiment e : experiments.values() )
  {
   if( e.samples.size() <=1 )
    continue;
   
   out.println("   Experiment: "+e.id+" (samples: "+e.samples.size()+")");
   
   for( Map.Entry<String, String> me: e.attributes.entrySet() )
   {
    out.println("     "+me.getKey()+" = "+me.getValue());
   
    expAttr.add(me.getKey());
   }
   
   if( e.contacts.size() > 0 )
   {
    out.println("   ++Contacts");
    int i=1;
    
    for( Map<String,String> cnt : e.contacts.values() )
    {
     for( Map.Entry<String,String> me : cnt.entrySet() )
      out.println("    "+i+". "+me.getKey()+": "+me.getValue());
     
     i++;
    }
    
   }
   
   if( e.contacts.size() > 0 )
   {
    out.println("   ++Publications");
    int i=1;
    
    for( Map<String,String> cnt : e.publications.values() )
    {
     for( Map.Entry<String,String> me : cnt.entrySet() )
      out.println("    "+i+". "+me.getKey()+": "+me.getValue());
     
     i++;
    }
    
   }

   
   if( e.samples.size() == 0 )
    continue;
   
   out.println("   ++Samples");
 
   for( Sample s : e.samples.values() )
   {
    out.println("       Sample: " + s.id + " (attrs: " + s.attributes.size() + ")");

    for(Map.Entry<String, String> me : s.attributes.entrySet())
    {
     out.println("         " + me.getKey() + " = " + me.getValue());
     smpAttr.add(me.getKey());
    }
   }
  }
  
  out.println("\nExpreriment attributes");
  
  for(String s : expAttr)
   out.println(s);

  out.println("\nSample attributes");
  for(String s : smpAttr)
   out.println(s);
  
  if( true )
   return;
  
  
  List<String> localSampAttr = new ArrayList<String>();
 
  int i=0;
  
  for( Experiment e : experiments.values() )
  {
   i++;
   
   out = new PrintStream(new File(wDir,i+".age.txt"));
   
   out.print("Group");
   
   for( String key: e.attributes.keySet() )
    out.print("\t"+key);
   
   out.println();
   
   out.print("GPR-"+e.id);
   
   for( String key: e.attributes.keySet() )
    out.print("\t"+e.attributes.get(key));
   
   out.println("\n");

   
   if( e.samples.size() == 0 )
    continue;
   
   localSampAttr.clear();
   
   for( String sa : sampleAttributes )
   {
    for( Sample s : e.samples.values() )
    {
     if( s.attributes.containsKey(sa) )
     {
      localSampAttr.add(sa);
      break;
     }
    }
   }
 
   out.print("Sample\tName");
   for( String lsa : localSampAttr )
    out.print("\t{"+lsa+"}");

   out.println("\tbelongsTo");
//   out.println("\tbelongsTo");
   
   for( Sample s : e.samples.values() )
   {
    out.print(s.id+"\t"+s.name);

    for( String lsa : localSampAttr )
    {
     String val = s.attributes.get(lsa);
     
     if( val == null )
      val="";
     
     out.print("\t"+val);
    }
    
    out.println("\tGPR-"+e.id);

   }
   
   out.close();
  }

 
 }

}