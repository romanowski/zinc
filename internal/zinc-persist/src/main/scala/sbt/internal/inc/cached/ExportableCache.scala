package sbt.internal.inc.cached

import java.io.File
import java.nio.file.{ Files, Path }

import sbt.internal.inc._
import sbt.io.{ PathFinder, IO }
import xsbti.compile.{ CompileAnalysis, MiniSetup, SingleOutput }

class ExportableCacheMapper(root: Path) extends AnalysisMappers {
  private def justRelativize = Mapper.relativizeFile(root)

  override val outputDirMapper: Mapper[File] = justRelativize
  override val sourceDirMapper: Mapper[File] = justRelativize
  override val sourceMapper: Mapper[File] = justRelativize
  override val productMapper: Mapper[File] = justRelativize
  override val binaryMapper: Mapper[File] = justRelativize

  override val binaryStampMapper: ContextAwareMapper[File, Stamp] =
    Mapper.updateModificationDateFileMapper(binaryMapper)
}

sealed trait CleanOutputMode
case object CleanOutput extends CleanOutputMode
case object FailOnNonEmpty extends CleanOutputMode
case object CleanClasses extends CleanOutputMode

class ExportableCache(val cacheLocation: Path, cleanOutputMode: CleanOutputMode = CleanOutput) extends CompilationCache {

  val analysisFile: Path = cacheLocation.resolve("analysis.zip")
  val classesZipFile: Path = cacheLocation.resolve("classes.zip")

  protected def createMapper(projectLocation: File) = new ExportableCacheMapper(projectLocation.toPath)

  private def outputDir(setup: MiniSetup): File = setup.output() match {
    case single: SingleOutput =>
      single.outputDirectory()
    case _ => throw new RuntimeException("Only single output is supported")
  }

  override def loadCache(projectLocation: File): Option[(CompileAnalysis, MiniSetup)] = {
    val store = FileBasedStore(analysisFile.toFile, createMapper(projectLocation))

    for ((newAnalysis: Analysis, newSetup) <- store.get()) yield {
      val output = outputDir(newSetup)

      if (output.exists()) {
        if (output.isDirectory) {
          cleanOutputMode match {
            case CleanOutput =>
              if (output.list().nonEmpty) IO.delete(output)
            case FailOnNonEmpty =>
              if (output.list().nonEmpty)
                throw new IllegalStateException(s"Output directory: $output is not empty and cleanOutput is false")
            case CleanClasses =>
              val classFiles = PathFinder(output) ** "*.class"
              IO.delete(classFiles.get)
          }
        } else throw new IllegalStateException(s"Output file: $output is not a directory")
      }

      IO.unzip(classesZipFile.toFile, output, preserveLastModified = true)
      (newAnalysis, newSetup)
    }
  }

  def exportCache(projectLocation: File, currentAnalysisStore: AnalysisStore): Unit = {
    for ((currentAnalysis: Analysis, currentSetup) <- currentAnalysisStore.get()) {
      val remoteStore = FileBasedStore(analysisFile.toFile, createMapper(projectLocation))

      val out = outputDir(currentSetup).toPath

      val entries = currentAnalysis.stamps.allProducts.map {
        classFile =>
          val mapping = out.relativize(classFile.toPath).toString
          classFile -> mapping
      }

      IO.zip(entries, classesZipFile.toFile)

      remoteStore.set(currentAnalysis, currentSetup)
    }
  }
}