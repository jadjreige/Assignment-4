/********************************************************************************************************2*4*w*

 * Created by:  Group 45
 * @author Noah King
 * @author Jad Jreige
 * @author Marwan Badr
 * @author Jesse Kong
 *   
 */

package acmecollege.rest.resource;

import static acmecollege.utility.MyConstants.ADMIN_ROLE;
import static acmecollege.utility.MyConstants.PEER_TUTOR_SUBRESOURCE_NAME;
import static acmecollege.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.PeerTutor;
import static acmecollege.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static acmecollege.utility.MyConstants.RESOURCE_PATH_ID_PATH;

@Path(PEER_TUTOR_SUBRESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PeerTutorResource {
	
	private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getPeerTutors() {
    	LOG.debug("retrieving all peer tutors ...");
    	List<PeerTutor> peerTutors = service.getAllPeerTutors();
    	return Response.ok(peerTutors).build();
    }
    
    @GET
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getPeerTutorById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
    	PeerTutor peerTutor = service.getPeerTutorById(id);
    	return Response.status(peerTutor == null ? Status.NOT_FOUND : Status.OK).entity(peerTutor).build();
    }
    
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addPeerTutor(PeerTutor newPeerTutor) {
    	PeerTutor newPeerTutorWithIdTimestamps = service.persistPeerTutor(newPeerTutor);
    	return Response.ok(newPeerTutorWithIdTimestamps).build();
    }
    
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updatePeerTutor(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, PeerTutor updatingPeerTutor) {
    	PeerTutor updatedPeerTutor = service.updatePeerTutorById(id, updatingPeerTutor);
    	return Response.ok(updatedPeerTutor).build();
    }
    
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deletePeerTutor(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
    	PeerTutor peerTutor = service.deletePeerTutor(id);
    	return Response.ok(peerTutor).build();
    }
}
