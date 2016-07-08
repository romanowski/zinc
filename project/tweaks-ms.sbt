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

resolvers ++=  Seq(DefaultMavenRepository, myLocalIntellijRepo)//just remove Intellij resolvers