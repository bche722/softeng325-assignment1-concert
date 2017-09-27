package nz.ac.auckland.concert.service.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import nz.ac.auckland.concert.common.types.PriceBand;

@Entity
@Table(name="RESERVATION")
public class Reservation {
	
	public enum Status {Expired, Waiting,Confirmed};
	
	@Id
	@Column(name = "RESERVATION_ID")
	private Long _id;
	
	@ManyToOne(cascade=CascadeType.PERSIST)
	@JoinColumn(name = "CONCERT_ID",nullable = false)
	private Concert _concert;
	
	@ManyToOne(cascade=CascadeType.PERSIST)
	@JoinColumn(name = "USER_ID",nullable = false)
	private User _user;
	
	@Column( name = "DATE" )
	private LocalDateTime _date;
	
	@ElementCollection
	@CollectionTable(name = "SEAT" )
	private Set<Seat> _seats;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS")
	private Status _status;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "PRICEBAND")
	private PriceBand _priceBand;
	
	public Reservation() {}
	
	public Reservation(Long id, Concert conert,User user, LocalDateTime date,Set<Seat> seats,PriceBand priceBand) {
		_id = id;
		_concert=conert;
		_user=user;
		_date=date;
		_seats = new HashSet<Seat>(seats);
		_status=Status.Waiting;
		_priceBand=priceBand;
	}
	
	public Long getId() {
		return _id;
	}
	
	public Concert getConcert() {
		return _concert;
	}
	
	public User getUser() {
		return _user;
	}
	
	public LocalDateTime getDate() {
		return _date;
	}
	
	public Set<Seat> getSeats() {
		return Collections.unmodifiableSet(_seats);
	}
	
	public Status getStatus() {
		return _status;
	}
	
	public PriceBand getPriceBand() {
		return _priceBand;
	}
	
	public void setStatus(Status status) {
		_status=status;
	}
}
