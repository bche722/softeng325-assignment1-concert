package nz.ac.auckland.concert.client.service;

import java.awt.Image;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nz.ac.auckland.concert.common.Config;
import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;

public class DefaultService implements ConcertService {

	private static String WEB_SERVICE_URI = "http://localhost:10000/services";

	private String _token;
	
	@Override
	public Set<ConcertDTO> getConcerts() throws ServiceException {
		Client client = ClientBuilder.newClient();
		Builder builder = client.target(WEB_SERVICE_URI + "/concerts").request().accept(MediaType.APPLICATION_XML);
		Response response = builder.get();
		if (response.getStatus() == 200) {
			Set<ConcertDTO> concerts = response.readEntity(new GenericType<Set<ConcertDTO>>() {
			});
			response.close();
			client.close();
			return concerts;
		} else {
			response.close();
			client.close();
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public Set<PerformerDTO> getPerformers() throws ServiceException {
		Client client = ClientBuilder.newClient();
		Builder builder = client.target(WEB_SERVICE_URI + "/performers").request().accept(MediaType.APPLICATION_XML);
		Response response = builder.get();
		if (response.getStatus() == 200) {
			Set<PerformerDTO> performers = response.readEntity(new GenericType<Set<PerformerDTO>>() {
			});
			response.close();
			client.close();
			return performers;
		} else {
			response.close();
			client.close();
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public UserDTO createUser(UserDTO newUser) throws ServiceException {
		Client client = ClientBuilder.newClient();
		Builder builder = client.target(WEB_SERVICE_URI + "/users").request().accept(MediaType.APPLICATION_XML);
		Response response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));
		if (response.getStatus() == 201) {
			UserDTO user = response.readEntity(UserDTO.class);
			_token=response.getCookies().get(Config.CLIENT_COOKIE).getValue();
			response.close();
			client.close();
			return user;
		} else if (response.getStatus() == 400) {
			response.close();
			client.close();
			throw new ServiceException(Messages.CREATE_USER_WITH_MISSING_FIELDS);
		} else if (response.getStatus() == 409) {
			response.close();
			client.close();
			throw new ServiceException(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME);
		} else {
			response.close();
			client.close();
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public UserDTO authenticateUser(UserDTO user) throws ServiceException {
		Client client = ClientBuilder.newClient();
		Builder builder = client.target(WEB_SERVICE_URI + "/users").request().accept(MediaType.APPLICATION_XML);
		Response response = builder.put(Entity.entity(user, MediaType.APPLICATION_XML));
		if (response.getStatus() == 200) {
			user = response.readEntity(UserDTO.class);
			_token=response.getCookies().get(Config.CLIENT_COOKIE).getValue();
			response.close();
			client.close();
			return user;
		} else if (response.getStatus() == 400) {
			response.close();
			client.close();
			throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS);
		} else if (response.getStatus() == 403) {
			response.close();
			client.close();
			throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD);
		} else if (response.getStatus() == 404) {
			response.close();
			client.close();
			throw new ServiceException(Messages.AUTHENTICATE_NON_EXISTENT_USER);
		} else {
			response.close();
			client.close();
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {

		Client client = ClientBuilder.newClient();
		Builder builder = client.target(WEB_SERVICE_URI + "/performers/" + performer.getId() + "/image").request()
				.accept(MediaType.APPLICATION_XML);
		Response response = builder.get();
		if (response.getStatus() == 200) {
			String imageName = response.readEntity(String.class);
			response.close();
			client.close();
			//TODO
			return null;
		} else if (response.getStatus() == 403) {
			response.close();
			client.close();
			throw new ServiceException(Messages.NO_IMAGE_FOR_PERFORMER);
		} else {
			response.close();
			client.close();
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
		Client client = ClientBuilder.newClient();
		Builder builder = client.target(WEB_SERVICE_URI + "/reservation").request().accept(MediaType.APPLICATION_XML).cookie(new Cookie(Config.CLIENT_COOKIE, _token));
		Response response = builder.post(Entity.entity(reservationRequest, MediaType.APPLICATION_XML));
		if (response.getStatus() == 201) {
			ReservationDTO reservation = response.readEntity(ReservationDTO.class);
			response.close();
			client.close();
			return reservation;
		} else if (response.getStatus() == 401) {
			response.close();
			client.close();
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		} else if (response.getStatus() == 403) {
			response.close();
			client.close();
			throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
		} else if (response.getStatus() == 400) {
			response.close();
			client.close();
			throw new ServiceException(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS);
		} else if (response.getStatus() == 404) {
			response.close();
			client.close();
			throw new ServiceException(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE);
		} else if (response.getStatus() == 407) {
			response.close();
			client.close();
			throw new ServiceException(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION);
		} else {
			response.close();
			client.close();
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public void confirmReservation(ReservationDTO reservation) throws ServiceException {
		Client client = ClientBuilder.newClient();
		Builder builder = client.target(WEB_SERVICE_URI + "/reservation").request().accept(MediaType.APPLICATION_XML).cookie(new Cookie(Config.CLIENT_COOKIE, _token));
		Response response = builder.put(Entity.entity(reservation, MediaType.APPLICATION_XML));
		if (response.getStatus() == 204) {
			response.close();
			client.close();
		} else if (response.getStatus() == 401) {
			response.close();
			client.close();
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		} else if (response.getStatus() == 403) {
			response.close();
			client.close();
			throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
		} else if (response.getStatus() == 408) {
			response.close();
			client.close();
			throw new ServiceException(Messages.EXPIRED_RESERVATION);
		} else if (response.getStatus() == 400) {
			response.close();
			client.close();
			throw new ServiceException(Messages.CREDIT_CARD_NOT_REGISTERED);
		} else {
			response.close();
			client.close();
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
		Client client = ClientBuilder.newClient();
		Builder builder = client.target(WEB_SERVICE_URI + "/creditcard").request().accept(MediaType.APPLICATION_XML).cookie(new Cookie(Config.CLIENT_COOKIE, _token));
		Response response = builder.post(Entity.entity(creditCard, MediaType.APPLICATION_XML));
		if (response.getStatus() == 204) {
			response.close();
			client.close();
		}else if (response.getStatus() == 401) {
			response.close();
			client.close();
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		} else if (response.getStatus() == 403) {
			response.close();
			client.close();
			throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
		}else {
			response.close();
			client.close();
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public Set<BookingDTO> getBookings() throws ServiceException {
		Client client = ClientBuilder.newClient();
		Builder builder = client.target(WEB_SERVICE_URI + "/bookings").request().accept(MediaType.APPLICATION_XML).cookie(new Cookie(Config.CLIENT_COOKIE, _token));
		Response response = builder.get();
		if (response.getStatus() == 200) {
			Set<BookingDTO> bookings = response.readEntity(new GenericType<Set<BookingDTO>>() {
			});
			response.close();
			client.close();
			return bookings;
		} else if (response.getStatus() == 401) {
			response.close();
			client.close();
			throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
		} else if (response.getStatus() == 403) {
			response.close();
			client.close();
			throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
		}else {
			response.close();
			client.close();
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public void subscribeForNewsItems(NewsItemListener listener) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void cancelSubscription() {
		throw new UnsupportedOperationException();
	}

}
