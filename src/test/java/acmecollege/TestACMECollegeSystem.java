/********************************************************************************************************2*4*w*
 * File:  TestACMECollegeSystem.java
 * Course materials CST 8277
 * Teddy Yap
 * (Original Author) Mike Norman
 *
 *
 * (Modified) @author Noah King
 * (Modified) @author Jad Jreige
 * (Modified) @author Marwan Badr
 * (Modified) @author Jesse Kong
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
import javax.ws.rs.core.MediaType;
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

import acmecollege.entity.AcademicStudentClub;
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
//        assertThat(students, hasSize(1));
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
    
    @Test
    @Order(18)
    public void test18_all_students_with_userrole() throws JsonMappingException, JsonProcessingException {
    	Response response = webTarget
    			.register(userAuth)
    			.path(STUDENT_RESOURCE_NAME)
    			.request()
    			.get();
    	assertThat(response.getStatus(), is(403)); 
    }
    
    @Test
    @Order(19)
    public void test19_student_id_adminrole() throws JsonMappingException, JsonProcessingException {
    	Response response = webTarget
    			.register(adminAuth)
    			.path("student/1")
    			.request()
    			.get();
    	assertThat(response.getStatus(), is(200)); 
    }
    
    @Test
    @Order(20)
    public void test20_student_id_userrole() throws JsonMappingException, JsonProcessingException {
    	Response response = webTarget
    			.register(userAuth)
    			.path("student/1")
    			.request()
    			.get();
    	assertThat(response.getStatus(), is(200)); 
    }
    
    @Test
    @Order(21)
    public void test21_add_student_userrole() throws JsonMappingException, JsonProcessingException {
    	Student student = new Student();
    	
    	Response response = webTarget
    			.register(userAuth)
    			.path("student")
    			.request()
    			.post(Entity.json(student));
    	assertThat(response.getStatus(), is(403)); // Forbidden
    }
    

    @Test
    @Order(22)
    public void test22_add_student_adminrole() throws JsonMappingException, JsonProcessingException {
    	Student student = new Student();
    	student.setFirstName("Jesse");
    	student.setLastName("Kong");
    	
    	Response response = webTarget
    			.register(adminAuth)
    			.path("student")
    			.request()
    			.post(Entity.json(student));
    	assertThat(response.getStatus(), is(200));
    }

    @Test
    @Order(23)
    public void test23_delete_student_id_userrole() throws JsonMappingException, JsonProcessingException {
    	
    	Response response = webTarget
    			.register(userAuth)
    			.path("student/1")
    			.request()
    			.delete();
    	assertThat(response.getStatus(), is(403)); // Forbidden
    }
    
    @Test
    @Order(24)
    public void test24_all_student_clubs_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path("studentclub")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(25)
    public void test25_all_student_clubs_user() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path("studentclub")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(26)
    public void test26_student_club_id_user() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path("studentclub/2")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(27)
    public void test27_student_club_id_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path("studentclub/2")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(28)
    public void test28_student_club_delete_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path("studentclub/2")
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(29)
    public void test29_student_club_delete_user() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path("studentclub/1")
            .request()
            .delete();
        assertThat(response.getStatus(), is(403)); // Forbidden
    }
    
    @Test
    @Order(30)
    public void test30_student_club_create_user() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path("studentclub")
            .request()
            .post(Entity.json(null));
        assertThat(response.getStatus(), is(403)); // Forbidden
    }
    
  @Test
  @Order(31)
  public void test31_student_club_create_admin() throws JsonMappingException, JsonProcessingException {
      AcademicStudentClub academicStudentClub = new AcademicStudentClub();
      academicStudentClub.setName("test club");
  	Response response = webTarget
          .register(adminAuth)
          .path("studentclub")
          .request()
          .post(Entity.json(academicStudentClub));
      assertThat(response.getStatus(), is(200));
  }

  @Test
  @Order(32)
  public void test32_Student_entity_is_json() throws JsonMappingException, JsonProcessingException {
      Response response = webTarget
              .register(adminAuth)
              .path(STUDENT_RESOURCE_NAME)
              .request()
              .get();
      assertThat(response.getMediaType(), is(not(MediaType.APPLICATION_XML)));
  }
  
  @Test
  @Order(33)
  public void test33_StudentClub_entity_is_json() throws JsonMappingException, JsonProcessingException {
      Response response = webTarget
              .register(adminAuth)
              .path("studentclub")
              .request()
              .get();
      assertThat(response.getMediaType(), is(not(MediaType.APPLICATION_XML)));
  }
  
  @Test
  @Order(34)
  public void test34_membershipcard_entity_is_json() throws JsonMappingException, JsonProcessingException {
      Response response = webTarget
              .register(adminAuth)
              .path("memberCard")
              .request()
              .get();
      assertThat(response.getMediaType(), is(not(MediaType.APPLICATION_XML)));
  }
  
  @Test
  @Order(35)
  public void test35_ClubMember_entity_is_json() throws JsonMappingException, JsonProcessingException {
      Response response = webTarget
              .register(adminAuth)
              .path("ClubMember")
              .request()
              .get();
      assertThat(response.getMediaType(), is(not(MediaType.APPLICATION_XML)));
  }
}