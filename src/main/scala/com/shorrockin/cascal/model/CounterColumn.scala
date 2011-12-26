package com.shorrockin.cascal.model

import java.nio.ByteBuffer
import org.apache.cassandra.thrift.{ColumnPath, ColumnOrSuperColumn}
import org.apache.cassandra.thrift.{CounterColumn => CassandraCounterColumn}
import org.apache.cassandra.thrift.{CounterSuperColumn => CassandraCounterSuperColumn}
import com.shorrockin.cascal.utils.Conversions
import scala.collection.JavaConversions._

case class CounterColumn[Owner](val name:ByteBuffer,
                         val value:Option[Long],
                         val owner:Owner) extends Gettable[CounterColumn[Owner], Option[Long]] {
  
  def this(name:ByteBuffer, owner:Owner) = this(name, None, owner)
  
  val key = owner.asInstanceOf[ColumnContainer[_, _]].key
  val family = key.family
  val keyspace = key.keyspace
  
  lazy val columnPath = {
    val out = new ColumnPath(family.value)
    owner match {
      case owner:SuperColumn => out.setColumn(name).setSuper_column(owner.value)
      case key:StandardKey => out.setColumn(name)
      case key:StandardCounterKey => out.setColumn(name)
    }
  }
  
  def +(newValue:Long) = new CounterColumn[Owner](name, Some(newValue), owner)
  
  def -(newValue:Long) = new CounterColumn[Owner](name, Some(-1 * newValue), owner)
  
  def convertGetResult(colOrSuperCol:ColumnOrSuperColumn):CounterColumn[Owner] = {
    val col = colOrSuperCol.getCounter_column
    CounterColumn(ByteBuffer.wrap(col.getName), Some(col.getValue), owner)
  }
  
  def cassandraColumn(): CassandraCounterColumn = new CassandraCounterColumn(name, value.get)
  
  lazy val columnOrSuperColumn = {
    val cosc = new ColumnOrSuperColumn
    owner match {
      case key:StandardCounterKey => cosc.setCounter_column(cassandraColumn())
      case sup:SuperColumn =>
        cosc.setCounter_super_column(new CassandraCounterSuperColumn(sup.value, cassandraColumn() :: Nil))
    }
  }
}