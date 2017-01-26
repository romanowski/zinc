package sbt.inc.cached

import java.io.File
import java.nio.file.{ Files, Path, Paths }

import org.scalatest.{ BeforeAndAfter, BeforeAndAfterAll }
import sbt.inc.BaseCompilerSpec
import sbt.internal.inc.cached.{ CacheAwareStore, CacheProvider, CompilationCache, ProjectRebasedCache }
import sbt.internal.inc.{ Analysis, AnalysisStore, BridgeProviderSpecification, FileBasedStore }
import sbt.io.IO
import xsbti.compile.{ CompileAnalysis, MiniSetup }

class ProjectRebaseCacheSpec extends CommonCachedCompilation("Project based cache") {

  override def remoteCacheProvider() = new CacheProvider {
    override def findCache(previous: Option[(CompileAnalysis, MiniSetup)]): Option[CompilationCache] =
      Some(ProjectRebasedCache(remoteProject.baseLocation, remoteProject.defaultStoreLocation.toPath))
  }

}
