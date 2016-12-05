package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.OptionalInt;

@Value
public class RangeInt {

    OptionalInt lte;
    OptionalInt gte;

    @JsonCreator
    public RangeInt(OptionalInt lte, OptionalInt gte) {
        this.lte = lte;
        this.gte = gte;
    }
}
