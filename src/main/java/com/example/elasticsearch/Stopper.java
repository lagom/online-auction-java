package com.example.elasticsearch;

public class Stopper {

    public static void main(String... args) throws Exception {
        System.out.println(NodeHolder.node);
        if (NodeHolder.node != null)
            NodeHolder.node.close();
    }
}
