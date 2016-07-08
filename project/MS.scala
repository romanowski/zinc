import sbt._
import sbt.Keys._

object MS {
  def settings: Seq[Setting[_]] = {
    Seq(version := "1.0.0-M5ms", trainRelease)
  }

  def trainRelease = TaskKey[File]("trainRelease") := {

    val ouputDir = (Keys.`package` in Compile).value / ".."
    publishLocal.value // Just create all nice thigs
    val distName = artifact.value.name
    val output = file("..") / "install" / "common" / distName
    val trainVersion = "ms"
    val currentVersion = version.value
    val allRes = (ouputDir ** "*.jar").get ++ (ouputDir ** "*.xml").get

    allRes.foreach{
      file =>
        val fixedName = file.getName //.replace(currentVersion, trainVersion)
        IO.copyFile(file, output / fixedName)
    }

    output
  }
}