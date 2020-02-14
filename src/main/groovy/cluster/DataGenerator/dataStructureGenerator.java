package cluster.DataGenerator;

import GPP_Builder.GPPlexFileHanding;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;


public class dataStructureGenerator {
    public static void main(String[] args) throws Exception
    {
        int numberWorkers = 0;

        //ArrayList<Integer> ListWorkers = new ArrayList<>();
        ArrayList<Integer> ListEmitInputs = new ArrayList<>();
        ArrayList<Integer> ListCollectInput = new ArrayList<>();
        ArrayList<Integer> ListNodesInputs = new ArrayList<>();

        boolean importBoolean = false;
        boolean emitDBoolean = false;
        boolean resDBoolean = false;
        boolean emBoolean = false;
        boolean colBoolean = false;
        boolean chaDefBoolean = false;

        ArrayList<String> ListImports = new ArrayList<>();
        ArrayList<String> ListLibraries = new ArrayList<>();
        ArrayList<String> ListEmitDetails = new ArrayList<>();
        ArrayList<String> ListResultDetails = new ArrayList<>();
        ArrayList<String> ListEmit = new ArrayList<>();
        ArrayList<String> ListCol = new ArrayList<>();
        ArrayList<String> ListBasicHost = new ArrayList<>();

        ArrayList<String> ListImportsFile = new ArrayList<>();
        ArrayList<String> ListNumbersNodeFile = new ArrayList<>();
        ArrayList<String> ListInputsFile = new ArrayList<>();
        ArrayList<String> ListOutputsFile = new ArrayList<>();
        ArrayList<String> ListProDefFile = new ArrayList<>();
        ArrayList<String> ListProManFile = new ArrayList<>();


        String[] words = null;
        String st;

        int typeConection = 0;
        String requestType = "";
        String responseType = "";
        int defCounter = 0;
        ArrayList<String> defStringNames = new ArrayList<>();

        //Reading the file
        String filePath = new File("").getAbsolutePath();
        //System.out.println(filePath);
        String tempOutPutfilePath = (filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\TemporaryFiles\\");
        String outPutfilePath = (filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\Files\\");
        BufferedReader reader1 = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\gppScript\\mcpiScript.gpp"));

        while ((st = reader1.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x++) {
                if(words[x].equals("def") &&  words[x + 1].equals("group")){
                    if(words[x + 4].contains("AnyGroupAny"))
                        typeConection = 1;
                }
                if(words[x].equals("def")){
                    defCounter++;
                    defStringNames.add(words[x + 1]);
                }
            }
        }
        switch (typeConection){
            case 1:
                ListLibraries.addAll(Arrays.asList("\n","import"," ", "GPP_Library.cluster.connectors.OneNodeRequestedList","\n",
                        "import"," ", "GPP_Library.connectors.reducers.AnyFanOne"));
                requestType = "requestListONRL";
                responseType = "responseListONRL";
                break;

        }

        BufferedReader reader2 = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\gppScript\\mcpiScript.gpp"));

        while ((st = reader2.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x ++)
            {

                if(words[x].equals("import")&& importBoolean == false)
                {
                    importBoolean = true;
                }
                if (words[x].equals("workers"))   //Search for the given word
                {
                    numberWorkers = Integer.parseInt(words[x+2]);
                    //ListWorkers.add( Integer.parseInt(words[x+2]));    //If Present increase the count by one
                }
                if (words[x].equals("def"))   //Search for the given word
                {
                    if(words[x+1].equals("emitDetails"))
                        emitDBoolean = true;
                    if(words[x+1].equals("resultDetails"))
                        resDBoolean = true;
                    if(words[x+1].equals("emit"))
                        emBoolean = true;
                    if(words[x+1].equals("collector"))
                        colBoolean = true;
                }


                if(importBoolean) {
                    ListImports.add(words[x]);
                    ListImports.add(" ");
                }
                if(emitDBoolean){
                    ListEmitDetails.add(words[x]);
                    ListEmitDetails.add(" ");
                }
                if(resDBoolean){
                    ListResultDetails.add(words[x]);
                    ListResultDetails.add(" ");
                }
                if(emBoolean){
                    ListEmit.add(words[x]);
                    ListEmit.add(" ");
                }
                if(colBoolean){
                    ListCol.add(words[x]);
                    ListCol.add(" ");
                }


                if(x == 0 && !words[x].equals("import") && importBoolean)
                    importBoolean = false;
                if (words[x].equals(")") && emitDBoolean)
                    emitDBoolean = false;
                if (words[x].equals(")") && resDBoolean)
                    resDBoolean = false;
                if (words[x].equals(")") && emBoolean)
                    emBoolean = false;
                if (words[x].equals(")") && colBoolean)
                    colBoolean = false;
            }

            if(importBoolean)
                ListImports.add("\n");
            if(emitDBoolean)
                ListEmitDetails.add("\n");
            if(resDBoolean)
                ListResultDetails.add("\n");
            if(emBoolean)
                ListEmit.add("\n");
            if(colBoolean)
                ListCol.add("\n");

        }

        //Adding ListLibraries to ListImports. ListLibraries will be different depending of the type of
        //Group defined on the Scripts.
        for (String model : ListLibraries){
            ListImports.add(model);
        }
        /*
        System.out.print("\nList of Nodes Workers: ");
        //for (int workers : ListWorkers){
            System.out.print(numberWorkers);
        //}
        System.out.print("\nList of Emit Inputs: ");
        for (int x = 0; x < numberWorkers ;x ++){
            ListEmitInputs.add(100+x);
            System.out.print(ListEmitInputs.get(x) +" ");
        }
        System.out.print("\nList of Collects Inputs: ");
        ListCollectInput.add(100+ numberWorkers);
        for (int x = 0; x < ListCollectInput.size() ; x++){
            System.out.print(ListCollectInput.get(x) +" ");
        }
        System.out.print("\nList of Nodes Inputs: ");
        for (int x = 0; x < numberWorkers ; x++){
            ListNodesInputs.add(100);
            System.out.print(ListNodesInputs.get(x) +" ");
        }

        System.out.println("\n");

        for(String model : ListImports) {
            System.out.print(model);
        }

        System.out.println("\n");
        for(int x = 0; x <= numberWorkers; x++)
        {
            int temp = 100 + x;
            System.out.println("NetChannelInput inChan" + (x+1) + " = NetChannel.numberedNet2One(" + temp + ")");
        }

        System.out.println(" ");
        for(int x = 0; x < numberWorkers; x++) {
            System.out.println("def otherNode"+ (x+1) +"Address = new TCPIPNodeAddress(nodeIPs["+ x +"], 1000)");
            System.out.println("NetChannelOutput outChan"+ (x+1) +" = NetChannel.one2net(otherNode"+ (x+1) +"Address, 100)");
        }

        for(int x = 0; x < 2; x++) {
            System.out.println("def chan"+ (x+1) +" = Channel.one2one()");
        }
        System.out.println("def "+ requestType +" = new ChannelInputList()");
        for(int x = 0; x < numberWorkers; x++) {
            System.out.println(requestType +".append(inChan" + (x + 1) + ")");
        }
        System.out.println("def "+ responseType +" = new ChannelOutputList()");
        for(int x = 0; x < numberWorkers; x++) {
            System.out.println(responseType + ".append(outChan"+ (x+1) +")");
        }
        System.out.println("\n");
        //System.out.println(ListEmitDetails);
        for(String model : ListEmitDetails) {
            System.out.print(model);
        }
        System.out.println("\n");
        for(String model : ListResultDetails) {
            System.out.print(model);
        }

        System.out.println("\n");
        for(String model : ListEmit) {
            System.out.print(model);
            if(model.equals("emitDetails"))
                System.out.print(", \n output: chanl.out()");
        }

        System.out.print("\ndef onrl = new OneNodeRequestedList(\n");
        System.out.print(" request: "+ requestType +"(\n");
        System.out.print(" request: "+ responseType +"(\n");
        System.out.print(" input: chan1.in()\n)");

        System.out.print("\ndef afo = new AnyFanOne(\n");
        System.out.print(" inputAny: inChan"+ numberWorkers+1 +",\n");
        System.out.print(" output: chan2.out(),\n");
        System.out.print(" sources: nodes\n)");

        System.out.println();
        for(String model : ListCol) {
            System.out.print(model);
            if(model.equals("resultDetails"))
                System.out.print(", \n anoutput: chl.out()");
        }

        //ProcessManager PrintOuts
        System.out.println("\n");
        System.out.println("new PAR(["+ defStringNames.get(defStringNames.size()-3) +", onrl, afo, "+ defStringNames.get(defStringNames.size()-1) +"]).run()");
        */
        dataStructureGenerator.createFileImports(tempOutPutfilePath,numberWorkers,ListImports);

        dataStructureGenerator.createFileNumberNodes(tempOutPutfilePath, numberWorkers);

        dataStructureGenerator.createFileInputs(tempOutPutfilePath, numberWorkers);

        dataStructureGenerator.createFileOutputs(tempOutPutfilePath,numberWorkers);

        dataStructureGenerator.createFileProcessDefinition(tempOutPutfilePath,ListEmitDetails,
                 ListResultDetails, ListEmit,ListCol,numberWorkers, requestType, responseType);

        dataStructureGenerator.createFileProcessManager(tempOutPutfilePath,defStringNames);



        BufferedReader readerBasicHost = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\boilerPlate\\BasicHost.groovy"));
        while ((st = readerBasicHost.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
             for (int x = 0; x < words.length; x++){
                ListBasicHost.add(words[x]);
                ListBasicHost.add(" ");
            }
            ListBasicHost.add("\n");
        }

        BufferedReader readerImports = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\TemporaryFiles\\Imports.gpp"));
        while ((st = readerImports.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x++){
                ListImportsFile.add(words[x]);
                ListImportsFile.add(" ");
            }
            ListImportsFile.add("\n");
        }

        BufferedReader readerNumbNodes = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\TemporaryFiles\\NumberNodes.gpp"));
        while ((st = readerNumbNodes.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x++){
                ListNumbersNodeFile.add(words[x]);
                ListNumbersNodeFile.add(" ");
            }
            ListNumbersNodeFile.add("\n");
        }

        BufferedReader readerInputs = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\TemporaryFiles\\InputsChannelCreation.gpp"));
        while ((st = readerInputs.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x++){
                ListInputsFile.add(words[x]);
                ListInputsFile.add(" ");
            }
            ListInputsFile.add("\n");
        }

        BufferedReader readerOutputs = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\TemporaryFiles\\OutputsChannelCreation.gpp"));
        while ((st = readerOutputs.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x++){
                ListOutputsFile.add(words[x]);
                ListOutputsFile.add(" ");
            }
            ListOutputsFile.add("\n");
        }

        BufferedReader readerProDef = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\TemporaryFiles\\ProcessDefinition.gpp"));
        while ((st = readerProDef.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x++){
                ListProDefFile.add(words[x]);
                ListProDefFile.add(" ");
            }
            ListProDefFile.add("\n");
        }

        BufferedReader readerProMan = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\TemporaryFiles\\ProcessManager.gpp"));
        while ((st = readerProMan.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x++){
                ListProManFile.add(words[x]);
                ListProManFile.add(" ");
            }
            ListProManFile.add("\n");
        }
        for (int x = 0; x < ListBasicHost.size(); x++){
            System.out.print(ListBasicHost.get(x));
            if(ListBasicHost.get(x).equals("@Imports"))
            {
                System.out.print("\n");
                for(String model : ListImportsFile){
                    System.out.print(model);
                }
            }
            if(ListBasicHost.get(x).equals("@NumberNodes")){
                System.out.print("\n");
                for(String model : ListNumbersNodeFile){
                    System.out.print(model);
                }
            }
            if(ListBasicHost.get(x).equals("@InputsChannelCreations")){
                System.out.print("\n");
                for(String model : ListInputsFile){
                    System.out.print(model);
                }
            }
            if(ListBasicHost.get(x).equals("@OutputsChannelCreation"))
            {
                System.out.print("\n");
                for(String model : ListOutputsFile){
                    System.out.print(model);
                }
            }
            if(ListBasicHost.get(x).equals("@ProcessDefinition")){
                System.out.print("\n");
                for(String model : ListProDefFile){
                    System.out.print(model);
                }
            }
            if(ListBasicHost.get(x).equals("@ProcessManager")){
                System.out.print("\n");
                for(String model : ListProManFile){
                    System.out.print(model);
                }
            }
        }
        //Creating File HostNode.gpp
        try {
            FileWriter HostNodeFileWriter = new FileWriter(outPutfilePath + "HostNode.groovy");
            for (int x = 0; x < ListBasicHost.size(); x++){
                HostNodeFileWriter.write(ListBasicHost.get(x));
                if(ListBasicHost.get(x).equals("@Imports"))
                {
                    HostNodeFileWriter.write("\n");
                    for(String model : ListImportsFile){
                        HostNodeFileWriter.write(model);
                    }
                }
                if(ListBasicHost.get(x).equals("@NumberNodes")){
                    HostNodeFileWriter.write("\n");
                    for(String model : ListNumbersNodeFile){
                        HostNodeFileWriter.write(model);
                    }
                }
                if(ListBasicHost.get(x).equals("@InputsChannelCreations")){
                    HostNodeFileWriter.write("\n");
                    for(String model : ListInputsFile){
                        HostNodeFileWriter.write(model);
                    }
                }
                if(ListBasicHost.get(x).equals("@OutputsChannelCreation"))
                {
                    HostNodeFileWriter.write("\n");
                    for(String model : ListOutputsFile){
                        HostNodeFileWriter.write(model);
                    }
                }
                if(ListBasicHost.get(x).equals("@ProcessDefinition")){
                    HostNodeFileWriter.write("\n");
                    for(String model : ListProDefFile){
                        HostNodeFileWriter.write(model);
                    }
                }
                if(ListBasicHost.get(x).equals("@ProcessManager")){
                    HostNodeFileWriter.write("\n");
                    for(String model : ListProManFile){
                        HostNodeFileWriter.write(model);
                    }
                }
            }
            HostNodeFileWriter.close();

            System.out.println("Successfully wrote to the file HostNode.gpp");
        } catch (IOException e) {
            System.out.println("An error occurred on file HostNode.gpp");
            e.printStackTrace();
        }



    }

    static void createFileImports(String tempOutPutfilePath, int numberWorkers, ArrayList<String> ListImports){
        //Creating File Imports.gpp
        try {
            FileWriter ImportFileWriter = new FileWriter(tempOutPutfilePath + "Imports.gpp");
            for(String model : ListImports) {
                ImportFileWriter.write((model));
            }
            ImportFileWriter.close();

            System.out.println("Successfully wrote to the file Imports.gpp");
        } catch (IOException e) {
            System.out.println("An error occurred on file Imports.gpp");
            e.printStackTrace();
        }
    }
    //Creating File NumberNodes.gpp
    static void createFileNumberNodes(String tempOutPutfilePath, int numberWorkers){

        try {
            FileWriter NumberNodesFileWriter = new FileWriter(tempOutPutfilePath + "NumberNodes.gpp");

            NumberNodesFileWriter.write("int nodes = " + numberWorkers);

            NumberNodesFileWriter.close();
            System.out.println("Successfully wrote to the file NumberNodes.gpp");
        } catch (IOException e) {
            System.out.println("An error occurred on file NumberNodes.gpp");
            e.printStackTrace();
        }
    }

    //Creating File InputsChannelCreation.gpp
    static void createFileInputs(String tempOutPutfilePath, int numberWorkers){

        try {
            FileWriter InputsFileWriter = new FileWriter(tempOutPutfilePath + "InputsChannelCreation.gpp");

            for(int x = 0; x <= numberWorkers; x++)
            {
                int temp = 100 + x;
                InputsFileWriter.write("NetChannelInput inChan" + (x+1) + " = NetChannel.numberedNet2One(" + temp + ")\n");
            }
            InputsFileWriter.close();

            System.out.println("Successfully wrote to the file InputsChannelCreation.gpp");
        } catch (IOException e) {
            System.out.println("An error occurred on file InputsChannelCreation.gpp");
            e.printStackTrace();
        }

    }
    //Creating File OutputsChannelCreation.gpp
    static void createFileOutputs(String tempOutPutfilePath, int numberWorkers){
        try {
            FileWriter OutputsFileWriter = new FileWriter(tempOutPutfilePath + "OutputsChannelCreation.gpp");

            for(int x = 0; x < numberWorkers; x++) {
                OutputsFileWriter.write("def otherNode"+ (x+1) +"Address = new TCPIPNodeAddress(nodeIPs["+ x +"], 1000)\n");
                OutputsFileWriter.write("NetChannelOutput outChan"+ (x+1) +" = NetChannel.one2net(otherNode"+ (x+1) +"Address, 100)\n");
            }
            OutputsFileWriter.close();

            System.out.println("Successfully wrote to the file OutputsChannelCreation.gpp");
        } catch (IOException e) {
            System.out.println("An error occurred on file OutputsChannelCreation.gpp");
            e.printStackTrace();
        }

    }
    //Creating File ProcessDefinition.gpp
    static void createFileProcessDefinition(String tempOutPutfilePath, ArrayList<String> ListEmitDetails,
                                            ArrayList<String> ListResultDetails, ArrayList<String> ListEmit,
                                            ArrayList<String> ListCol, int numberWorkers,String requestType,
                                            String responseType){

        try {
            FileWriter ProDefFileWriter = new FileWriter(tempOutPutfilePath + "ProcessDefinition.gpp");

            for(int x = 0; x < 2; x++) {
                ProDefFileWriter.write("def chan"+ (x+1) +" = Channel.one2one()\n");
            }
            ProDefFileWriter.write("def "+ requestType +" = new ChannelInputList()\n");
            for(int x = 0; x < numberWorkers; x++) {
                ProDefFileWriter.write(requestType +".append(inChan" + (x + 1) + ")\n");
            }
            ProDefFileWriter.write("def "+ responseType +" = new ChannelOutputList()\n");
            for(int x = 0; x < numberWorkers; x++) {
                ProDefFileWriter.write(responseType + ".append(outChan"+ (x+1) +")\n");
            }

            ProDefFileWriter.write("\n");
            //System.out.println(ListEmitDetails);
            for(String model : ListEmitDetails) {
                ProDefFileWriter.write(model);
            }
            ProDefFileWriter.write("\n");
            for(String model : ListResultDetails) {
                ProDefFileWriter.write(model );
            }

            ProDefFileWriter.write("\n");
            for(String model : ListEmit) {
                ProDefFileWriter.write(model);
                if(model.equals("emitDetails"))
                    ProDefFileWriter.write(", \n output: chanl.out()");

            }
            ProDefFileWriter.write("\ndef onrl = new OneNodeRequestedList(\n");
            ProDefFileWriter.write(" request: "+ requestType +",\n");
            ProDefFileWriter.write(" response: "+ responseType +",\n");
            ProDefFileWriter.write(" input: chan1.in()\n)");

            ProDefFileWriter.write("\ndef afo = new AnyFanOne(\n");
            ProDefFileWriter.write(" inputAny: inChan"+ numberWorkers+1 +",\n");
            ProDefFileWriter.write(" output: chan2.out(),\n");
            ProDefFileWriter.write(" sources: nodes\n)");

            ProDefFileWriter.write("\n");
            for(String model : ListCol) {
                ProDefFileWriter.write(model);
                if(model.equals("resultDetails"))
                    ProDefFileWriter.write(", \n output: chanl.out()");
            }
            ProDefFileWriter.close();

            System.out.println("Successfully wrote to the file ProcessDefinition.gpp");
        } catch (IOException e) {
            System.out.println("An error occurred on file ProcessDefinition.gpp");
            e.printStackTrace();
        }

    }


    //Creating File ProcessManager.gpp
    static void createFileProcessManager(String tempOutPutfilePath,ArrayList<String> defStringNames){
        try {
            FileWriter ProcessManagerFileWriter = new FileWriter(tempOutPutfilePath + "ProcessManager.gpp");

            ProcessManagerFileWriter.write("new PAR(["+ defStringNames.get(defStringNames.size()-3) +", onrl, afo, "+ defStringNames.get(defStringNames.size()-1) +"]).run()");

            ProcessManagerFileWriter.close();

            System.out.println("Successfully wrote to the file ProcessManager.gpp");
        } catch (IOException e) {
            System.out.println("An error occurred on file ProcessManager.gpp");
            e.printStackTrace();
        }

    }
}
