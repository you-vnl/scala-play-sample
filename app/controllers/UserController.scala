package controllers

import javax.inject.Inject
import models.UserRepository
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import scalikejdbc._
import UserController._
import play.api.libs.functional.syntax._

/**
 * ユーザのコントローラを提供します。
 *
 * @param components コントローラに対するコンポーネント
 */
class UserController @Inject()(components: ControllerComponents) extends AbstractController(components) {

  val u = UserRepository.syntax("u")

  /**
   * ユーザ一覧をDBから取得して返します。
   */
  def list: Action[AnyContent] = Action { implicit request =>
    DB.readOnly { implicit session =>
      // ユーザのリストを取得
      val users: Seq[UserRepository] = withSQL {
        select.from(UserRepository as u).orderBy(u.id.asc)
      }.map(UserRepository(u.resultName)).list.apply()
      // ユーザの一覧をJSONで返す ケースクラスに対応したWritesが定義されていないとコンパイルエラーになる
      Ok(Json.obj("users" -> users))
    }
  }

  /**
   * ユーザ登録をリクエストパラメータから行います。
   * validateメソッドでJSONをケースクラスに変換でき、変換に失敗した場合の処理をrecoverTotalメソッドで行います。
   * OKの場合はユーザを登録し、NGの場合はバリデーションエラーを返します。
   *
   */
  def create: Action[JsValue] = Action(parse.json) { implicit request =>
    request.body.validate[UserForm].map { form =>
      DB.localTx { implicit session =>
        UserRepository.create(form.name, form.companyId)
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { e =>
      BadRequest(Json.obj(
        "result" -> "failure",
        "error" -> JsError.toJson(e)))
    }
  }

  /**
   * ユーザ更新を行います。
   */
  def update: Action[JsValue] = Action(parse.json) { implicit request =>
    request.body.validate[UserForm].map { form =>
      DB.localTx {implicit session =>
        UserRepository.find(form.id.get).foreach { user =>
          UserRepository.save(user.copy(name = form.name, companyId = form.companyId))
        }
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { e =>
      BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
    }
  }

  /**
   * ユーザ削除を行います。
   */
  def remove(id: Long): Action[AnyContent] = Action { implicit request =>
    DB.localTx { implicit session =>
      UserRepository.find(id).foreach { user =>
        UserRepository.destroy(user)
      }
      Ok(Json.obj("result" -> "success"))
    }
  }

}

/**
 * UserControllerのコンパニオンオブジェクトを定義します。
 */
object UserController {
  implicit val usersWritesFormat: Writes[UserRepository] = (user: UserRepository) => {
    Json.obj(
      "id" -> user.id,
      "name" -> user.name,
      "companyId" -> user.companyId
    )
  }

  // ユーザ情報を受取るためのケースクラス
  case class UserForm(id: Option[Long], name: String, companyId: Option[Int])

  /**
   * JSONをUserFormに変換するためのReadsを定義
   *
   * play.api.libs.json パッケージは、JsPath の別名として ダブルアンダースコア を定義している
   * 以下のようにマクロを使ってシンプルに記述することも可能
   * e.g. implicit val userFormReads  = Json.reads[UserForm]
   * リーダとライタが必要な場合は下記
   * e.g. implicit val userFormFormat = Json.format[UserForm]
   *
   */
  implicit val userFormReads: Reads[UserForm] = (
    (__ \ "id").readNullable[Long] and
      (__ \ "name").read[String] and
      (__ \ "companyId").readNullable[Int]
    ) (UserForm)
}
