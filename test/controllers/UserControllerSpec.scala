package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._

class UserControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {
  "UserController GET" should {
    "request users list from the application" in {
      val controller = inject[UserController]
      // FakeRequestを使ってコントローラーのメソッドを呼び出す
      val result = controller.list.apply(FakeRequest())

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      val resultJson = contentAsJson(result)
      val expectedJson = Json.parse(
        """{"users":[{"id":1,"name":"Taro Yamada","companyId":1},
          |{"id":2,"name":"Jiro Sato","companyId":null}]}""".stripMargin
      )
      resultJson mustEqual  expectedJson
    }
  }

}
