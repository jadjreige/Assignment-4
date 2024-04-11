package acmecollege.rest.resource;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.ClubMembership;
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

import static acmecollege.utility.MyConstants.ADMIN_ROLE;
import static acmecollege.utility.MyConstants.USER_ROLE;
import static acmecollege.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static acmecollege.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static acmecollege.utility.MyConstants.CLUB_MEMBERSHIP_RESOURCE_NAME;
@Path(CLUB_MEMBERSHIP_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClubMembershipResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;

    @GET
    public Response getClubMemberships() {
        LOG.debug("Retrieving all Club Memberships...");
        List<ClubMembership> clubMemberships = service.getAll(ClubMembership.class, ClubMembership.FIND_ALL);
        LOG.debug("Club membership found = {}", clubMemberships);
        Response response = Response.ok(clubMemberships).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getClubMembershipById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("Retrieving club membership with id = {}", id);
        ClubMembership clubMembership = service.getClubMembershipById(id);
        Response response = Response.ok(clubMembership).build();
        return response;
    }

    // Please try to understand and test the below methods:
    @RolesAllowed({ADMIN_ROLE})
    @POST
    public Response addClubMembership(ClubMembership newClubMembership) {
        LOG.debug("Adding a new club membership = {}", newClubMembership);
        ClubMembership tempClubMembership = service.persistClubMembership(newClubMembership);
        return Response.ok(newClubMembership).build();
    }

    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @PUT
    @Path(CLUB_MEMBERSHIP_RESOURCE_NAME)
    public Response updateClubMembership(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, ClubMembership updatingClubMembership) {
        LOG.debug("Updating a specific club membership with id = {}", id);
        Response response = null;
        ClubMembership updatedClubMembership = service.updateClubMembership(id, updatingClubMembership);
        response = Response.ok(updatedClubMembership).build();
        return response;
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteClubMembership(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Response response = null;
        ClubMembership clubMembership = service.deleteClubMembership(id);
        response = Response.ok(clubMembership).build();
        return response;
    }
}
