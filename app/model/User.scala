package model

case class User (
  id: Long,
  name: String,
  companyId: Option[Int] = None)
