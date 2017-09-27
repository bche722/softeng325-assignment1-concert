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
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.Table;

import nz.ac.auckland.concert.common.types.PriceBand;

@Entity
@Table(name = "CONCERTS")
public class Concert {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "CONCERT_ID")
	private Long _id;

	@Column(name = "TITLE")
	private String _title;

	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name = "CONCERT_PERFORMER", joinColumns = @JoinColumn(name = "CONCERT_ID"), inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID"))
	private Set<Performer> _performers = new HashSet<Performer>();

	@ElementCollection
	@CollectionTable(name = "CONCERT_TARIFS")
	@MapKeyColumn(name = "PRICEBAND")
	@MapKeyEnumerated(EnumType.STRING)
	@Column(name = "PRICE")
	private Map<PriceBand, BigDecimal> _tariff;

	@ElementCollection
	@CollectionTable(name = "CONCERT_DATES", joinColumns = @JoinColumn(name = "CONCERT_ID"))
	@Column(name = "DATE")
	private Set<LocalDateTime> _dates;

	public Concert() {
	}

	public Concert(Long id, String title, Set<LocalDateTime> dates, Map<PriceBand, BigDecimal> ticketPrices,
			Set<Performer> performers) {
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

	public Set<Long> getPerformerIds() {
		Set<Long> performerIds = new HashSet<Long>();
		for (Performer p : _performers) {
			performerIds.add(p.getId());
		}
		return performerIds;
	}

	public Map<PriceBand, BigDecimal> getTariff() {
		return _tariff;
	}
}