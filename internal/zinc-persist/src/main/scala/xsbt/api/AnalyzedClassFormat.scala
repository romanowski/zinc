/* sbt -- Simple Build Tool
 * Copyright 2010 Mark Harrah
 */
package xsbt.api

import xsbti.SafeLazy
import xsbti.api._
import sbinary._
import sbinary.DefaultProtocol._
import sbt.internal.inc.APIs.emptyCompanions

object AnalyzedClassFormats {
  // This will throw out API information intentionally.
  def analyzedClassFormat(implicit ev0: Format[Compilation], ev1: Format[NameHashes]): Format[AnalyzedClass] =
    wrap[AnalyzedClass, (Long, String, Int, NameHashes, Boolean)](
      a => (a.compilationTimestamp(), a.name, a.apiHash, a.nameHashes, a.hasMacro),
      (x: (Long, String, Int, NameHashes, Boolean)) => x match {
        case (compilationTimestamp: Long, name: String, apiHash: Int, nameHashes: NameHashes, hasMacro: Boolean) =>
          new AnalyzedClass(compilationTimestamp, name, SafeLazy(emptyCompanions), apiHash, nameHashes, hasMacro)
      }
    )
}
final class InputWrapperStream(in: Input) extends java.io.InputStream {
  def toInt(b: Byte) = if (b < 0) b + 256 else b.toInt
  def read() = try { toInt(in.readByte) } catch { case e: sbinary.EOF => -1 }
  override def read(b: Array[Byte], off: Int, len: Int) = in.readTo(b, off, len)
}
final class OutputWrapperStream(out: Output) extends java.io.OutputStream {
  override def write(bs: Array[Byte], off: Int, len: Int) = out.writeAll(bs, off, len)
  def write(b: Int) = out.writeByte(b.toByte)
}
