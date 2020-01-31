package cluster.DataGenerator;

import GPP_Builder.GPPlexFileHanding;
import java.io.*;
import java.util.ArrayList;


public class dataStructureGenerator {
    public static void main(String[] args) throws Exception
    {
        GPPlexFileHanding fileHan = new GPPlexFileHanding();

        ArrayList<Integer> ListWorkers = new ArrayList<>();
        ArrayList<Integer> ListEmitInputs = new ArrayList<>();
        ArrayList<Integer> ListCollectInput = new ArrayList<>();
        ArrayList<Integer> ListNodesInputs = new ArrayList<>();

        String[] words = null;
        String st;
        String workersString = "workers";
        //Reading the file
        String filePath = new File("").getAbsolutePath();
        //System.out.println(filePath);
        String outPutfilePath = (filePath + "\\src\\main\\groovy\\cluster\\DataGenerator\\Files\\");
        //System.out.println (filePath);
        BufferedReader reader = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\gppScript\\mcpiScript.gpp"));
        //fileHan.parse();



        while ((st = reader.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x ++)
            {
                if (words[x].equals(workersString))   //Search for the given word
                {
                    ListWorkers.add( Integer.parseInt(words[x+2]));    //If Present increase the count by one
                }
            }
        }
        System.out.print("List of Nodes Workers: ");
        for (int workers : ListWorkers){
            System.out.print(workers);
        }
        System.out.print("\nList of Emit Inputs: ");
        for (int x = 0; x < ListWorkers.get(0) ;x ++){
            ListEmitInputs.add(100+x);
            System.out.print(ListEmitInputs.get(x) +" ");
        }
        System.out.print("\nList of Collects Inputs: ");
        ListCollectInput.add(100+ListWorkers.get(0));
        for (int x = 0; x < ListCollectInput.size() ; x++){
            System.out.print(ListCollectInput.get(x) +" ");
        }
        System.out.print("\nList of Nodes Inputs: ");
        for (int x = 0; x < ListWorkers.get(0) ; x++){
            ListNodesInputs.add(100);
            System.out.print(ListNodesInputs.get(x) +" ");
        }


        //Creating File NumberWorkersStructure
        try {
            FileWriter NumberWorkersStructureWriter = new FileWriter(outPutfilePath + "NumberWorkersStructure.gpp");

            NumberWorkersStructureWriter.write(ListEmitInputs.get(0) + "");
            NumberWorkersStructureWriter.close();

            System.out.println("\n\nSuccessfully wrote to the file NumberWorkersStructure.");
        } catch (IOException e) {
            System.out.println("\nAn error occurred on file EmitStructure.");
            e.printStackTrace();
        }

        //Creating File EmitInputsStructure
        try {
            FileWriter EmitStructureWriter = new FileWriter(outPutfilePath + "EmitInputsStructure.gpp");
            for (int x = 0; x < ListWorkers.get(0) ;x ++){
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
            for (int x = 0; x < ListWorkers.get(0) ;x ++){
                NodesStrutureWriter.write((ListNodesInputs.get(x)+ " "));
            }
            NodesStrutureWriter.close();
            System.out.println("Successfully wrote to the file EmitStructure.");
        } catch (IOException e) {
            System.out.println("\nAn error occurred on file EmitStructure.");
            e.printStackTrace();
        }

        System.out.println(" ");
        for(int x = 0; x <= ListWorkers.get(0); x++)
        {
            int temp = 100 + x;
            System.out.println("NetChannelInput inChan" + (x+1) + " = NetChannel.numberedNet2One(" + temp + ")");
        }

        System.out.println(" ");
        for(int x = 0; x < ListWorkers.get(0); x++) {
            System.out.println("def otherNode"+ (x+1) +"Address = new TCPIPNodeAddress(nodeIPs["+ x +"], 1000)");
            System.out.println("NetChannelOutput outChan"+ (x+1) +" = NetChannel.one2net(otherNode"+ (x+1) +"Address, 100)");
        }

        System.out.println(" ");

        for(int x = 0; x < 2; x++) {
            System.out.println("def chan"+ (x+1) +" = Channel.one2one()");
        }
        System.out.println("def requestListONRL = new ChannelInputList()");
        for(int x = 0; x < ListWorkers.get(0); x++) {
            System.out.println("requestListONRL.append(inChan" + (x + 1) + ")");
        }
        System.out.println("def responseListONRL = new ChannelOutputList()");
        for(int x = 0; x < ListWorkers.get(0); x++) {
            System.out.println("responseListONRL.append(outChan"+ (x+1) +")");
        }
        String def = "def";
        while ((st = reader.readLine()) != null) {
            words = st.split("\\s+");  //Split the word using space
            for (int x = 0; x < words.length; x ++)
            {
                System.out.println(words[x]);
                if (words[x].equals(def))   //Search for the given word
                {
                    System.out.println("Hey!");
                    ListWorkers.add( Integer.parseInt(words[x+2]));    //If Present increase the count by one
                }
            }
        }
    }
}
