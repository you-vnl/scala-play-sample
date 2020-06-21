package repository

import scalikejdbc._

case class UserRepository(
                           id: Long,
                           name: String,
                           companyId: Option[Int] = None) {

  def save()(implicit session: DBSession = UserRepository.autoSession): UserRepository = UserRepository.save(this)(session)

  def destroy()(implicit session: DBSession = UserRepository.autoSession): Int = UserRepository.destroy(this)(session)

}


object UserRepository extends SQLSyntaxSupport[UserRepository] {

  override val schemaName: Option[String] = Some("PUBLIC")

  override val tableName = "USERS"

  override val columns = Seq("ID", "NAME", "COMPANY_ID")

  def apply(u: SyntaxProvider[UserRepository])(rs: WrappedResultSet): UserRepository = apply(u.resultName)(rs)

  def apply(u: ResultName[UserRepository])(rs: WrappedResultSet): UserRepository = new UserRepository(
    id = rs.get(u.id),
    name = rs.get(u.name),
    companyId = rs.get(u.companyId)
  )

  val u: scalikejdbc.QuerySQLSyntaxProvider[scalikejdbc.SQLSyntaxSupport[UserRepository], UserRepository] = UserRepository.syntax("u")

  override val autoSession: AutoSession.type = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[UserRepository] = {
    withSQL {
      select.from(UserRepository as u).where.eq(u.id, id)
    }.map(UserRepository(u.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[UserRepository] = {
    withSQL(select.from(UserRepository as u)).map(UserRepository(u.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(UserRepository as u)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[UserRepository] = {
    withSQL {
      select.from(UserRepository as u).where.append(where)
    }.map(UserRepository(u.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[UserRepository] = {
    withSQL {
      select.from(UserRepository as u).where.append(where)
    }.map(UserRepository(u.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(UserRepository as u).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              name: String,
              companyId: Option[Int] = None)(implicit session: DBSession = autoSession): UserRepository = {
    val generatedKey = withSQL {
      insert.into(UserRepository).namedValues(
        column.name -> name,
        column.companyId -> companyId
      )
    }.updateAndReturnGeneratedKey.apply()

    UserRepository(
      id = generatedKey,
      name = name,
      companyId = companyId)
  }

  def batchInsert(entities: collection.Seq[UserRepository])(implicit session: DBSession = autoSession): List[Int] = {
    val params: collection.Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        Symbol("name") -> entity.name,
        Symbol("companyId") -> entity.companyId))
    SQL(
      """insert into USERS(
      NAME,
      COMPANY_ID
    ) values (
      {name},
      {companyId}
    )""").batchByName(params.toSeq: _*).apply[List]()
  }

  def save(entity: UserRepository)(implicit session: DBSession = autoSession): UserRepository = {
    withSQL {
      update(UserRepository).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.companyId -> entity.companyId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: UserRepository)(implicit session: DBSession = autoSession): Int = {
    withSQL {
      delete.from(UserRepository).where.eq(column.id, entity.id)
    }.update.apply()
  }

  /**
   * ユーザリストをDBから取得します。
   *
   * @return ユーザリスト
   */
  def getUserList: Seq[UserRepository] = {
    DB.readOnly { implicit session =>
      withSQL {
        select.from(UserRepository as u).orderBy(u.id.asc)
      }.map(UserRepository(u.resultName))
        .list.apply()
    }
  }

  /**
   * ユーザの作成を行います。
   *
   * @param name      ユーザ名
   * @param companyId 会社ID
   */
  def createUser(name: String, companyId: Int): Unit = {
    DB.localTx { implicit session =>
      UserRepository.create(name, Option(companyId))
    }
  }


  /**
   * ユーザの登録を行います。
   *
   * @param id        ユーザID
   * @param name      ユーザ名
   * @param companyId 会社ID
   */
  def saveUser(id: Long, name: String, companyId: Option[Int]): Unit = {
    DB.localTx { implicit session =>
      UserRepository.find(id).foreach { user =>
        UserRepository.save(user.copy(name = name, companyId = companyId))
      }
    }
  }

  /**
   * ユーザの削除を行います。
   *
   * @param id ユーザID
   */
  def removeUser(id: Long): Unit = {
    DB.localTx { implicit session =>
      UserRepository.find(id).foreach { user =>
        UserRepository.destroy(user)
      }
    }
  }

}
