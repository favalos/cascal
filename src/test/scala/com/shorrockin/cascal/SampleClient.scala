package com.shorrockin.cascal

import model.{StringValue, ByteValue, ColumnValue}

object SampleClient {
  import Conversions._
  import collection.jcl.Conversions._

  def log(str:String, args:String*) = System.out.println(str.format(args:_*))
  def now = System.currentTimeMillis

  val Presence = "NOS" \ "Presence"
  val Events = "NOS" \\ "Events"

  def main(args:Array[String]) {
    val session = new Session("shorrockin.com", 9160, Consistency.One)

    try {
      log("[%s] Cluster Name: %s", now, session.clusterName)
      log("[%s] Version: %s", now, session.version)
      log("[%s] Keyspaces: %s", now, session.keyspaces.mkString("", ",", ""))
      // log("[%s] Config File: %s", now, session.configFile)
      log("---------------------------------------------------")
      testStandard(session)
      log("---------------------------------------------------")
      testSuper(session)
    } finally {
      session.close()
    }
  }

  def testStandard(session:Session) = {
    log("[%s] standard about to insert/query", now)
    val key    = Presence \ "1"
    val value1 = Presence \ "1" \ "Moo" \ ("Presence Time: " + new java.util.Date().toString)
    val value2 = Presence \ "1" \ "Foo" \ "Value 2"
    val value3 = Presence \ "1" \ "Bar" \ "Value 3"

    session.insert(value1)
    session.insert(value2)
    session.insert(value3)

    log("[%s] result1 value: %s", now, session.get(value1.name))
    log("[%s] result2 value: %s", now, session.get(value2.name))
    log("[%s] number of columns in key: %s", now, session.count(key))

    session.list(key).foreach { (value) =>
      log("[%s] queried key '1', got value: %s -> %s", now, value.name, value)
    }

    session.list(key, RangePredicate("Bar", "Foo")).foreach { (value) =>
      log("[%s] queried range 'Bar' to 'Foo' on Key #1 - got value: %s -> %s", now, value.name, value)
    }

    session.list(key, ColumnPredicate(List("Moo", "Bar"))).foreach { (value) =>
      log("[%s] queried columns 'Moo' & 'Bar' on Key #1 - got value: %s -> %s", now, value.name, value)
    }
  }


  def testSuper(session:Session) = {
    log("[%s] super about to insert/query", now)
    val key         = Events \ "1"
    val superColumn = Events \ "1" \ UUID()
    val value1      = superColumn \ "Message" \ ("Event Time: " + new java.util.Date().toString)
    val value2      = superColumn \ "Other Message" \ "Another Message"

    session.insert(value1)
    session.insert(value2)

    val columnResult = session.get(value1.name)
    val superResult  = session.get(superColumn)
    log("[%s] standard column result: %s", now, columnResult)
    log("[%s] super column get result for: %s", now, UUID(superColumn.value))
    superResult.foreach { (value) =>
      log("[%s]    %s -> %s", now, value.name, value)
    }
    
    log("[%s] number of super columns: %s, number of columns in first super: %s", now, session.count(key), session.count(superColumn))

    // example listing at the super key level
    val superKeyMap = session.list(key, RangePredicate(5))
    log("[%s] super column list return %s keys", now, superKeyMap.size)
    superKeyMap.foreach { (sc) =>
      log("[%s]  SuperKey: %s", now, sc._1)
      superKeyMap(sc._1).foreach { (value) =>
        log("[%s]    %s -> %s", now, value.name, value)
      }
    }

    // example listing at the super column level
    val recentInsert = session.list(superColumn)
    log("[%s] listed recent inserts, size: %s", now, recentInsert.size)
    recentInsert.foreach { (value) =>
      log("[%s]  %s -> %s", now, value.name, value)
    }

  }

  implicit def colSeqtoString(list:Seq[ColumnValue[_]]):String = (list.map { (cv) => "[%s -> %s]".format(cv.name, cv) }).mkString("", ",", "")
  implicit def intToString(i:Int):String = Integer.toString(i)
  implicit def longToString(l:Long):String = java.lang.Long.toString(l)
  implicit def byteValueToString(obj:ByteValue):String = string(obj.value)
  implicit def stringValueToString(obj:StringValue):String = obj.value

}