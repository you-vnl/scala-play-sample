package controllers

import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import UserController._
import repository.UserRepository
import play.api.libs.json.JsonConfiguration.Aux

/**
 * ユーザのコントローラを提供します。
 *
 * @param components コントローラに対するコンポーネント
 */
class UserController @Inject()(components: ControllerComponents) extends AbstractController(components) {

  /**
   * ユーザ一覧をDBから取得して返します。
   * ケースクラスに対応したWritesが定義されていないとコンパイルエラーになります。
   */
  def list: Action[AnyContent] = Action { implicit request =>
    Ok(Json.toJson(UserRepository.getUserList).toString())
  }

  /**
   * ユーザ登録をリクエストパラメータから行います。
   * validateメソッドでJSONをケースクラスに変換でき、変換に失敗した場合の処理をrecoverTotalメソッドで行います。
   * OKの場合はユーザを登録し、NGの場合はバリデーションエラーを返します。
   */
  def create: Action[JsValue] = Action(parse.json) { implicit request =>
    request.body.validate[UserForm].map { form =>
      UserRepository.createUser(form.name, form.companyId.get)
      Ok(successJson)
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
      UserRepository.saveUser(id = form.id.get, form.name, form.companyId)
      Ok(successJson)
    }.recoverTotal { e =>
      BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
    }
  }

  /**
   * ユーザ削除を行います。
   */
  def remove(id: Long): Action[AnyContent] = Action { implicit request =>
    UserRepository.removeUser(id)
    Ok(successJson)
  }
}

/**
 * UserControllerのコンパニオンオブジェクトを定義します。
 */
object UserController {

  /**
   * JSON出力をスネークケースで行う設定を行います。
   */
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(JsonNaming.SnakeCase)

  /**
   * JSONをUserFormに変換するためのReadsを定義します。
   */
  implicit val userFormReads: Reads[UserForm] = Json.reads[UserForm]

  /**
   * UserRepositoryをJSONに変換するためのWritesを定義します。
   */
  implicit val userWrites: OWrites[UserRepository] = Json.writes[UserRepository]

  /**
   * ユーザ情報を受取るためのケースクラス
   */
  case class UserForm(id: Option[Long], name: String, companyId: Option[Int])

  /**
   * 処理成功時に返すJSONオブジェクトを定義します。
   */
  val successJson: JsObject = Json.obj("result" -> "success")
}
