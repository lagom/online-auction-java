package com.example.auction.item.impl;

import com.datastax.driver.core.BoundStatement;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;


// TODO: move to core?
public class CassandraReadSideUtils {

    public static CompletionStage<List<BoundStatement>> completedStatements(BoundStatement... statements) {
        return CassandraReadSide.completedStatements(Arrays.asList(statements));
    }

}
