package models

import scalikejdbc._

case class CompanyRepository(
  id: Int,
  name: String) {

  def save()(implicit session: DBSession = CompanyRepository.autoSession): CompanyRepository = CompanyRepository.save(this)(session)

  def destroy()(implicit session: DBSession = CompanyRepository.autoSession): Int = CompanyRepository.destroy(this)(session)

}


object CompanyRepository extends SQLSyntaxSupport[CompanyRepository] {

  override val schemaName: Option[String] = Some("PUBLIC")

  override val tableName = "COMPANIES"

  override val columns = Seq("ID", "NAME")

  def apply(c: SyntaxProvider[CompanyRepository])(rs: WrappedResultSet): CompanyRepository = apply(c.resultName)(rs)
  def apply(c: ResultName[CompanyRepository])(rs: WrappedResultSet): CompanyRepository = new CompanyRepository(
    id = rs.get(c.id),
    name = rs.get(c.name)
  )

  val c: scalikejdbc.QuerySQLSyntaxProvider[scalikejdbc.SQLSyntaxSupport[CompanyRepository], CompanyRepository] = CompanyRepository.syntax("c")

  override val autoSession: AutoSession.type = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[CompanyRepository] = {
    withSQL {
      select.from(CompanyRepository as c).where.eq(c.id, id)
    }.map(CompanyRepository(c.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[CompanyRepository] = {
    withSQL(select.from(CompanyRepository as c)).map(CompanyRepository(c.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(CompanyRepository as c)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[CompanyRepository] = {
    withSQL {
      select.from(CompanyRepository as c).where.append(where)
    }.map(CompanyRepository(c.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[CompanyRepository] = {
    withSQL {
      select.from(CompanyRepository as c).where.append(where)
    }.map(CompanyRepository(c.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(CompanyRepository as c).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    id: Int,
    name: String)(implicit session: DBSession = autoSession): CompanyRepository = {
    withSQL {
      insert.into(CompanyRepository).namedValues(
        column.id -> id,
        column.name -> name
      )
    }.update.apply()

    CompanyRepository(
      id = id,
      name = name)
  }

  def batchInsert(entities: collection.Seq[CompanyRepository])(implicit session: DBSession = autoSession): List[Int] = {
    val params: collection.Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        Symbol("id") -> entity.id,
        Symbol("name") -> entity.name))
    SQL("""insert into COMPANIES(
      ID,
      NAME
    ) values (
      {id},
      {name}
    )""").batchByName(params.toSeq: _*).apply[List]()
  }

  def save(entity: CompanyRepository)(implicit session: DBSession = autoSession): CompanyRepository = {
    withSQL {
      update(CompanyRepository).set(
        column.id -> entity.id,
        column.name -> entity.name
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: CompanyRepository)(implicit session: DBSession = autoSession): Int = {
    withSQL { delete.from(CompanyRepository).where.eq(column.id, entity.id) }.update.apply()
  }

}
