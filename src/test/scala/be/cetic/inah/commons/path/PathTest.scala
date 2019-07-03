package path

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import be.cetic.inah.commons.path._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class PathTest extends TestKit(ActorSystem("PathSchemaTest")) with ImplicitSender with WordSpecLike
  with Matchers with BeforeAndAfterAll {

  import PathImplicits._

  "Strings with / " should {
    "produce pathes" in {
      assert("p1" / "p2" == Path(Seq(Segment("p1"), Segment("p2"))))
    }

    "handles sharp" in {
      assert("#" / "p2" == Path(Seq(Sharp(None), Segment("p2"))))
    }

    "handles parameterPath" in {
      assert("{path}" / "p2" == Path(Seq(Sharp("path"), Segment("p2"))))
    }


    "handles wildcard symbol" in {
      assert("p1" / "*" == Path(Seq(Segment("p1"), WildCard)))
    }

    "handles single path segment" in {
      assert("#".toPath == Path(Seq(Sharp())))
    }
  }

  "Pathes" should {
    "be constructed out of strings" in {
      assert("/coucou/test".toPath == "coucou" / "test")
    }

    "compare correctly full pathes" in {
      assert("p1" / "p2" isCompatible "p1" / "p2")
    }

    "compare correctly wildcards 1" in {
      assert("#" / "p2" isCompatible "p1" / "p2")
    }

    "compare correctly wildcards 2" in {
      assert("p1" / "#" isCompatible "p1" / "p2")
    }

    "compare correctly wildcards 3" in {
      assert("p1" / "p2" isCompatible "#" / "p2")
    }

    "compare correctly wildcards 4" in {
      assert("p1" / "p2" isCompatible "p1" / "#")
    }

    "compare correctly named wildcards " in {
      assert("p1" / "p2" isCompatible "p1" / "{path}")
    }
    "compare correctly * 1" in {
      assert("p1" / "*" isCompatible "p1" / "p2" / "p3")
    }

    "compare correctly * 2" in {
      assert("p1" / "*" isCompatible "p1" / "p2" / "p3")
    }

    "compare correctly * 3" in {
      assert("*".toPath isCompatible "p1" / "p2" / "p3")
    }

    "compare correctly incompatible pathes 1" in {
      assert(!("p1" / "#" isCompatible "p2" / "p3"))
    }

    "compare correctly incompatible pathes 2" in {
      assert(!("p1" / "p2" isCompatible "p1" / "p2" / "p3"))
    }

    "extract path parameters" in {
      val matching ="{p}" / "world" matchPath "hello" / "world"
      assert(matching== Map("p"->"hello") )
    }
  }


}
