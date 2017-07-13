package a.b.c

import a.d.Base

trait Middle {

  trait InnerTrait extends Base

  abstract class Inner extends InnerTrait {
    def m2() = m()
  }
}
