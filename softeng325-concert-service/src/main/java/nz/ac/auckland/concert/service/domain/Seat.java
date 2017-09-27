package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Embeddable
public class Seat {

	@Enumerated(EnumType.STRING)
	@Column(name = "ROW")
	private SeatRow _row;

	@Convert(converter = SeatNumberConverter.class)
	@Column(name = "NUMBER")
	private SeatNumber _number;

	public Seat() {
	}

	public Seat(SeatRow row, SeatNumber number) {
		_row = row;
		_number = number;
	}

	public SeatRow getRow() {
		return _row;
	}

	public SeatNumber getNumber() {
		return _number;
	}
}
