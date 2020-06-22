package controllers

import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import UserController._
import repository.UserRepository
import play.api.libs.json.JsonConfiguration.Aux

/**
 * ユーザのコントローラを定義します。
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
      BadRequest(createErrorJson(JsError.toJson(e)))
    }
  }

  /**
   * ユーザ更新を行います。
   * JSONボディパーサーを利用すると、request.body の値が直接JsValueとして扱えるようになります。
   * validateで暗黙の Reads[(String, Long)] に従って、入力された JSON のバリデーションと変換を明示的に行います。
   */
  def update: Action[JsValue] = Action(parse.json) { implicit request =>
    request.body.validate[UserForm].map { form =>
      UserRepository.saveUser(id = form.id.get, form.name, form.companyId) match { //要バリデーション
        case Some(_) => Ok(successJson)
        case None => BadRequest(createErrorJson(Json.obj("error" -> "ID not Found")))
      }
    }.recoverTotal { e =>
      BadRequest(createErrorJson(JsError.toJson(e)))
    }
  }

  /**
   * ユーザ削除を行います。
   */
  def remove(id: Long): Action[AnyContent] = Action { implicit request =>
    UserRepository.removeUser(id) match {
      case Some(_) => Ok(successJson)
      case None => BadRequest(createErrorJson(Json.obj("error" -> "ID not Found")))
    }
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

  /**
   * エラー時の結果クラスを定義します。
   *
   * @param result 結果
   * @param error  エラーオブジェクト
   */
  case class ErrorResult(result: String, error: JsObject)

  /**
   * ErrorResultをJSONに変換するためのWritesを定義します。
   */
  implicit val errorResultWrites: OWrites[ErrorResult] = Json.writes[ErrorResult]

  /**
   * エラー時のJSONオブジェクトを作成します。
   *
   * @param error エラー内容JsObject
   * @return
   */
  def createErrorJson(error: JsObject): JsValue = Json.toJson(ErrorResult("failure", error))

}
