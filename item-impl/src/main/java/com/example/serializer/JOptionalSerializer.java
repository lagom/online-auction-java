package com.example.serializer;

import akka.actor.*;
import akka.serialization.*;

import java.util.Optional;
import java.util.function.Supplier;


/**
 *
 */
public class JOptionalSerializer extends SerializerWithStringManifest {


    private String separator = ":";
    private String emptyManifest = "E";

    private final ExtendedActorSystem system;
    private final Supplier<Serialization> serialization;

    public JOptionalSerializer(ExtendedActorSystem system) {
        this.system = system;
        // Need to use a Supplier<T> (aka poor man's lazy val) to avoid an infinite loop on Extension initialization.
        serialization = () -> SerializationExtension.get(system);
    }

    @Override
    public int identifier() {
        return 581884278;
    }

    @Override
    public String manifest(Object obj) {
        Optional<Object> optional = (Optional<Object>) obj;
        if (optional.isPresent()) {
            Object obj1 = optional.get();
            Class<?> innerClass = obj1.getClass();
            String fqcn = innerClass.getCanonicalName();
            String manifest = serializer(innerClass).manifest(obj1);
            return "P" + separator + fqcn + separator + manifest;
        } else {
            return emptyManifest;
        }
    }

    @Override
    public byte[] toBinary(Object obj) {
        Optional<Object> optional = (Optional<Object>) obj;
        if (optional.isPresent()) {
            Object innerObject = optional.get();
            return serializer(innerObject.getClass()).toBinary(innerObject);
        } else {
            return new byte[0];
        }
    }

    @Override
    public Object fromBinary(byte[] bytes, String manifest) {
        String[] splits = manifest.split(separator);

        if (splits[0].equals(emptyManifest)) {
            return Optional.empty();
        } else {
            try {
                Class<?> clazz = system.dynamicAccess().classLoader().loadClass(splits[1]);
                Object innerObj = serializer(clazz).fromBinary(bytes, splits[2]);
                return Optional.of(innerObj);
            } catch (ClassNotFoundException e) {
                return Optional.empty();
            }
        }
    }


    private SerializerWithStringManifest serializer(Class<?> clazz) {
        return (SerializerWithStringManifest) serialization.get().serializerFor(clazz);
    }


}
