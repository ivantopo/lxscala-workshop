package controllers

import components.KeyStorage
import javax.inject._
import kamon.Kamon
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
 * Handles keys for an organization
 */
@Singleton
class OrganizationController @Inject()(keyStorage: KeyStorage, cc: ControllerComponents, ec: ExecutionContext) extends AbstractController(cc) {
  implicit val iec = ec

  /**
    * Retrieves all keys for an organization
    */
  def keys(organizationID: Long) = Action.async { implicit request: Request[AnyContent] =>
    Kamon.currentSpan().name("GET /organizations/:id/keys")

    spiceUp {
      keyStorage.list(organizationID)
        .map(keys => Ok(keyListToJson(organizationID, keys)))
    }
  }

  /**
    * Create a new key for an organization
    */
  def create(organizationID: Long) = Action.async { implicit request: Request[AnyContent] =>
    Kamon.currentSpan()
      .name("POST /organizations/:id/keys")
      .tag("organizationID", organizationID)

    keyStorage.create(organizationID)
      .map(key => Ok(keyToJson(key)))
  }

  /**
    * Revoke an API key if it exists
    */
  def revoke(organizationID: Long, key: String) = Action.async { implicit request: Request[AnyContent] =>
    Kamon.currentSpan()
      .name("DELETE /organizations/:id/keys")
      .tag("bouncer.organization_id", organizationID)
      .tag("bouncer.revoked_key", key)

    keyStorage.revoke(organizationID, key).map(revoked => {
      if(revoked)
        Ok
      else
        NotFound
    })
  }
}
