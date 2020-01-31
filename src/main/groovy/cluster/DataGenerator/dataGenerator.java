package cluster.DataGenerator;

import java.io.*;
import java.util.ArrayList;

public class dataGenerator {
    public static void main(String[] args) throws Exception
    {

        if(args.length < 1) {
            System.out.println("Error, usage: java ClassName inputfile");
            System.exit(1);
        }

        ArrayList<Integer> ListWorkers = new ArrayList<>();
        ArrayList<Integer> ListEmitInputs = new ArrayList<>();
        ArrayList<Integer> ListCollectInput = new ArrayList<>();
        ArrayList<Integer> ListNodesInputs = new ArrayList<>();

        String[] words = null;
        String st;
        String workersString = "workers";
        //Reading the file
        String filePath = new File("").getAbsolutePath();
        //System.out.println (filePath);= new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\gppScript\\mcpiScript.gpp"));
        //BufferedReader reader = new BufferedReader(new FileReader(filePath + "\\src\\main\\groovy\\cluster\\gppScript\\mcpiScript.gpp"));
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));

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
        for (int x = 0; x < ListCollectInput.size() ;x ++){
            System.out.print(ListCollectInput.get(x) +" ");
        }
        System.out.print("\nList of Nodes Inputs: ");
        for (int x = 0; x < ListWorkers.get(0) ;x ++){
            ListNodesInputs.add(100);
            System.out.print(ListNodesInputs.get(x) +" ");
        }
    }
}
