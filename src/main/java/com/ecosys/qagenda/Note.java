package com.ecosys.qagenda;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Note {

    private String timestamp_creation;
    private String timestamp_link_event;
    private String title;

    public String readContent(){
        return "";
    }

    public String getTimestamp_creation() {
        return timestamp_creation;
    }

    public void setTimestamp_creation(String timestamp_creation) {
        this.timestamp_creation = timestamp_creation;
    }

    public String getTimestamp_link_event() {
        return timestamp_link_event;
    }

    public void setTimestamp_link_event(String timestamp_link_event) {
        this.timestamp_link_event = timestamp_link_event;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void getTitle(String title) {
        this.title = title;
    }

    public void setFromAnnuaireEntry(String entry){
        String[] parts = entry.split(";");

        setTimestamp_creation(parts[0]);
        if(parts.length > 1){
            setTitle(parts[1]);

            if (parts.length > 2){
                setTimestamp_link_event(parts[2]);
            }
        }
    }

    public String readNote() throws IOException {
        FileInputStream fis = new FileInputStream(new File(
                "notes"+"/"+getTimestamp_creation()+".txt"
        ));

        return new String(fis.readAllBytes(), StandardCharsets.UTF_8);
    }

    public void saveNote(byte[] content) throws IOException{

        File noteFile = new File("notes"+"/"+getTimestamp_creation()+".txt");

        // check if file is already here ( indicate if this is a note modification or a note creation)

        if(!noteFile.exists()){
            noteFile.createNewFile();

            File annuaireFile = new File("notes"+"/"+"annuaire.txt");

            // add entry to annuaire
            BufferedWriter bfw = new BufferedWriter(new FileWriter(annuaireFile,true));
            StringBuilder stb = new StringBuilder();
            stb.append(this.timestamp_creation);
            stb.append(";");
            stb.append(this.title);
            stb.append(";");
            stb.append(this.timestamp_link_event);
            stb.append("\n");

            bfw.write(stb.toString());

            bfw.close();

        }


        FileOutputStream fos = new FileOutputStream(noteFile);
        fos.write(content);
        fos.close();
    }

    public boolean deleteNote(){
        // remove entry from annuaire

        File annuaireFile = new File("notes"+"/"+"annuaire.txt");

        try{

            BufferedReader bfr = new BufferedReader(new FileReader(annuaireFile));


            StringBuilder stb = new StringBuilder();
            while(bfr.ready()){
                String entry = bfr.readLine();

                System.out.println(entry);
                // write all entries except the one we want to remove
                if(!entry.startsWith(getTimestamp_creation())){
                    System.out.println("passed");
                    stb.append(entry).append("\n");
                }
            }

            bfr.close();

            // write after as the FileWriter will erase the old content,
            // thus not allowing us to read entry by entry
            BufferedWriter bfw = new BufferedWriter(new FileWriter(annuaireFile));
            bfw.write(stb.toString());
            bfw.close();

        }catch (IOException e){
            throw new RuntimeException(e);
        }




        File noteFile = new File(
                "notes"+"/"+getTimestamp_creation()+".txt"
        );

        return noteFile.delete();
    }
}
