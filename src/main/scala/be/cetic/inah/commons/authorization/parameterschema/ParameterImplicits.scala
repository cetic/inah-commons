package be.cetic.inah.commons.authorization.parameterschema

object ParameterImplicits {
  implicit def stringParameterOps(name: String): StringParameterOps = new StringParameterOps(name)

  implicit def parameterOps(parameter: Parameter): ParameterOps = new ParameterOps(parameter)


}

class StringParameterOps(name: String) {
  def toParameter: Parameter = {
    name match {
      case "*" => AnyParameter
      case _ => NamedParameter(name)
    }
  }
}
class ParameterOps(parameter: Parameter) {
  def ! : Parameter = parameter match {
    case n: NamedParameter => n.copy(required = true)
    case AnyParameter => AnyParameter
    case _ => ???
  }

  def ? : Parameter = parameter match {
    case n: NamedParameter => n.copy(required = false)
    case AnyParameter => AnyParameter
    case _ => ???
  }
}