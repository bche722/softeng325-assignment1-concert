package nz.ac.auckland.concert.service.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import nz.ac.auckland.concert.common.types.Genre;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name="PERFORMERS")
public class Performer {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long _id;

	@Column(name = "NAME")
	private String _name;

	@Column(name = "IMAGENAME")
	private String _imageName;

	@Enumerated
	@Column(name = "GENRE")
	private Genre _genre;

	@ManyToMany(mappedBy= "_performers")
	private Set<Concert> _concerts;

	public Performer() {}

	public Performer(Long id, String name, String imageName, Genre genre, Set<Concert> concerts) {
		_id = id;
		_name = name;
		_imageName = imageName;
		_genre = genre;
		_concerts = new HashSet<Concert>(concerts);
	}

	public Long getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public String getImageName() {
		return _imageName;
	}

	public Set<Concert> getConcertIds() {
		return Collections.unmodifiableSet(_concerts);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Performer))
			return false;
		if (obj == this)
			return true;

		Performer rhs = (Performer) obj;
		return new EqualsBuilder().
				append(_name, rhs._name).
				append(_imageName, rhs._imageName).
				append(_genre, rhs._genre).
				append(_concerts, rhs._concerts).
				isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
				append(_name).
				append(_imageName).
				append(_genre).
				append(_concerts).
				hashCode();
	}
}

