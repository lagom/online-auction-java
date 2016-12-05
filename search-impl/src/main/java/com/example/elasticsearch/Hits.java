package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.pcollections.PSequence;

import java.util.List;

@Value
public class Hits {
    @JsonProperty("hits")
    PSequence<HitResult> hits;


    @JsonCreator
    public Hits(PSequence<HitResult> hits) {
        this.hits = hits;
    }

    public List<HitResult> getHits() {
        return hits;
    }
}
