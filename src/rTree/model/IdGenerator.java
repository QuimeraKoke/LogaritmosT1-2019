package rTree.model;


import java.io.*;

public class IdGenerator {

    public static final File FILE = new File(RTree.DIR + "id");

    public static int nextId() {
        try {
            int result;
            if (FILE.exists()) {
                FileInputStream fis = new FileInputStream(FILE);
                ObjectInputStream in = new ObjectInputStream(fis);
//                BufferedReader br = new BufferedReader(new FileReader(FILE));
                result = in.read();
            } else {
                result = 0;
            }
            ObjectOutputStream out
                    = new ObjectOutputStream(new FileOutputStream(FILE));
            out.writeInt(result + 1);
            out.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return 0;
        }
    }

}