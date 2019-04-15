package com.example.stemcomp2019;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PathFinder {
    public static String TAG = "PathFinder";
    private Tag current0;
    private Tag last;
    private Tag dest;
    private int dir;

    private ArrayList<Tag> tags;
    private Map<Tag,ArrayList<Edge>> edgeList;
    private Map<Tag,Integer> distances;
    private Map<Tag,Edge> parent;
    private Map<Tag,Boolean> sptSet;

    public PathFinder(Tag start, Tag dest) {
        this.current0 = start;
        this.last = null;
        this.dest = dest;
        this.dir = 0;

        tags = Tag.getAllTags();
        edgeList = new HashMap<>();
        distances = new HashMap<>();
        parent = new HashMap<>();
        sptSet = new HashMap<>();
        makeGraph();
    }

    private void makeGraph() {
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            edgeList.put(tag, tag.edges);
        }
    }

    public String update(Tag current) {
        if (current == this.current0) return "";
        if (current == this.dest) return "You have arrived at " + this.dest.label + ".";
        this.last = this.current0;
        this.current0 = current;
        ArrayList<Edge> path1 = dijkstra(this.last, current);
        ArrayList<Edge> path2 = dijkstra(current, this.dest);
        if (path1.size() == 0 || path2.size() == 0) return "";
        this.dir = path1.get(path1.size() - 1).dir;
        int dir2 = path2.get(0).dir;
        if (this.dir == Tag.getOppositeDir(dir2))
            return "You're going the wrong way. Turn around.";
        else {
            String turn0 = getTurn(this.dir, dir2);
            if (!turn0.equals("")) return turn0 + " turn coming up.";
            for (int i = 1; i < path2.size(); i++) {
                Edge edge0 = path2.get(i - 1);
                Edge edge1 = path2.get(i);
                if (edge0.dir != edge1.dir) {
                    if (i < 4) {
                        String turn = getTurn(edge0.dir, edge1.dir);
                        return turn + " turn coming up.";
                    }
                    else return "You're going the right way.";
                }
            }
        }
        return "Your destination is ahead.";
    }

    public String getTurn(int dir1, int dir2) {
        int dir1inv = Tag.getOppositeDir(dir1);
        if (dir2 == dir1inv - 1 || (dir2 == 3 && dir1inv == 0)) return "Left";
        else if (dir1inv == dir2 - 1 || (dir1inv == 3 && dir2 == 0)) return "Right";
        return "";
    }

    public ArrayList<Edge> dijkstra(Tag source, Tag destination) {
        ArrayList<Edge> path = new ArrayList<Edge>();

        for(Tag tag: tags) {
            distances.put(tag, 10000);
            sptSet.put(tag, false);
            parent.clear();
        }
        distances.put(source, 0);

        for(int i=0; i<tags.size(); i++) {
            Tag min = minDistance();
            sptSet.put(min, true);

            ArrayList<Edge> edges = edgeList.get(min);

            for (Edge edge: edges) {
                Tag current = edge.end;
                if(!sptSet.get(current) && distances.get(min) + edge.weight < distances.get(current)) {
                    distances.put(current, distances.get(min) + edge.weight);
                    parent.put(current, edge);
                }
            }
        }

        while (parent.get(destination) != null && destination != source) {
            path.add(parent.get(destination));
            destination = parent.get(destination).start;
        }

        Collections.reverse(path);
        return path;
    }

    private Tag minDistance() {
        int min = 100000;
        Tag minTag = null;

        for(Tag tag: tags) {
            if(sptSet.get(tag) == false && distances.get(tag) < min) {
                min = distances.get(tag);
                minTag = tag;
            }
        }
        return minTag;
    }
}
