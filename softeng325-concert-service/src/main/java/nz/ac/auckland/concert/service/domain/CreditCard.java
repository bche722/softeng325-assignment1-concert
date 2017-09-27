package nz.ac.auckland.concert.service.domain;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="CREDITCARDS")
public class CreditCard {
	
	public enum Type {Visa, Master};

	@Id
	@Column(name = "CARD_NUMBER")
	private String _number;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE")
	private Type _type;
	
	@Column(name = "NAME")
	private String _name;
	
	@Column(name = "EXPIRYDATE")
	private LocalDate _expiryDate;
	
	public CreditCard() {}
	
	public CreditCard(Type type, String name, String number, LocalDate expiryDate) {
		_type = type;
		_name = name;
		_number = number;
		_expiryDate = expiryDate;
	}
	
	public Type getType() {
		return _type;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getNumber() {
		return _number;
	}

	public LocalDate getExpiryDate() {
		return _expiryDate;
	}
}
