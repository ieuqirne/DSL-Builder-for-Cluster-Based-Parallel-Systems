package cluster.DataGenerator

import GPP_Builder.GPPlexFileHanding
import jcsp.lang.*


FileHanding fileHan = new FileHanding()

ArrayList<Integer> ListWorkers = new ArrayList<>()
ArrayList<Integer> ListEmitInputs = new ArrayList<>()
ArrayList<Integer> ListCollectInput = new ArrayList<>()
ArrayList<Integer> ListNodesInputs = new ArrayList<>()

String[] words = null
String st
String workersString = "workers"
//Reading the file
String filePath = new File("").getAbsoluteFile().getParentFile()
//String getParentFile = filePath.getParentFile().getName()
println "filePath " + filePath
//println "getParentFile " + getParentFile
String outPutfilePath = (filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\Files\\")
//System.out.println (filePath)
//BufferedReader reader = new BufferedReader(new FileReader(filePath + "\\smcpiScriptrc\\main\\groovy\\cluster\\gppScript\\mcpiScript.gpp"))

inFile =(filePath + "\\gppScript\\mcpiScript.gpp")
outFile =(filePath + "\\DataGenerator\\Files\\Everything.gpp")

println inFile
println outFile

fileHan.openFiles(inFile, outFile)
fileHan.parse(2)
/*
if ( error == ""){
    System.out.println("Build Successful: " + outFile);
}
else System.out.println("Build failed:" + error);*/