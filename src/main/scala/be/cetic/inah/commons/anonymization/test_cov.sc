
import scala.reflect.

trait Dto


case class Dto1(id: Option[Int]) extends Dto

case class Dto2(id: Option[String]) extends Dto
case class Dto3(id: Option[Int]) extends Dto


trait DaoWrite[A] {
  def write(a: A) : A={
    println(s"Created: $a}")
    a
  }
}

trait DaoRead[+A, -PK] {
  def read(i: PK): A
}


trait Dao[A, -PK] extends DaoWrite[A] with DaoRead[A, PK]

implicit object DtoDao extends Dao[Dto1, Int]  {
  def read(i: Int): Dto1 = Dto1(Some(i))
}

implicit object Dto2Dao extends Dao[Dto2, String]{
  override def read(i: String): Dto2 = Dto2(Some(i))
}

implicit object DtoDao3 extends Dao[Dto3, Int]  {
  def read(i: Int): Dto3 = Dto3(Some(i))
}


def read[A, PK](id: PK)(implicit dao: DaoRead[A, PK]) = dao.read(id)

def write[A](a: A)(implicit dao: DaoWrite[A]) = dao.write(a)


write(Dto1(Some(1)))

write(Dto2(Some("id")))

write(Dto3(Some(5)))

read[Dto1, Int](1)

read[Dto2, String]("string")

read[Dto3, Int](5)

case class Write[+A](a: A)

case class Read[+A, PK](pk: PK)



def receive[A: TypeTag] PartialFunction[Any, Unit]={
  case Write(a:Dto1) =>println("pattern1"); write(a)
  case Write(a:Dto2) => write(a)
  case Write(a:Dto3) => write(a)
  case r: Read[Dto1, Int] => read(r.pk)
  case r: Read[Dto2, String] => read(r.pk)
  case r: Read[Dto3, Int] => read(r.pk)
}

receive(Write(Dto2(Some("Coucou"))))

receive(Read[Dto2, String]("test"))