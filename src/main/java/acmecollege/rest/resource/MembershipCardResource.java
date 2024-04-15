/********************************************************************************************************2*4*w*

 * Created by:  Group 45
 * @author Noah King
 * @author Jad Jreige
 * @author Marwan Badr
 * @author Jesse Kong
 *   
 */

package acmecollege.rest.resource;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.ClubMembership;
import acmecollege.entity.MembershipCard;
import acmecollege.entity.StudentClub;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static acmecollege.utility.MyConstants.*;

@Path(MEMBERSHIP_CARD_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MembershipCardResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;

    @GET
    public Response getMembershipCards() {
        LOG.debug("Retrieving all membership cards...");
        List<MembershipCard> membershipCards = service.getAll(MembershipCard.class, MembershipCard.ALL_CARDS_QUERY_NAME);
        LOG.debug("Club membership found = {}", membershipCards);
        Response response = Response.ok(membershipCards).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getMembershipCardById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("Retrieving membership card with id = {}", id);
        MembershipCard membershipCard = service.getById(MembershipCard.class, MembershipCard.ID_CARD_QUERY_NAME, id);
        Response response = Response.ok(membershipCard).build();
        return response;
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMembershipCard(MembershipCard card) {
        Response response = null;
        MembershipCard membershipCard = service.addMembershipCard(card);
        response = Response.ok(membershipCard).build();
        return response;
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteMembershipCard(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Response response = null;
        MembershipCard membershipCard = service.deleteMembershipCard(id);
        response = Response.ok(membershipCard).build();
        return response;
    }
}
