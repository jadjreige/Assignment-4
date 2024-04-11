/********************************************************************************************************2*4*w*
 * File:  ACMEColegeService.java
 * Course materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 * 
 * Updated by:  Group NN
 *   studentId, firstName, lastName (as from ACSIS)
 *   studentId, firstName, lastName (as from ACSIS)
 *   studentId, firstName, lastName (as from ACSIS)
 *   studentId, firstName, lastName (as from ACSIS)
 *
 */
package acmecollege.ejb;

import static acmecollege.entity.StudentClub.ALL_STUDENT_CLUBS_QUERY_NAME;
import static acmecollege.entity.StudentClub.SPECIFIC_STUDENT_CLUB_QUERY_NAME;
import static acmecollege.entity.StudentClub.IS_DUPLICATE_QUERY_NAME;
import static acmecollege.entity.Student.ALL_STUDENTS_QUERY_NAME;
import static acmecollege.utility.MyConstants.DEFAULT_KEY_SIZE;
import static acmecollege.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static acmecollege.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static acmecollege.utility.MyConstants.DEFAULT_SALT_SIZE;
import static acmecollege.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmecollege.utility.MyConstants.DEFAULT_USER_PREFIX;
import static acmecollege.utility.MyConstants.PARAM1;
import static acmecollege.utility.MyConstants.PROPERTY_ALGORITHM;
import static acmecollege.utility.MyConstants.PROPERTY_ITERATIONS;
import static acmecollege.utility.MyConstants.PROPERTY_KEY_SIZE;
import static acmecollege.utility.MyConstants.PROPERTY_SALT_SIZE;
import static acmecollege.utility.MyConstants.PU_NAME;
import static acmecollege.utility.MyConstants.USER_ROLE;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;
import javax.transaction.Transactional;

import acmecollege.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMECollegeService
 */
@Singleton
public class ACMECollegeService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Student> getAllStudents() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Student> cq = cb.createQuery(Student.class);
        cq.select(cq.from(Student.class));
        return em.createQuery(cq).getResultList();
    }

    public Student getStudentById(int id) {
        return em.find(Student.class, id);
    }

    @Transactional
    public Student persistStudent(Student newStudent) {
        em.persist(newStudent);
        return newStudent;
    }

    @Transactional
    public void buildUserForNewStudent(Student newStudent) {
        SecurityUser userForNewStudent = new SecurityUser();
        userForNewStudent.setUsername(
            DEFAULT_USER_PREFIX + "_" + newStudent.getFirstName() + "." + newStudent.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewStudent.setPwHash(pwHash);
        userForNewStudent.setStudent(newStudent);
        SecurityRole userRole = /* TODO ACMECS01 - Use NamedQuery on SecurityRole to find USER_ROLE */
                em.createNamedQuery(SecurityRole.ROLE_BY_NAME_QUERY, SecurityRole.class).setParameter(PARAM1, USER_ROLE).getSingleResult();
        userForNewStudent.getRoles().add(userRole);
        userRole.getUsers().add(userForNewStudent);
        em.persist(userForNewStudent);
    }

    @Transactional
    public PeerTutor setPeerTutorForStudentCourse(int studentId, int courseId, PeerTutor newPeerTutor) {
        Student studentToBeUpdated = em.find(Student.class, studentId);
        if (studentToBeUpdated != null) { // Student exists
            Set<PeerTutorRegistration> peerTutorRegistrations = studentToBeUpdated.getPeerTutorRegistrations();
            peerTutorRegistrations.forEach(pt -> {
                if (pt.getCourse().getId() == courseId) {
                    if (pt.getPeerTutor() != null) { // PeerTutor exists
                        PeerTutor peer = em.find(PeerTutor.class, pt.getPeerTutor().getId());
                        peer.setPeerTutor(newPeerTutor.getFirstName(),
                        				  newPeerTutor.getLastName(),
                        				  newPeerTutor.getProgram());
                        em.merge(peer);
                    }
                    else { // PeerTutor does not exist
                        pt.setPeerTutor(newPeerTutor);
                        em.merge(studentToBeUpdated);
                    }
                }
            });
            return newPeerTutor;
        }
        else return null;  // Student doesn't exists
    }

    @Transactional
    public PeerTutorRegistration setCourseForStudent(int studentId, Course newCourse) {
        Student studentToBeUpdated = em.find(Student.class, studentId);
        if (studentToBeUpdated != null) { // Student exists
            Course course = em.find(Course.class, newCourse.getId());
            if (course != null) {
                PeerTutorRegistration ptr = new PeerTutorRegistration();
                ptr.setStudent(studentToBeUpdated);
                ptr.setCourse(course);
                em.persist(ptr);
                Set<PeerTutorRegistration> ptrs = studentToBeUpdated.getPeerTutorRegistrations(); 
                ptrs.add(ptr);
                em.merge(studentToBeUpdated);
                studentToBeUpdated.setPeerTutorRegistrations(ptrs);
                em.merge(course);
                return ptr;
            } else return null; // Course doesn't exist
        }
        else return null;  // Student doesn't exists
    }

    /**
     * To update a student
     * 
     * @param id - id of entity to update
     * @param studentWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Student updateStudentById(int id, Student studentWithUpdates) {
        Student studentToBeUpdated = getStudentById(id);
        if (studentToBeUpdated != null) {
            em.refresh(studentToBeUpdated);
            em.merge(studentWithUpdates);
            em.flush();
        }
        return studentToBeUpdated;
    }

    /**
     * To delete a student by id
     * 
     * @param id - student id to delete
     */
    @Transactional
    public Student deleteStudentById(int id) {
        Student student = getStudentById(id);
        if (student != null) {
            em.refresh(student);
            TypedQuery<SecurityUser> findUser = 
                /* TODO ACMECS02 - Use NamedQuery on SecurityRole to find this related Student
                   so that when we remove it, the relationship from SECURITY_USER table
                   is not dangling
                */
                em.createNamedQuery(SecurityUser.SECURITY_USER_BY_NAME_QUERY, SecurityUser.class)
                        .setParameter(PARAM1, student.getId());
            SecurityUser sUser = findUser.getSingleResult();
            em.remove(sUser);
            em.remove(student);
            return student;
        }
        return null;
    }
    
    public List<StudentClub> getAllStudentClubs() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<StudentClub> cq = cb.createQuery(StudentClub.class);
        cq.select(cq.from(StudentClub.class));
        return em.createQuery(cq).getResultList();
    }

    // Why not use the build-in em.find?  The named query SPECIFIC_STUDENT_CLUB_QUERY_NAME
    // includes JOIN FETCH that we cannot add to the above API
    public StudentClub getStudentClubById(int id) {
        TypedQuery<StudentClub> specificStudentClubQuery = em.createNamedQuery(SPECIFIC_STUDENT_CLUB_QUERY_NAME, StudentClub.class);
        specificStudentClubQuery.setParameter(PARAM1, id);
        return specificStudentClubQuery.getSingleResult();
    }
    
    // These methods are more generic.

    public <T> List<T> getAll(Class<T> entity, String namedQuery) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        return allQuery.getResultList();
    }
    
    public <T> T getById(Class<T> entity, String namedQuery, int id) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        allQuery.setParameter(PARAM1, id);
        return allQuery.getSingleResult();
    }

    @Transactional
    public StudentClub deleteStudentClub(int id) {
        //StudentClub sc = getStudentClubById(id);
    	StudentClub sc = getById(StudentClub.class, StudentClub.SPECIFIC_STUDENT_CLUB_QUERY_NAME, id);
        if (sc != null) {
            Set<ClubMembership> memberships = sc.getClubMemberships();
            List<ClubMembership> list = new LinkedList<>();
            memberships.forEach(list::add);
            list.forEach(m -> {
                if (m.getCard() != null) {
                    MembershipCard mc = getById(MembershipCard.class, MembershipCard.ID_CARD_QUERY_NAME, m.getCard().getId());
                    mc.setClubMembership(null);
                }
                m.setCard(null);
                em.merge(m);
            });
            em.remove(sc);
            return sc;
        }
        return null;
    }
    
    // Please study & use the methods below in your test suites
    
    public boolean isDuplicated(StudentClub newStudentClub) {
        TypedQuery<Long> allStudentClubsQuery = em.createNamedQuery(IS_DUPLICATE_QUERY_NAME, Long.class);
        allStudentClubsQuery.setParameter(PARAM1, newStudentClub.getName());
        return (allStudentClubsQuery.getSingleResult() >= 1);
    }

    @Transactional
    public StudentClub persistStudentClub(StudentClub newStudentClub) {
        em.persist(newStudentClub);
        return newStudentClub;
    }

    @Transactional
    public StudentClub updateStudentClub(int id, StudentClub updatingStudentClub) {
    	StudentClub studentClubToBeUpdated = getStudentClubById(id);
        if (studentClubToBeUpdated != null) {
            em.refresh(studentClubToBeUpdated);
            studentClubToBeUpdated.setName(updatingStudentClub.getName());
            em.merge(studentClubToBeUpdated);
            em.flush();
        }
        return studentClubToBeUpdated;
    }
    
    @Transactional
    public ClubMembership persistClubMembership(ClubMembership newClubMembership) {
        em.persist(newClubMembership);
        return newClubMembership;
    }

    public ClubMembership getClubMembershipById(int cmId) {
        TypedQuery<ClubMembership> allClubMembershipQuery = em.createNamedQuery(ClubMembership.FIND_BY_ID, ClubMembership.class);
        allClubMembershipQuery.setParameter(PARAM1, cmId);
        return allClubMembershipQuery.getSingleResult();
    }

    @Transactional
    public ClubMembership updateClubMembership(int id, ClubMembership clubMembershipWithUpdates) {
    	ClubMembership clubMembershipToBeUpdated = getClubMembershipById(id);
        if (clubMembershipToBeUpdated != null) {
            em.refresh(clubMembershipToBeUpdated);
            em.merge(clubMembershipWithUpdates);
            em.flush();
        }
        return clubMembershipToBeUpdated;
    }

    public List<Course> getAllCourses() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Course> cq = cb.createQuery(Course.class);
        cq.select(cq.from(Course.class));
        return em.createQuery(cq).getResultList();
    }

    public Course getCourseById(int id) { return em.find(Course.class, id); }

    @Transactional
    public Course persistCourse(Course newCourse) {
        em.persist(newCourse);
        return newCourse;
    }

    @Transactional
    public Course updateCourseById(int id, Course courseWithUpdates) {
        Course courseToBeUpdated = getCourseById(id);
        if (courseToBeUpdated != null) {
            em.refresh(courseToBeUpdated);
            em.merge(courseWithUpdates);
            em.flush();
        }
        return courseToBeUpdated;
    }

    @Transactional
    public Course deleteCourse(int id) {
        Course course = getCourseById(id);
        if (course != null) {
            Set<PeerTutorRegistration> ptrs = course.getPeerTutorRegistrations();
            List<PeerTutorRegistration> list = new LinkedList<>();
            ptrs.forEach(list::add);
            list.forEach(ptr -> {
                if (ptr.getPeerTutor() != null) {
                    PeerTutor pt = getPeerTutorById(ptr.getPeerTutor().getId());
                    Set<PeerTutorRegistration> peerTutorPtrs = pt.getPeerTutorRegistrations();
                    peerTutorPtrs.remove(ptr);
                }

                if (ptr.getStudent() != null) {
                    Student student = getStudentById(ptr.getStudent().getId());
                    Set<PeerTutorRegistration> studentPtrs = student.getPeerTutorRegistrations();
                    studentPtrs.remove(ptr);
                }
                ptr.setPeerTutor(null);
                ptr.setStudent(null);
                em.merge(ptr);
                em.remove(ptr);
            });
            em.remove(course);
            return course;
        }
        return null;
    }

    public List<PeerTutor> getAllPeerTutors() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PeerTutor> cq = cb.createQuery(PeerTutor.class);
        cq.select(cq.from(PeerTutor.class));
        return em.createQuery(cq).getResultList();
    }

    public PeerTutor getPeerTutorById(int id) { return em.find(PeerTutor.class, id); }

    @Transactional
    public PeerTutor persistPeerTutor(PeerTutor newPeerTutor) {
        em.persist(newPeerTutor);
        return newPeerTutor;
    }
    
    @Transactional
    public PeerTutor updatePeerTutorById(int id, PeerTutor peerTutorWithUpdates) {
        PeerTutor peerTutorToBeUpdated = getPeerTutorById(id);
        if (peerTutorToBeUpdated != null) {
            em.refresh(peerTutorToBeUpdated);
            em.merge(peerTutorWithUpdates);
            em.flush();
        }
        return peerTutorToBeUpdated;
    }
    
    @Transactional
    public PeerTutor deletePeerTutor(int id) {
        PeerTutor peerTutor = getPeerTutorById(id);
        if (peerTutor != null) {
            Set<PeerTutorRegistration> ptrs = peerTutor.getPeerTutorRegistrations();
            List<PeerTutorRegistration> list = new LinkedList<>();
            ptrs.forEach(list::add);
            list.forEach(ptr -> {
                if (ptr.getPeerTutor() != null) {
                    PeerTutor pt = getPeerTutorById(ptr.getPeerTutor().getId());
                    Set<PeerTutorRegistration> peerTutorPtrs = pt.getPeerTutorRegistrations();
                    peerTutorPtrs.remove(ptr);
                }
                ptr.setPeerTutor(null);
                ptr.setStudent(null);
                em.merge(ptr);
            });
            em.remove(peerTutor);
            return peerTutor;
        }
        return null;
    }

    public List<PeerTutorRegistration> getPeerTutorRegistrations() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PeerTutorRegistration> cq = cb.createQuery(PeerTutorRegistration.class);
        cq.select(cq.from(PeerTutorRegistration.class));
        return em.createQuery(cq).getResultList();
    }

    public PeerTutorRegistration getPeerTutorRegistrationById(int id) {
        TypedQuery<PeerTutorRegistration> allPtrQuery = em.createNamedQuery("PeerTutorRegistration.findById", PeerTutorRegistration.class);
        allPtrQuery.setParameter(PARAM1, id);
        return allPtrQuery.getSingleResult();
    }

    @Transactional
    public PeerTutorRegistration persistPeerTutorRegistration(PeerTutorRegistration newPeerTutorRegistration) {
        em.persist(newPeerTutorRegistration);
        return newPeerTutorRegistration;
    }

    @Transactional
    public PeerTutorRegistration updatePeerTutorRegistration(int id, PeerTutorRegistration peerTutorRegistrationWithUpdates) {
        PeerTutorRegistration peerTutorRegistrationToBeUpdated = getPeerTutorRegistrationById(id);
        if (peerTutorRegistrationToBeUpdated != null) {
            em.refresh(peerTutorRegistrationToBeUpdated);
            em.merge(peerTutorRegistrationWithUpdates);
            em.flush();
        }
        return peerTutorRegistrationToBeUpdated;
    }

    @Transactional
    public PeerTutorRegistration deletePeerTutorRegistration(int id) {
        PeerTutorRegistration ptr = getPeerTutorRegistrationById(id);
        if (ptr != null) {
            ptr.setPeerTutor(null);
            ptr.setStudent(null);
            em.merge(ptr);
            em.remove(ptr);
            return ptr;
        }
        ;
        return null;
    }
}

