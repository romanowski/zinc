def myLocalIntellijRepo =  {
  import org.apache.ivy.plugins.resolver._
  val res = new FileSystemResolver
  res.setLocal(true)
  res.setCheckconsistency(false)
  res.setName("myLocalIntellijRepo")
  res.addIvyPattern("//v/global/user/k/kr/krzysztr/ZincRepo/[organisation]/[module]/ivy-[revision].xml")
  res.addArtifactPattern("//v/global/user/k/kr/krzysztr/ZincRepo/[organisation]/[module]/jars/[artifact]-[revision].[ext]")
  new RawRepository(res)
}

def myLocalTrainRepo =  {
  import org.apache.ivy.plugins.resolver._
  val res = new FileSystemResolver
  res.setLocal(true)
  res.setCheckconsistency(false)
  res.setName("myLocalTrainRepo")
  val outPath = "file:///D:/workspace/install/"
  res.addIvyPattern(s"$outPath/[revision]/[module]/ivy-[revision].xml")
  res.addArtifactPattern(s"$outPath/[revision]/[module]/[artifact]-[revision].[ext]")
  new RawRepository(res)
}

(resolvers in ThisBuild) ++=  Seq(DefaultMavenRepository, myLocalIntellijRepo)

(publishTo in ThisBuild) :=  Some(myLocalIntellijRepo)
