package com.example.serializer

import java.util.Optional

import akka.actor.ExtendedActorSystem
import akka.serialization.{BaseSerializer, SerializationExtension, SerializerWithStringManifest}

/**
  *
  */
class OptionalSerializer(val system: ExtendedActorSystem)
  extends SerializerWithStringManifest with BaseSerializer {

  private val separator = ':'

  private val emptyManifest: String = "E"

  // Must be lazy otherwise there's an infinite loop when loading the SerializationExtension
  lazy val serialization = SerializationExtension(system)

  // TODO: handle ClassCastException
  def serializer(clazz: Class[_]): SerializerWithStringManifest = serialization.serializerFor(clazz).asInstanceOf[SerializerWithStringManifest]

  override def manifest(obj: AnyRef): String = {
    val optional: Optional[AnyRef] = obj.asInstanceOf[Optional[AnyRef]]
    if (optional.isPresent) {
      val obj1 = optional.get()
      val fqcn = obj1.getClass.getCanonicalName
      val manifest = serializer(obj1.getClass).manifest(obj1)
      s"P$separator$fqcn$separator$manifest"
    } else {
      emptyManifest
    }
  }

  override def toBinary(obj: AnyRef): Array[Byte] = {
    val optional: Optional[AnyRef] = obj.asInstanceOf[Optional[AnyRef]]
    if (optional.isPresent) {
      val obj1 = optional.get()
      serializer(obj1.getClass).toBinary(obj1)
    } else
      Array.emptyByteArray
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    val splits: Array[String] = manifest.split(":")
    splits(0) match {
      case `emptyManifest` => Optional.empty()
      case _ => {
        val clazz = system.dynamicAccess.classLoader.loadClass(splits(1))
        Optional.of(serializer(clazz).fromBinary(bytes, splits(2)))
      }
    }
  }

}
