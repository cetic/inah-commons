package be.cetic.inah.commons.path

import scala.util.matching.Regex

object PathImplicits {

  implicit def toStringToPath(s: String): StringToPath = new StringToPath(s)

  implicit def toPathImplicit(path: Path): PathImplicits = new PathImplicits(path)

  implicit def toPathImplicit(s: String): PathImplicits = new PathImplicits(s.toPath)

}


class StringToPath(s: String) {
  private val parameterPattern = raw"\{([a-zA-Z]*)\}".r


  def toPath: Path = {
    val segments = s.split("/")
      .filter(_.nonEmpty)
      .map(toSegment).toSeq
    Path(segments)
  }

  private def toSegment(s: String): PathSegment = {
    s match {
      case "#" => Sharp(None)
      case parameterPattern(ref) => Sharp(Some(ref))
      case "*" => WildCard
      case s: String => Segment(s)
    }
  }
}


class PathImplicits(val path: Path) {

  def /(other: Path): Path = Path(path.segments ++ other.segments)

  def /(other: PathImplicits): Path = /(other.path)

  override def toString: String = {
    "/" + path.segments.map {
      case Sharp(None) => "#"
      case Sharp(Some(ref)) => "{" + ref + "}"
      case WildCard => "*"
      case Segment(s) => s
      case _ => throw new Exception("Unsupported path segment")
    }.mkString("/")
  }

  def isCompatible(other: Path): Boolean = {
    isCompatible(path.segments, other.segments)
  }

  def matchPath(other: Path): Map[String, String] = {
    matchPath(path.segments, other.segments)
  }

  private def matchPath(seg1: Seq[PathSegment], seg2: Seq[PathSegment]): Map[String, String] = {
    var matchMap = Map[String, String]()
    if (isCompatible(seg1, seg2)) {
      seg1.zip(seg2).foreach {
        case (s1: Sharp, s2: Segment) => s1.ref.foreach(ref => matchMap = matchMap.updated(ref, s2.name))
        case (s2: Segment, s1: Sharp) => s1.ref.foreach(ref => matchMap = matchMap.updated(ref, s2.name))
        case _ =>
      }
    }
    matchMap
  }

  private def isCompatible(seg1: Seq[PathSegment], seg2: Seq[PathSegment]): Boolean = {
    (seg1, seg2) match {
      case (Nil, Nil) => true
      case (Seq(WildCard), _) | (_, Seq(WildCard)) => true
      case (seq1, seq2) if (seq1.isEmpty && seq2.nonEmpty) || (seq2.isEmpty && seq1.nonEmpty) => false
      case (Seq(Sharp(_), tail1@_*), Seq(p: PathSegment, tail2@_*)) => isCompatible(tail1, tail2)
      case (Seq(p: PathSegment, tail1@_*), Seq(Sharp(_), tail2@_*)) => isCompatible(tail1, tail2)
      case (Seq(Segment(s1), tail1@_*), Seq(Segment(s2), tail2@_*)) if s1 == s2 => isCompatible(tail1, tail2)
      case (Seq(Segment(s1), _), Seq(Segment(s2), _)) if s1 != s2 => false
      case _ => throw new Exception(s"Unhandled case ${Path(seg1).toString} Vs ${Path(seg2).toString}")
    }

  }
}


