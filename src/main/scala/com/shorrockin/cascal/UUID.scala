package com.shorrockin.cascal

import java.util.{UUID => JavaUUID}
import com.eaio.uuid.{UUID => EaioUUID}

/**
 * utility method for working with, and creating uuids, suitable in a way
 * that they can be used within Cassandra for time based, and non time based
 * operations.
 *
 * @author Chris Shorrock
 */
object UUID {
  
  /**
   * returns a new uuid, can be used as a time uuid
   */
  def uuid = JavaUUID.fromString(new EaioUUID().toString());

  /**
   * returns a new uuid based on the specified string
   */
  def uuid(data:Array[Byte]):JavaUUID = {
    var msb = 0L
    var lsb = 0
    assert(data.length == 16)

    (0 until 8).foreach  { (i) => msb = (msb << 8) | (data(i) & 0xff) }
    (8 until 16).foreach { (i) => lsb = (lsb << 8) | (data(i) & 0xff) }

    val mostSigBits = msb
    val leastSigBits = lsb

    JavaUUID.fromString(new EaioUUID(msb,lsb).toString)
  }

  implicit def toString(source:JavaUUID) = new String(toBytes(source), Conversions.utf8)

  /**
   * converts uuids to bytes
   */
  implicit def toBytes(source:JavaUUID):Array[Byte] = {
    val msb = source.getMostSignificantBits()
    val lsb = source.getLeastSignificantBits()
    val buffer = new Array[Byte](16)

    (0 until 8).foreach  { (i) => buffer(i) = (msb >>> 8 * (7 - i)).asInstanceOf[Byte] }
    (8 until 16).foreach { (i) => buffer(i) = (lsb >>> 8 * (7 - i)).asInstanceOf[Byte] }

    buffer
  }
}