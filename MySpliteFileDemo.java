import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by mxk94 on 2017/4/8.
 */
class MySpliteFileDemo {
    private static final String exted = ".cfg";
    private static final String ext_p = ".part";

    public static void main(String[] args) throws Exception {
        System.out.println("Please Enter File Path:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        File file = new File(br.readLine());
        System.out.println("1.Splite File");
        System.out.println("2.Merger File");
        switch (Integer.parseInt(br.readLine())) {
            case 1:
                if (!(file.exists() && file.isFile()))
                    throw new RuntimeException("File not exist or it not a file");
                System.out.println("Please Enter Splite-Size(MB) (1~1000):");
                int size = Integer.parseInt(br.readLine());
                splite(file,size);
                break;
            case 2:
                if (!(file.exists() && file.isDirectory()))
                    throw new RuntimeException("File not exist or it not a Directory");
                merger(file);
                break;
        }
    }

    static void splite(File file,int size) throws IOException {
        if (!file.exists()) {
            throw new IOException("File not found!");
        }
        File splite_dir = new File(file.getParentFile(), file.getName()+".partfile");
        if (!splite_dir.exists())
            splite_dir.mkdir();
        System.out.println("Please Wait...");
        byte buf[] = new byte[size*1024*1024];
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = null;
        File f = null;
        int count = 0;
        int len;
        while ((len = fis.read(buf)) != -1) {
            f = new File(splite_dir, (count++) + ext_p);
            fos = new FileOutputStream(f);
            fos.write(buf, 0, len);
            fos.close();
        }
        Properties prop = new Properties();
        prop.setProperty("filename", file.getName());
        prop.setProperty("part_count", count + "");
        File prop_file = new File(splite_dir.getAbsoluteFile(),file.getName() + ".cfg");
        fos = new FileOutputStream(prop_file);
        prop.store(fos, file.getName());
        fos.close();
        fis.close();
        System.out.println("Storage path:" + splite_dir.getAbsoluteFile());
        System.out.println("splite success!");
    }

    static void merger(File dir) throws Exception {
        if (!(dir.exists() && dir.isDirectory()))
            throw new RuntimeException("Dir not exists or dir not a Directory");
        File[] files = dir.listFiles(new FilteExted(exted));
        if (files.length == 0)
            throw new RuntimeException(exted + "File not found!");
        Properties prop = new Properties();
        FileInputStream fis = new FileInputStream(files[0]);
        prop.load(fis);
        String filename = prop.getProperty("filename");
        int filecount = Integer.parseInt(prop.getProperty("part_count"));
        files = dir.listFiles(new FilteExted(ext_p));
        if (files.length != filecount)
            throw new RuntimeException("part file is missing");
        System.out.println("Please Wait...");
        ArrayList<FileInputStream> filelist = new ArrayList<FileInputStream>();
        for (int i = 0; i < filecount; i++) {
            filelist.add(new FileInputStream(new File(dir, i + ext_p)));
        }
        Enumeration<FileInputStream> em = Collections.enumeration(filelist);
        SequenceInputStream sis = new SequenceInputStream(em);
        File targ_file = new File(dir, filename);
        FileOutputStream fos = new FileOutputStream(targ_file);
        byte buf[] = new byte[1024];
        int len = 0;
        while ((len = sis.read(buf)) != -1) {
            fos.write(buf);
        }
        fos.close();
        sis.close();
        System.out.println("merger succes!");
    }
}

class FilteExted implements FileFilter {
    String ext;

    public FilteExted(String ext) {
        this.ext = ext;
    }

    @Override
    public boolean accept(File dir) {
        return dir.getName().endsWith(ext);
    }
}
