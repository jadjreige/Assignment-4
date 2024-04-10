package acmecollege.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2024-04-09T21:11:34.648-0400")
@StaticMetamodel(PeerTutor.class)
public class PeerTutor_ extends PojoBase_ {
	public static volatile SingularAttribute<PeerTutor, String> firstName;
	public static volatile SingularAttribute<PeerTutor, String> lastName;
	public static volatile SingularAttribute<PeerTutor, String> program;
	public static volatile SetAttribute<PeerTutor, PeerTutorRegistration> peerTutorRegistrations;
}
