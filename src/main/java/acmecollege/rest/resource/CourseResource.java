package acmecollege.rest.resource;


import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.Course;
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

@Path(COURSE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getCourses() {
        LOG.debug("retrieving all courses ..");
        List<Course> courses = service.getAllCourses();
        Response res = Response.ok(courses).build();
        return res;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getCourseById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("try to retrieve specific course " + id);
        Course course = service.getCourseById(id);
        Response response = Response.status(course == null ? Response.Status.NOT_FOUND : Response.Status.OK).entity(course).build();
        return response;
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addCourse(Course newCourse) {
        Course newCourseWithIdTimestamps = service.persistCourse(newCourse);
        return Response.ok(newCourseWithIdTimestamps).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateCourse(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Course updatingCourse) {
        LOG.debug("Updating a specific course with id = {}", id);
        Course updatedCourse = service.updateCourseById(id, updatingCourse);
        return Response.ok(updatedCourse).build();
    }
    
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteCourse(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
    	Course course = service.deleteCourse(id);
    	return Response.ok(course).build();
    }
}
