/********************************************************************************************************2*4*w*
 * File:  NonAcademicStudentClub.java Course materials CST 8277
 * 
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 * Updated by:  Group 45
 * (Modified) @author Noah King
 * (Modified) @author Jad Jreige
 * (Modified) @author Marwan Badr
 * (Modified) @author Jesse Kong
 * 
 */
package acmecollege.entity;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

//TODO NASC01 - Add missing annotations, please see Week 9 slides page 15.  Value 1 is academic and value 0 is non-academic.
@Entity
@DiscriminatorValue("0")
public class NonAcademicStudentClub extends StudentClub implements Serializable {
	private static final long serialVersionUID = 1L;

	public NonAcademicStudentClub() {
		super(false);

	}
}