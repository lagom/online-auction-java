package com.example.elasticsearch;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class Starter {

    public static void main(String... args) throws Exception {

        Settings.Builder settingsBuilder = Settings.builder();

        settingsBuilder.put("node.name", "embedded");
        settingsBuilder.put("path.home", "com/target/scala-2.11/classes");
        settingsBuilder.put("path.data", "target/es-data");
        settingsBuilder.put("http.enabled", true);

        Settings settings = settingsBuilder.build();

        NodeHolder.node = NodeBuilder.nodeBuilder()
                .settings(settings)
                .data(true).local(true).node();
        NodeHolder.node.start();
    }

}

