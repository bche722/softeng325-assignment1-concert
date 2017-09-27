package nz.ac.auckland.concert.service.services;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import nz.ac.auckland.concert.common.Config;
import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.CreditCard;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.Reservation;
import nz.ac.auckland.concert.service.domain.Reservation.Status;
import nz.ac.auckland.concert.service.domain.User;
import nz.ac.auckland.concert.service.domain.jpa.Mapper;
import nz.ac.auckland.concert.service.util.TheatreUtility;

@Path("/")
public class ConcertResource {

	private EntityManager entityManager = PersistenceManager.instance().createEntityManager();

	private static Map<String, User> _tokens = new HashMap<String, User>();
	
	private static AtomicLong _idCounter = new AtomicLong();

	@GET
	@Path("concerts")
	@Produces({ MediaType.APPLICATION_XML })
	public Response retrieveConcerts() {
		entityManager.getTransaction().begin();
		TypedQuery<Concert> query = entityManager.createQuery("from Concert c", Concert.class);
		List<Concert> result = query.getResultList();
		Set<Concert> concerts = new HashSet<Concert>(result);
		GenericEntity<Set<ConcertDTO>> entity = new GenericEntity<Set<ConcertDTO>>(Mapper.toConcertDTO(concerts)) {
		};
		return Response.ok(entity).build();
	}

	@GET
	@Path("performers")
	@Produces({ MediaType.APPLICATION_XML })
	public Response retrievePerformers() {
		entityManager.getTransaction().begin();
		TypedQuery<Performer> query = entityManager.createQuery("from Performer p", Performer.class);
		List<Performer> result = query.getResultList();
		Set<Performer> performers = new HashSet<Performer>(result);
		GenericEntity<Set<PerformerDTO>> entity = new GenericEntity<Set<PerformerDTO>>(
				Mapper.toPerformerDTO(performers)) {
		};
		return Response.ok(entity).build();
	}

	@GET
	@Path("performers/{id}/image")
	@Produces({ MediaType.APPLICATION_XML })
	public Response getImageForPerformer(@PathParam("id") Long id) {
		entityManager.getTransaction().begin();
		Performer performer = entityManager.find(Performer.class, id);
		if (performer.getImageName() == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		return Response.ok(performer.getImageName()).build();
	}

	@POST
	@Path("users")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	public Response createUser(UserDTO newUser) {

		if (newUser.getFirstname() == null || newUser.getLastname() == null || newUser.getPassword() == null
				|| newUser.getUsername() == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		User u = entityManager.find(User.class, newUser.getUsername());
		if (u != null) {
			throw new WebApplicationException(Response.Status.CONFLICT);
		}

		User user = new User(newUser.getUsername(), newUser.getPassword(), newUser.getLastname(),
				newUser.getFirstname());
		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.getTransaction().commit();

		return Response.created(URI.create("/concerts/" + user.getUsername())).entity(Mapper.toUserDTO(user))
				.cookie(makeCookie(user)).build();
	}

	@PUT
	@Path("users")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	public Response authenticateUser(UserDTO newUser) {

		if (newUser.getPassword() == null || newUser.getUsername() == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		User user = entityManager.find(User.class, newUser.getUsername());
		if (user == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		if (!user.getPassword().equals(newUser.getPassword())) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}

		return Response.ok(Mapper.toUserDTO(user)).cookie(makeCookie(user)).build();
	}

	@POST
	@Path("reservation")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	public Response reserveSeats(ReservationRequestDTO reservationRequest,
			@CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {
		User user=null;
		if (cookie.getValue().equals("")) {
			throw new WebApplicationException(Response.Status.UNAUTHORIZED);
		}
		if (!checkToken(cookie)) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}else {
			user=entityManager.find(User.class, _tokens.get(cookie.getValue()).getUsername());
		}
		if (reservationRequest.getConcertId() == null || reservationRequest.getDate() == null
				|| reservationRequest.getSeatType() == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		Concert concert = entityManager.find(Concert.class, reservationRequest.getConcertId());
		
		LocalDateTime date = reservationRequest.getDate();
		if (!concert.getDates().contains(date)) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		TypedQuery<Reservation> query = entityManager
				.createQuery("select r from Reservation r where r._concert=:concert and r._date=:date",
						Reservation.class)
				.setParameter("concert", concert).setParameter("date", date);
		List<Reservation> result = query.getResultList();
		Set<SeatDTO> bookedSeats = new HashSet<SeatDTO>();
		for (Reservation r : result) {
			bookedSeats.addAll(Mapper.toSeatDTO(r.getSeats()));
		}
		Set<SeatDTO> availableSeats = TheatreUtility.findAvailableSeats(reservationRequest.getNumberOfSeats(),
				reservationRequest.getSeatType(), bookedSeats);
		if(availableSeats.size()==0) {
			throw new WebApplicationException(Response.Status.PROXY_AUTHENTICATION_REQUIRED);
		}
		
		Reservation reservation=new Reservation(Long.valueOf(_idCounter.incrementAndGet()), concert,user, date, Mapper.toSeat(availableSeats),reservationRequest.getSeatType()); 

		entityManager.getTransaction().begin();
		entityManager.persist(reservation);
		entityManager.getTransaction().commit();
		
		Timer timer=new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				reservation.setStatus(Status.Expired);
				entityManager.getTransaction().begin();
				entityManager.merge(reservation);
				entityManager.getTransaction().commit();
			}}, 5000);
		
		ReservationDTO reservationDTO=new ReservationDTO(reservation.getId(),reservationRequest,availableSeats);
		
		return Response.created(URI.create("/resevation/" + reservation.getId())).entity(reservationDTO).build();
	}

	@PUT
	@Path("reservation")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	public Response confirmReservation(ReservationDTO reservation,@CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {
		if (cookie.getValue().equals("")) {
			throw new WebApplicationException(Response.Status.UNAUTHORIZED);
		}
		if (!checkToken(cookie)) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		Reservation reservationData=entityManager.find(Reservation.class, reservation.getId());
		if(reservationData.getStatus()==Reservation.Status.Expired) {
			throw new WebApplicationException(Response.Status.REQUEST_TIMEOUT);
		}
		
		if(reservationData.getUser().getCreditCard()==null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		reservationData.setStatus(Reservation.Status.Confirmed);
		entityManager.getTransaction().begin();
		entityManager.merge(reservationData);
		entityManager.getTransaction().commit();

		return Response.noContent().build();
	}
	
	@POST
	@Path("creditcard")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	public Response registerCreditCard(CreditCardDTO creditCard, @CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {

		if (cookie.getValue().equals("")) {
			throw new WebApplicationException(Response.Status.UNAUTHORIZED);
		}
		if (!checkToken(cookie)) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}

		User user = _tokens.get(cookie.getValue());
		CreditCard card = new CreditCard(CreditCard.Type.valueOf(creditCard.getType().toString()), creditCard.getName(),
				creditCard.getNumber(), creditCard.getExpiryDate());
		user.setCreditCard(card);
		entityManager.getTransaction().begin();
		entityManager.persist(card);
		entityManager.merge(user);
		entityManager.getTransaction().commit();
		return Response.noContent().build();
	}
	
	@GET
	@Path("bookings")
	@Produces({ MediaType.APPLICATION_XML })
	public Response getBookings(@CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {
		User user=null;
		if (cookie.getValue().equals("")) {
			throw new WebApplicationException(Response.Status.UNAUTHORIZED);
		}
		if (!checkToken(cookie)) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}else {
			user=entityManager.find(User.class, _tokens.get(cookie.getValue()).getUsername());
		}
		
		TypedQuery<Reservation> query = entityManager
				.createQuery("select r from Reservation r where r._user=:user and r._status=:status",
						Reservation.class)
				.setParameter("user", user).setParameter("status", Reservation.Status.Confirmed);
		List<Reservation> result = query.getResultList();
		Set<BookingDTO> bookings=new HashSet<BookingDTO>();
		for(Reservation r:result) {
			BookingDTO booking=new BookingDTO(r.getConcert().getId(),r.getConcert().getTitle(),r.getDate(),Mapper.toSeatDTO(r.getSeats()),r.getPriceBand());
			bookings.add(booking);
		}
		
		GenericEntity<Set<BookingDTO>> entity = new GenericEntity<Set<BookingDTO>>(bookings) {
		};
		return Response.ok(entity).build();
	}

	private boolean checkToken(Cookie cookie) {
		return _tokens.keySet().contains(cookie.getValue());
	}

	private NewCookie makeCookie(User user) {
		String token = UUID.randomUUID().toString();
		NewCookie newCookie = new NewCookie(Config.CLIENT_COOKIE, token);
		_tokens.put(token, user);
		return newCookie;
	}
}
