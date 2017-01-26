package sbt.inc.cached

import java.io.File
import java.nio.file.Path

import sbt.internal.inc.cached._
import sbt.io.IO
import xsbti.compile.{ MiniSetup, CompileAnalysis }
import org.scalatest._

class ExportedCacheSpec extends CommonCachedCompilation("Exported Cache") {

  var cacheLocation: Path = _

  class TestVerifierResults extends VerficationResults {
    val categories = Set.newBuilder[String]
    val values = Set.newBuilder[String]
  }

  class TestVerifier extends CacheVerifier {
    val currentResults = new TestVerifierResults

    override protected def analyzeValue(category: String, serializedValue: String, deserializedValue: Any): Unit = {
      currentResults.categories += category
      currentResults.values += deserializedValue.toString
      ()
    }

    override def results: VerficationResults = currentResults
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    cacheLocation = remoteProject.baseLocation.resolveSibling("cache")
    val remoteCache = new ExportableCache(cacheLocation) {
      override protected def cacheVerifier(): CacheVerifier = new TestVerifier
    }

    remoteCache.exportCache(remoteProject.baseLocation.toFile, remoteAnalysisStore) match {
      case Some(results: TestVerifierResults) =>
        results.categories.result() should not be empty
        val values = results.values.result()
        values should not be empty
        remoteProject.allSources.map(_.toString).foreach {
          source => values should contain(source)
        }
      case Some(other) =>
        fail(s"Bad verification results: $other (of class ${other.getClass.getName}")
      case _ =>
        fail(s"No cache verification results.")
    }
  }

  override def remoteCacheProvider(): CacheProvider = new CacheProvider {
    override def findCache(previous: Option[(CompileAnalysis, MiniSetup)]): Option[CompilationCache] =
      Some(new ExportableCache(cacheLocation))
  }

  private class NonEmptyOutputFixture(tmpDir: File) {
    private def nonEmptyFile(parent: File, name: String) = {
      val f = new File(parent, name)
      IO.write(f, "some-text")
      f
    }

    val plainFileProjectMock = new File(tmpDir, "plain-file")
    val plainFileAsOutput = nonEmptyFile(plainFileProjectMock, remoteProject.defaultClassesDir.getName)

    val nonemptyDirProjectMock = new File(tmpDir, "non-empty-dir")
    val nonemptyDirOutputDir = new File(nonemptyDirProjectMock, remoteProject.defaultClassesDir.getName)
    val contentFile: File = nonEmptyFile(nonemptyDirOutputDir, "some-file")
    val contentDirectory = new File(nonemptyDirOutputDir, "some-dir")
    val contentDirectoryMember = nonEmptyFile(contentDirectory, "another")
    val contentDirectoryClassMember = nonEmptyFile(contentDirectory, "another.class")
  }

  it should "fail non empty output with FailOnNonEmpty" in IO.withTemporaryDirectory {
    tmpDir =>
      val fixture = new NonEmptyOutputFixture(tmpDir)
      import fixture._

      intercept[IllegalStateException] {
        new ExportableCache(cacheLocation, cleanOutputMode = FailOnNonEmpty).loadCache(nonemptyDirProjectMock)
      }

      assert(contentFile.exists())
      assert(contentDirectory.exists())
      assert(contentDirectory.list().nonEmpty)
  }

  it should "fail on plain file as classes dir" in IO.withTemporaryDirectory {
    tmpDir =>
      val fixture = new NonEmptyOutputFixture(tmpDir)
      import fixture._

      intercept[IllegalStateException] { new ExportableCache(cacheLocation).loadCache(plainFileProjectMock) }
      assert(plainFileProjectMock.exists())
  }

  it should "handle output with CleanOutput" in IO.withTemporaryDirectory {
    tmpDir =>
      val fixture = new NonEmptyOutputFixture(tmpDir)
      import fixture._

      new ExportableCache(cacheLocation).loadCache(nonemptyDirProjectMock)

      assert(!contentFile.exists())
      assert(!contentDirectory.exists())
  }

  it should "handle output with CleanClasses" in IO.withTemporaryDirectory {
    tmpDir =>
      val fixture = new NonEmptyOutputFixture(tmpDir)
      import fixture._

      new ExportableCache(cacheLocation, cleanOutputMode = CleanClasses).loadCache(nonemptyDirProjectMock)

      assert(contentFile.exists())
      assert(contentDirectory.exists())
      assert(contentDirectoryMember.exists())
      assert(!contentDirectoryClassMember.exists())
  }

}
