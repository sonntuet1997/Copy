import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class FileInfo {
    public String location;
    public String hash;

    public FileInfo(String location, String hash) {
        this.location = location;
        this.hash = hash;
    }
}


public class Main {
    HashMap<String, List<FileInfo>> Info = new HashMap<String, List<FileInfo>>();
    HashMap<String, HashMap<String, Boolean>> CheckDup = new HashMap<String, HashMap<String, Boolean>>();
    //    HashMap<String, Boolean> Blacklist = new HashMap<String, Boolean>();
    HashMap<String, String> Paths = new HashMap<String, String>();

    public static void main(String[] args) throws IOException {
        Main main = new Main();
//        main.process("E:\\Assignment", "Hoàn Đào Minh");
        main.process("E:\\Final", "");
        Set<String> listMssv = main.Info.keySet();
        for (String identity : listMssv) {
            List<FileInfo> fs = main.checkDup(identity);
            if(fs.size() == 0) continue;
            System.out.println("---INFORMATION IDENTITY: " + identity + " PATH: " + main.Paths.get(identity) + "---");
//            if (main.Blacklist.containsKey(identity)) {
            System.out.println("Files:");
            for (FileInfo f : main.Info.get(identity)) {
                System.out.println("FileLocation: " + f.location + " ------ HASH: " + f.hash);
            }
            System.out.println();
            System.out.println("Duplicate Files: ");
            for (FileInfo f : fs) {
                System.out.println("FileLocation: " + f.location + " ------ HASH: " + f.hash);
                System.out.println("Same file in: ");
                HashMap<String, Boolean> list = main.CheckDup.get(f.hash);
                for (String i : list.keySet()) {
                    if (!i.equals(identity)) System.out.print(i + ", ");
                }
                System.out.println();
            }
            System.out.println("Blacklist: " + (fs.size() > 0 ? "YES" : "NO"));
//            }
            if (fs.size() < 1) {
                File file = new File(main.Paths.get(identity));
                Desktop desktop = Desktop.getDesktop();
                desktop.open(file);
            }
            System.out.println("----------------------------------------");

        }
        System.out.println("PROCESS COMPLETED!");
        Scanner keyboard = new Scanner(System.in);
        while (true) {
            String identity = keyboard.nextLine();
            if (main.Paths.get(identity) == null) {
                System.out.println("CANNOT FIND THIS IDENTITY");
                System.out.println("----------------------------------------");
                continue;
            }
            System.out.println("---INFORMATION IDENTITY: " + identity + " PATH: " + main.Paths.get(identity) + "---");
//            if (main.Blacklist.containsKey(identity)) {
            System.out.println("Files:");
            for (FileInfo f : main.Info.get(identity)) {
                System.out.println("FileLocation: " + f.location + " ------ HASH: " + f.hash);
            }
            System.out.println();
            System.out.println("Duplicate Files: ");
            List<FileInfo> fs = main.checkDup(identity);
            for (FileInfo f : fs) {
                System.out.println("FileLocation: " + f.location + " ------ HASH: " + f.hash);
                System.out.println("Same file in: ");
                HashMap<String, Boolean> list = main.CheckDup.get(f.hash);
                for (String i : list.keySet()) {
                    if (!i.equals(identity)) System.out.print(i + ", ");
                }
                System.out.println();
            }
            System.out.println("Blacklist: " + (fs.size() > 0 ? "YES" : "NO"));
//            }
            if (fs.size() < 1) {
                File file = new File(main.Paths.get(identity));
                Desktop desktop = Desktop.getDesktop();
                desktop.open(file);
            }
            System.out.println("----------------------------------------");
        }
    }

    public void process(String path, String find) throws IOException {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                System.out.println("Skipping---------------");
            } else if (listOfFile.isDirectory()) {
                if (!listOfFile.getName().contains(find)) continue;
//                System.out.println("---------Directory " + listOfFile.getName());
                String identity = findIdentity(listOfFile.getAbsolutePath());
                if (identity == null) {
                    System.out.println("CANNOT FIND IDENTITY IN THIS FOLDER: " + listOfFile.getAbsolutePath());
                    continue;
                }
                Paths.put(identity, listOfFile.getAbsolutePath());
//                System.out.println("Student Identity: " + identity);
                List<FileInfo> fileInfos;
                if (!Info.containsKey(identity)) {
                    fileInfos = new ArrayList<FileInfo>();
                    Info.put(identity, fileInfos);
                } else fileInfos = Info.get(identity);
                processFile(listOfFile.getAbsolutePath(), fileInfos);
                checkDup(identity);
//                System.out.println("Checking....................");
//                for (FileInfo f: Info.get(identity)
//                     ) {
//                    System.out.println("File: " + f.location);
//                    System.out.println("Hash: " + f.hash);
//                }
            }
        }
    }

    List<FileInfo> checkDup(String identity) {
        List<FileInfo> result = new ArrayList<FileInfo>();
        for (FileInfo f : Info.get(identity)) {
            HashMap<String, Boolean> list = new HashMap<String, Boolean>();
            if (!CheckDup.containsKey(f.hash)) {
                CheckDup.put(f.hash, list);
                list.put(identity, true);
            } else {
                list = CheckDup.get(f.hash);
                list.put(identity, true);
                if (list.size() > 1) result.add(f);
            }
        }
        return result;
    }

    public void processFile(String path, List<FileInfo> fileInfos) throws IOException {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                if (listOfFile.getName().endsWith(".zip")) {
                    try {
                        processZipFile(listOfFile.getAbsolutePath(), fileInfos);

                    } catch (Exception e) {
                        System.out.println("ERROR " + listOfFile.getAbsolutePath());
                    }
                } else {
                    System.out.println("---------File " + listOfFile.getName());
                }
            }
        }
    }

    public String findIdentity(String fileName) {
        File folder = new File(fileName);
        File[] listOfFiles = folder.listFiles();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                Pattern p = Pattern.compile("\\d+");
                // create matcher for pattern p and given string
                Matcher m = p.matcher(listOfFile.getName());
                // if an occurrence if a pattern was found in a given string...
                while (m.find()) {
                    String r = m.group();
                    if (r.length() == 8) return r;
                }
            }
        }
        return null;
    }

    public void processZipFile(String zipFile, List<FileInfo> fileInfos) throws IOException {
//        BufferedOutputStream dest = null;
//        System.out.println("Extracting File: " + zipFile);
        ZipFile zis = new ZipFile(zipFile, Charset.forName("CP437"));
        ZipEntry entry;
        Enumeration<? extends ZipEntry> entries = zis.entries();
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (entry.isDirectory()) continue;
            if (entry.getName().contains("MACOSX")) {
//                System.out.println(entry.getName());
                continue;
            }
            if (entry.getName().contains("~$")) {
//                System.out.println(entry.getName());
                continue;
            }
            MessageDigest shaDigest = null;
            try {
                shaDigest = MessageDigest.getInstance("SHA-1");
            } catch (Exception ignored) {

            }
            fileInfos.add(new FileInfo(entry.getName(), getFileChecksum(shaDigest, zis.getInputStream(entry))));
        }
        zis.close();
    }

    private String getFileChecksum(MessageDigest digest, InputStream inputStream) throws IOException {
        //Get file input stream for reading the file content

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = inputStream.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        //close the stream; We don't need it now.
        inputStream.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }
}
