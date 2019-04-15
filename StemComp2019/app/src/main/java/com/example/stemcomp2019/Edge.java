package com.example.stemcomp2019;

public class Edge {
    public Tag start;
    public Tag end;
    public int dir;
    public int weight;

    public Edge(Tag start, Tag end, int dir, int weight) {
        this.start = start;
        this.end = end;
        this.dir = dir;
        this.weight = weight;
    }

}
