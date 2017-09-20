package nz.ac.auckland.concert.service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import nz.ac.auckland.concert.common.types.PriceBand;

@Entity
@Table(name="CONCERTS")
public class Concert {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long _id;
	
	@Column(name = "TITLE")
	private String _title;
	
	@ElementCollection
	@CollectionTable(name = "CONCERT_DATES",joinColumns= @JoinColumn( name = "CONCERT_ID" ) )
	@Column( name = "DATE" )
	private Set<LocalDateTime> _dates;
	
	@ElementCollection
	@CollectionTable(name = "CONCERT_PERFORMER",joinColumns= @JoinColumn( name = "CONCERT_ID" ) )
	@MapKeyColumn( name = "PRICEBAND" )
	@Column( name = "PRICE" )
	private Map<PriceBand, BigDecimal> _tariff;
	
	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name = "CONCERT_PERFORMER",joinColumns= @JoinColumn(name = "CONCERT_ID"),inverseJoinColumns= @JoinColumn(name = "PERFORMER_ID"))
	private Set<Performer> _performers;

	public Concert() {
	}

	public Concert(Long id, String title, Set<LocalDateTime> dates,
			Map<PriceBand, BigDecimal> ticketPrices, Set<Performer> performers) {
		_id = id;
		_title = title;
		_dates = new HashSet<LocalDateTime>(dates);
		_tariff = new HashMap<PriceBand, BigDecimal>(ticketPrices);
		_performers = new HashSet<Performer>(performers);
	}

	public Long getId() {
		return _id;
	}

	public String getTitle() {
		return _title;
	}

	public Set<LocalDateTime> getDates() {
		return Collections.unmodifiableSet(_dates);
	}

	public BigDecimal getTicketPrice(PriceBand seatType) {
		return _tariff.get(seatType);
	}

	public Set<Performer> getPerformerIds() {
		return Collections.unmodifiableSet(_performers);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().
            append(_title, rhs._title).
            append(_dates, rhs._dates).
            append(_tariff, rhs._tariff).
            append(_performers, rhs._performers).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_title).
	            append(_dates).
	            append(_tariff).
	            append(_performers).
	            hashCode();
	}
}













