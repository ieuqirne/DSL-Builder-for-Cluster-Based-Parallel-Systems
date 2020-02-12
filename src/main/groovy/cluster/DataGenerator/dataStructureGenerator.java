package cluster.DataGenerator;

import GPP_Builder.GPPlexFileHanding;
import java.io.*;
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

        String emitDetailsSt = "emitDetails";
        String resultDetailsSt = "resultDetails";
        ArrayList<String> ListImports = new ArrayList<>();
        ArrayList<String> ListLibraries = new ArrayList<>();
        ArrayList<String> ListEmitDetails = new ArrayList<>();
        ArrayList<String> ListResultDetails = new ArrayList<>();
        ArrayList<String> ListEmit = new ArrayList<>();
        ArrayList<String> ListCol = new ArrayList<>();


        String[] words = null;
        String st;
        String workersString = "workers";
        String defString = "def";
        String importString = "import";

        String emitSt = "emit";
        String groupSt = "group";
        String collectorSt = "collector";
        String closingBracket = ")";
        String openingBracket = "(";


        int typeConection = 0;
        String requestType = "";
        String responseType = "";

        //Reading the file
        String filePath = new File("").getAbsolutePath();
        //System.out.println(filePath);
        String outPutfilePath = (filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\Files\\");
        //System.out.println (filePath);
        BufferedReader reader1 = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\gppScript\\mcpiScript.gpp"));
        //fileHan.parse();

        while ((st = reader1.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x++) {
                if(words[x].equals(defString) &&  words[x + 1].equals(groupSt)){
                    if(words[x + 4].contains("AnyGroupAny"))
                        typeConection = 1;
                }

            }
        }
        switch (typeConection){
            case 1:
                ListLibraries.addAll(Arrays.asList("import", "GPP_Library.cluster.connectors.OneNodeRequestedList",
                        "import", "GPP_Library.connectors.reducers.AnyFanOne"));
                requestType = "requestListONRL";
                responseType = "responseListONRL";
                break;

        }

        BufferedReader reader2 = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\gppScript\\mcpiScript.gpp"));

        while ((st = reader2.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x ++)
            {

                if(words[x].equals(importString)&& importBoolean == false)
                {
                    importBoolean = true;
                }
                if (words[x].equals(workersString))   //Search for the given word
                {
                    numberWorkers = Integer.parseInt(words[x+2]);
                    //ListWorkers.add( Integer.parseInt(words[x+2]));    //If Present increase the count by one
                }
                if (words[x].equals(defString))   //Search for the given word
                {
                    if(words[x+1].equals(emitDetailsSt))
                        emitDBoolean = true;
                    if(words[x+1].equals(resultDetailsSt))
                        resDBoolean = true;
                    if(words[x+1].equals(emitSt))
                        emBoolean = true;
                    if(words[x+1].equals(collectorSt))
                        colBoolean = true;
                }


                if(importBoolean)
                    ListImports.add(words[x]);
                if(emitDBoolean)
                    ListEmitDetails.add(words[x]);
                if(resDBoolean)
                    ListResultDetails.add(words[x]);
                if(emBoolean)
                    ListEmit.add(words[x]);
                if(colBoolean)
                    ListCol.add(words[x]);



                if(x == 0 && !words[x].equals(importString) && importBoolean)
                    importBoolean = false;
                if (words[x].equals(closingBracket) && emitDBoolean)
                    emitDBoolean = false;
                if (words[x].equals(closingBracket) && resDBoolean)
                    resDBoolean = false;
                if (words[x].equals(closingBracket) && emBoolean)
                    emBoolean = false;
            }
        }

        //Adding ListLibraries to ListImports. ListLibraries will be different depending of the type of
        //Group defined on the Scripts.
        ListImports.addAll(ListLibraries);

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
            if(model.equals(importString))
                System.out.println();
            System.out.print(model + " ");

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

        System.out.println(" ");

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
            System.out.print(model + " ");
            if(model.endsWith(","))
                System.out.println();
        }
        System.out.println("\n");
        for(String model : ListResultDetails) {
            System.out.print(model + " ");
            if(model.endsWith(","))
                System.out.println();
        }

        System.out.println("\n");
        for(String model : ListEmit) {
            if(model.equals(closingBracket))
                System.out.print(", \n output: chanl.out() \n");
            System.out.print(model + " ");
            if(model.equals(openingBracket))
                System.out.print("\n");
        }
        System.out.print("\ndef onrl = new OneNodeRequestedList(\n");
        System.out.print("request: "+ requestType +"(\n");
        System.out.print("request: "+ responseType +"(\n");
        System.out.print("input: chan1.in()\n)");

        System.out.print("\ndef afo = new AnyFanOne(\n");
        System.out.print("inputAny: inChan"+ numberWorkers+1 +",\n");
        System.out.print("output: chan2.out(),\n");
        System.out.print("sources: nodes\n)");

        for(String model : ListCol) {
            if(model.equals(closingBracket))
                System.out.print(", \n output: chanl.out() \n");
            System.out.print(model + " ");
            if(model.equals(openingBracket))
                System.out.print("\n");
        }

        //Creating File Imports.gpp
        try {
            FileWriter EmitStructureWriter = new FileWriter(outPutfilePath + "Imports.gpp");
            for(String model : ListImports) {
                if(model.equals(importString))
                    EmitStructureWriter.write(("\n"));
                EmitStructureWriter.write((model+ " "));
            }
            EmitStructureWriter.close();

            System.out.println("\n\nSuccessfully wrote to the file Imports.gpp");
        } catch (IOException e) {
            System.out.println("\nAn error occurred on file Imports.gpp");
            e.printStackTrace();
        }
        //Creating File NumberNodes.gpp
        try {
            FileWriter NumberWorkersStructureWriter = new FileWriter(outPutfilePath + "NumberNodes.gpp");

            NumberWorkersStructureWriter.write("int nodes = " + numberWorkers);
            NumberWorkersStructureWriter.close();

            System.out.println("\n\nSuccessfully wrote to the file NumberNodes.gpp.");
        } catch (IOException e) {
            System.out.println("\nAn error occurred on file NumberNodes.gpp.");
            e.printStackTrace();
        }
        //Creating File InputsChannelCreation.gpp
        try {
            FileWriter EmitStructureWriter = new FileWriter(outPutfilePath + "InputsChannelCreation.gpp");

            for(int x = 0; x <= numberWorkers; x++)
            {
                int temp = 100 + x;
                EmitStructureWriter.write("NetChannelInput inChan" + (x+1) + " = NetChannel.numberedNet2One(" + temp + ")\n");
            }
            EmitStructureWriter.close();

            System.out.println("\n\nSuccessfully wrote to the file InputsChannelCreation.gpp");
        } catch (IOException e) {
            System.out.println("\nAn error occurred on file InputsChannelCreation.gpp");
            e.printStackTrace();
        }

        //Creating File OutputsChannelCreation.gpp
        try {
            FileWriter EmitStructureWriter = new FileWriter(outPutfilePath + "OutputsChannelCreation.gpp");

            for(int x = 0; x < numberWorkers; x++) {
                EmitStructureWriter.write("def otherNode"+ (x+1) +"Address = new TCPIPNodeAddress(nodeIPs["+ x +"], 1000)\n");
                EmitStructureWriter.write("NetChannelOutput outChan"+ (x+1) +" = NetChannel.one2net(otherNode"+ (x+1) +"Address, 100)\n");
            }
            EmitStructureWriter.close();

            System.out.println("\n\nSuccessfully wrote to the file OutputsChannelCreation.gpp");
        } catch (IOException e) {
            System.out.println("\nAn error occurred on file OutputsChannelCreation.gpp");
            e.printStackTrace();
        }

        //Creating File ProcessDefinition.gpp
        try {
            FileWriter EmitStructureWriter = new FileWriter(outPutfilePath + "ProcessDefinition.gpp");

            for(int x = 0; x < 2; x++) {
                EmitStructureWriter.write("def chan"+ (x+1) +" = Channel.one2one()\n");
            }
            EmitStructureWriter.write("def "+ requestType +" = new ChannelInputList()\n");
            for(int x = 0; x < numberWorkers; x++) {
                EmitStructureWriter.write(requestType +".append(inChan" + (x + 1) + ")\n");
            }
            EmitStructureWriter.write("def "+ responseType +" = new ChannelOutputList()\n");
            for(int x = 0; x < numberWorkers; x++) {
                EmitStructureWriter.write(responseType + ".append(outChan"+ (x+1) +")\n");
            }

            EmitStructureWriter.close();

            System.out.println("\n\nSuccessfully wrote to the file ProcessDefinition.gpp");
        } catch (IOException e) {
            System.out.println("\nAn error occurred on file ProcessDefinition.gpp");
            e.printStackTrace();
        }
        //Creating File EmitInputsStructure
        /*try {
            FileWriter EmitStructureWriter = new FileWriter(outPutfilePath + "EmitInputsStructure.gpp");
            for (int x = 0; x < numberWorkers ;x ++){
                EmitStructureWriter.write((ListEmitInputs.get(x)+ " "));
            }
            EmitStructureWriter.close();
            System.out.println("Successfully wrote to the file EmitStructure.");
        } catch (IOException e) {
            System.out.println("\nAn error occurred on file EmitStructure.");
            e.printStackTrace();
        }

        //Creating File CollectInputsStructure
        try {
            FileWriter CollectInputsStructureWriter = new FileWriter(outPutfilePath + "CollectInputsStructure.gpp");

            CollectInputsStructureWriter.write( ListCollectInput.get(0)+ "");
            CollectInputsStructureWriter.close();

            System.out.println("Successfully wrote to the file CollectInputsStructure.");
        } catch (IOException e) {
            System.out.println("\nAn error occurred on file CollectInputsStructure.");
            e.printStackTrace();
        }

        //Creating File NodesInputsStructure
        try {
            FileWriter NodesStrutureWriter = new FileWriter(outPutfilePath + "NodesInputsStructure.gpp");
            for (int x = 0; x < numberWorkers ;x ++){
                NodesStrutureWriter.write((ListNodesInputs.get(x)+ " "));
            }
            NodesStrutureWriter.close();
            System.out.println("Successfully wrote to the file EmitStructure.");
        } catch (IOException e) {
            System.out.println("\nAn error occurred on file EmitStructure.");
            e.printStackTrace();
        }*/
    }
}
