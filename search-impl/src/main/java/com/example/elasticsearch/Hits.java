package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.pcollections.PSequence;

import java.util.List;

@Value
public class Hits {

    PSequence<HitResult> hits;
    int total;

    @JsonCreator
    public Hits(PSequence<HitResult> hits, int total) {
        this.hits = hits;
        this.total = total;
    }

    public List<HitResult> getHits() {
        return hits;
    }
}
