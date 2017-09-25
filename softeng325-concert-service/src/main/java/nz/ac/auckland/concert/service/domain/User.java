package nz.ac.auckland.concert.service.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name="USERS")
public class User {

	@Id
	@Column(name = "USERNAME")
	private String _username;
	
	@Column(name = "PASSWORD")
	private String _password;
	
	@Column(name = "FIRSTNAME")
	private String _firstname;
	
	@Column(name = "LASTNAME")
	private String _lastname;
	
	protected User() {}
	
	public User(String username, String password, String lastname, String firstname) {
		_username = username;
		_password = password;
		_lastname = lastname;
		_firstname = firstname;
	}
	
	public User(String username, String password) {
		this(username, password, null, null);
	}
	
	public String getUsername() {
		return _username;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public String getFirstname() {
		return _firstname;
	}
	
	public String getLastname() {
		return _lastname;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User))
            return false;
        if (obj == this)
            return true;

        User rhs = (User) obj;
        return new EqualsBuilder().
            append(_username, rhs._username).
            append(_password, rhs._password).
            append(_firstname, rhs._firstname).
            append(_lastname, rhs._lastname).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_username).
	            append(_password).
	            append(_firstname).
	            append(_password).
	            hashCode();
	}
}
