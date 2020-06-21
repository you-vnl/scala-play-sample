package repository

import org.scalatest._
import repository.UserRepository
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._


class UserRepositorySpec extends fixture.FlatSpec with Matchers with AutoRollback {

  config.DBs.setup()

  val u = UserRepository.syntax("u")

  behavior of "Users"

  it should "find by primary keys" in { implicit session =>
    val maybeFound = UserRepository.find(1L)
    maybeFound.isDefined should be(true)
  }
  it should "find by where clauses" in { implicit session =>
    val maybeFound = UserRepository.findBy(sqls.eq(u.id, 1L))
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    val allResults = UserRepository.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    val count = UserRepository.countAll()
    count should be >(0L)
  }
  it should "find all by where clauses" in { implicit session =>
    val results = UserRepository.findAllBy(sqls.eq(u.id, 1L))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    val count = UserRepository.countBy(sqls.eq(u.id, 1L))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    val created = UserRepository.create(name = "MyString")
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    val entity = UserRepository.findAll().head
    // nameを変更
    val modified = entity.copy(name = "modify")
    val updated = UserRepository.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    val entity = UserRepository.findAll().head
    val deleted = UserRepository.destroy(entity)
    deleted should be(1)
    val shouldBeNone = UserRepository.find(1L)
    shouldBeNone.isDefined should be(false)
  }
  it should "perform batch insert" in { implicit session =>
    val entities = UserRepository.findAll()
    entities.foreach(e => UserRepository.destroy(e))
    val batchInserted = UserRepository.batchInsert(entities)
    batchInserted.size should be >(0)
  }
}
