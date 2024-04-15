/********************************************************************************************************2*4*w*
 * File:  TestACMECollegeSystem.java
 * Course materials CST 8277
 * Teddy Yap
 * (Original Author) Mike Norman
 *
 *
 * (Modified) @author Student Name
 */
package acmecollege;

import static acmecollege.utility.MyConstants.APPLICATION_API_VERSION;
import static acmecollege.utility.MyConstants.APPLICATION_CONTEXT_ROOT;
import static acmecollege.utility.MyConstants.DEFAULT_ADMIN_USER;
import static acmecollege.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static acmecollege.utility.MyConstants.DEFAULT_USER;
import static acmecollege.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmecollege.utility.MyConstants.STUDENT_RESOURCE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import acmecollege.entity.Course;
import acmecollege.entity.PeerTutor;
import acmecollege.entity.Student;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMECollegeSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    // Test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient(
            new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
    }

    @Test
    @Order(1)
    public void test01_all_students_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Student> students = response.readEntity(new GenericType<List<Student>>(){});
        assertThat(students, is(not(empty())));
        assertThat(students, hasSize(1));
    }
    @Test
    @Order(2)
    public void test02_all_courses_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(3)
    public void test03_all_courses_with_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403)); 
    }

  @Test
    @Order(4)
    public void test04_course_id_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path("course/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(5)
    public void test05_course_id_user() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path("course/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(403)); 
    }
    
    @Test
    @Order(6)
    public void test06_add_course_admin() throws JsonMappingException, JsonProcessingException {
        Course course = new Course();
        course.setCourse("CALC", "Vectors", 2024, "WINTER", 2, (byte) 0);
        
        Response response = webTarget
            .register(adminAuth)
            .path("course")
            .request()
            .post(Entity.json(course));
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(7)
    public void test07_add_course_user() throws JsonMappingException, JsonProcessingException {
        Course course = new Course();
        course.setCourse("CALC", "Vectors", 2024, "WINTER", 2, (byte) 0);
        
        Response response = webTarget
            .register(userAuth)
            .path("course")
            .request()
            .post(Entity.json(course));
        assertThat(response.getStatus(), is(403)); 
    }
    
    @Test
    @Order(8)
    public void test08_delete_course_user() throws JsonMappingException, JsonProcessingException {
        
        Response response = webTarget
            .register(userAuth)
            .path("course/1")
            .request()
            .delete();
        assertThat(response.getStatus(), is(403)); 
    }
    
    @Test
    @Order(9)
    public void test09_delete_course_admin() throws JsonMappingException, JsonProcessingException {
        
        Response response = webTarget
            .register(adminAuth)
            .path("course/3")
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
    }
    @Test
    @Order(10)
    public void test10_all_PeerTutors_with_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path("peertutor")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(11)
    public void test11_all_PeerTutors_with_user() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path("peertutor")
            .request()
            .get();
        assertThat(response.getStatus(), is(403));
    }
    
    @Test
    @Order(12)
    public void test12_PeerTutor_id_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path("peertutor/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(13)
    public void test13_PeerTutor_id_user() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path("peertutor/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(403)); 
    }
    
    @Test
    @Order(14)
    public void test14_add_PeerTutor_user() throws JsonMappingException, JsonProcessingException {
        PeerTutor peerTutor = new PeerTutor();
        peerTutor.setFirstName("Marwan");
        peerTutor.setLastName("Badr");
        peerTutor.setProgram("ECON");
        
        Response response = webTarget
            .register(userAuth)
            .path("peertutor")
            .request()
            .post(Entity.json(peerTutor));
        assertThat(response.getStatus(), is(403)); 
    }
    
    @Test
    @Order(15)
    public void test15_add_PeerTutor_admin() throws JsonMappingException, JsonProcessingException {
        PeerTutor peerTutor = new PeerTutor();
        peerTutor.setFirstName("Noah");
        peerTutor.setLastName("King");
        peerTutor.setProgram("BIO");
        
        Response response = webTarget
            .register(adminAuth)
            .path("peertutor")
            .request()
            .post(Entity.json(peerTutor));
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(16)
    public void test16_delete_PeerTutor_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path("peertutor/2")
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(17)
    public void test17_delete_PeerTutor_user() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path("peertutor/1")
            .request()
            .delete();
        assertThat(response.getStatus(), is(403)); 
    }
}