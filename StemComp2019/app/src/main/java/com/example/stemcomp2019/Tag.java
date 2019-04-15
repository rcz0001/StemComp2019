package com.example.stemcomp2019;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Tag implements Serializable {
    public String serial; //serial number of tag
    public String building; //building where tag is located (e.g. Conestoga High School)
    public String label; //room corresponding to tag (e.g. "room 235")
    public int index;
    public ArrayList<int[]> edgesData;
    transient public ArrayList<Edge> edges;

    private static ArrayList<Tag> tags = new ArrayList<>();

    private Tag(String serial, String building, String label) {
        this.serial = serial;
        this.building = building;
        this.label = label;
        this.edgesData = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.index = tags.size();
        tags.add(this);
    }

    public void addEdge(Tag end, int dir) {
        this.edgesData.add(new int[]{end.index, dir});
        Edge edge = new Edge(this, end, dir, 1);
        this.edges.add(edge);
    }

    /* Adds a new Tag to the database */
    public static Tag addTag(String serial, String label, int dir, Tag previous) {
        if (previous != null && serial.equals(previous.serial)) return previous;
        Tag tag = findTag(serial);
        if (tag == null) tag = new Tag(serial, "", label);
        if (previous != null) {
            int dir2 = getOppositeDir(dir);
            previous.addEdge(tag, dir);
            tag.addEdge(previous, dir2);
        }
        return tag;
    }

    public static Tag findTag(String serial) {
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            if (tag.serial.equals(serial))
                return tag;
        }
        return null;
    }

    /* Returns all Tags stored in the database */
    public static ArrayList<Tag> getAllTags() {
        return tags;
    }

    public static void setTags(ArrayList<Tag> tags0) {
        tags = tags0;
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            tag.edges = new ArrayList<>();
            for (int j = 0; j < tag.edgesData.size(); j++) {
                int[] data = tag.edgesData.get(j);
                Edge edge = new Edge(tag, tags.get(data[0]), data[1], 1);
                tag.edges.add(edge);
            }
        }
    }

    public static int getOppositeDir(int dir) {
        if (dir >= 2) return dir - 2;
        return dir + 2;
    }

    public static double[] getConfidences(String query, ArrayList<Tag> tags) {
        double[] confidences = new double[tags.size()];

        ArrayList<ArrayList<String>> substrings = new ArrayList<>();
        for(Tag tag: tags){
            ArrayList<String> temp = genSubstrings(tag.label);
            substrings.add(temp);
        }

        ArrayList<String> queryString = genSubstrings(query);

        for(int i=0; i<substrings.size(); i++){
            int cnt = 0;
            ArrayList<String> curSubstring = substrings.get(i);
            for(String cur: queryString)
                if(curSubstring.indexOf(cur) != -1) cnt++;
            double percent = (double) cnt/ queryString.size();
            confidences[i] = percent;
        }
        //your code here

        return confidences;
    }

    public static ArrayList<String> genSubstrings(String query){
        ArrayList<String> querySubstrings = new ArrayList<String>();
        String cur;
        for(int i=1; i<=query.length(); i++){
            for(int j=0; j<query.length()-i+1; j++){
                cur = query.substring(j,j+i);
                String copy = new String();
                copy = cur;
                querySubstrings.add(copy);
            }
        }
        return querySubstrings;
    }

}
