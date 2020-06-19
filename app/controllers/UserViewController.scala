package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import javax.inject.Inject
import scalikejdbc._
import models._
import UserViewController._

class UserViewController @Inject()(components: MessagesControllerComponents)
  extends MessagesAbstractController(components) {

  private val u = UserRepository.syntax("u")
  private val c = CompanyRepository.syntax("c")


  /**
   * 一覧表示
   */
  def list: Action[AnyContent] = Action { implicit request =>
    DB.readOnly { implicit session =>
      // ユーザのリストを取得
      val users = withSQL {
        select.from(UserRepository as u)
          .leftJoin(CompanyRepository as c).on(u.companyId, c.id)
          .orderBy(u.id.asc)
      }.map { rs =>
        (UserRepository(u)(rs), rs.intOpt(c.resultName.id)
          .map(_ => CompanyRepository(c)(rs)))
      }.list.apply()
      Ok(views.html.user.list(users))
    }
  }

  /**
   * 編集画面表示
   */
  def edit(id: Option[Long]): Action[AnyContent] = Action { implicit request =>
    DB.readOnly { implicit session =>
      // リクエストパラメータにIDが存在する場合
      val form = id match {
        // IDが渡されなかった場合は新規登録フォーム
        case None => userForm
        // IDからユーザ情報を1件取得してフォームに詰める
        case Some(id) => UserRepository.find(id) match {
          case Some(user) => userForm.fill(UserForm(Some(user.id), user.name, user.companyId))
          case None => userForm
        }
      }

      // プルダウンに表示する会社のリストを取得
      val companies = withSQL {
        select.from(CompanyRepository as c).orderBy(c.id.asc)
      }.map(CompanyRepository(c.resultName)).list().apply()

      Ok(views.html.user.edit(form, companies))
    }
  }

  /**
   * 登録実行
   */
  def create: Action[AnyContent] = Action { implicit request =>
    DB.localTx { implicit session =>
      // リクエストの内容をバインド
      userForm.bindFromRequest().fold(
        // エラーの場合は編集画面に戻す
        error => {
          BadRequest(views.html.user.edit(error, CompanyRepository.findAll()))
        },
        // OKの場合
        form => {
          UserRepository.create(form.name, form.companyId)
          // 一覧画面へリダイレクト
          Redirect(routes.UserViewController.list())
        }

      )
    }
  }

  /**
   * 更新実行
   */
  def update: Action[AnyContent] = Action { implicit  request =>
    DB.localTx { implicit session =>
      userForm.bindFromRequest.fold(
        error => {
          BadRequest(views.html.user.edit(error, CompanyRepository.findAll()))
        },
        form => {
          // ユーザ情報を更新
          UserRepository.find(form.id.get).foreach { user =>
            UserRepository.save(user.copy(name = form.name, companyId = form.companyId))
          }
          Redirect(routes.UserViewController.list())
        }
      )
    }
  }

  /**
   * 削除実行
   */
  def remove(id: Long): Action[AnyContent] = Action { implicit request =>
    DB.localTx { implicit session =>
      // ユーザを削除
      UserRepository.find(id).foreach { user =>
        UserRepository.destroy(user)
      }
      Redirect(routes.UserViewController.list())
    }
  }

}

object UserViewController {

  /**
   * フォームの値を格納するケースクラス
   *
   * @param id ID
   * @param name 名前
   * @param companyId 会社ID
   */
  case class UserForm(id: Option[Long], name: String, companyId: Option[Int])

  /**
   * ユーザーフォームのコンパニオンオブジェクト
   */
  val userForm: Form[UserForm] = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText(maxLength = 20),
      "companyID" -> optional(number)
    )(UserForm.apply)(UserForm.unapply)
  )
}