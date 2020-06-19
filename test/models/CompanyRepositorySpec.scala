package models

import org.scalatest._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._


class CompanyRepositorySpec extends fixture.FlatSpec with Matchers with AutoRollback {
  config.DBs.setup()

  val c = CompanyRepository.syntax("c")

  // テスト終了後に全てロールバックをしてくれる
  override def fixture(implicit session: DBSession): Unit = {
    SQL("insert into COMPANIES values (?, ?)").bind(123, "test_company1").update.apply()
    SQL("insert into COMPANIES values (?, ?)").bind(234, "test_company2").update.apply()
  }

  behavior of "Companies"

  it should "find by primary keys" in { implicit session =>
    val maybeFound = CompanyRepository.find(123)
    maybeFound.isDefined should be(true)
  }
  it should "find by where clauses" in { implicit session =>
    val maybeFound = CompanyRepository.findBy(sqls.eq(c.id, 123))
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    val allResults = CompanyRepository.findAll()
    allResults.size should be > (0)
  }
  it should "count all records" in { implicit session =>
    val count = CompanyRepository.countAll()
    count should be > (0L)
  }
  it should "find all by where clauses" in { implicit session =>
    val results = CompanyRepository.findAllBy(sqls.eq(c.id, 123))
    results.size should be > (0)
  }
  it should "count by where clauses" in { implicit session =>
    val count = CompanyRepository.countBy(sqls.eq(c.id, 123))
    count should be > (0L)
  }
  it should "create new record" in { implicit session =>
    // 一意なIDを指定
    val created = CompanyRepository.create(id = 999, name = "MyString")
    created should not be (null)
  }
  it should "save a record" in { implicit session =>
    val entity = CompanyRepository.findAll().head
    // nameを変更
    val modified = entity.copy(name = "modify")
    val updated = CompanyRepository.save(modified)
    updated should not equal (entity)
  }
  it should "destroy a record" in { implicit session =>
    // フィクスチャで生成したデータが削除対象
    val entity = CompanyRepository.find(123).head
    val deleted = CompanyRepository.destroy(entity)
    deleted should be(1)
    val shouldBeNone = CompanyRepository.find(123)
    shouldBeNone.isDefined should be(false)
  }
  it should "perform batch insert" in { implicit session =>
    // フィクスチャで生成したデータをバッチ更新
    val entities = CompanyRepository.findAllBy(sqls.in(c.id, Seq(123, 234)))
    entities.foreach(e => CompanyRepository.destroy(e))
    val batchInserted = CompanyRepository.batchInsert(entities)
    batchInserted.size should be > (0)
  }
}
