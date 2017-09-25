package nz.ac.auckland.concert.service.services;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.User;
import nz.ac.auckland.concert.service.domain.jpa.Mapper;

@Path("/")
public class ConcertResource {

	private EntityManager entityManager = PersistenceManager.instance().createEntityManager();

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
		if(performer.getImageName()==null) {
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

		return Response.created(URI.create("/concerts/" + user.getUsername())).status(201)
				.entity(Mapper.toUserDTO(user)).build();
	}

	@PUT
	@Path("users")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	public Response authenticateUser(UserDTO newUser) {

		if ( newUser.getPassword() == null || newUser.getUsername() == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		User u = entityManager.find(User.class, newUser.getUsername());
		if (u == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		if(!u.getPassword().equals(newUser.getPassword())) {
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}

		User user = new User(u.getUsername(), u.getPassword(),u.getLastname(),u.getFirstname());
		return Response.ok(Mapper.toUserDTO(user)).build();
	}

	/**
	 * Deletes all Concerts, returning a status code of 204.
	 * 
	 * When clientId is null, the HTTP request message doesn't contain a cookie
	 * named clientId (Config.CLIENT_COOKIE), this method generates a new cookie,
	 * whose value is a randomly generated UUID. This method returns the new cookie
	 * as part of the HTTP response message.
	 * 
	 * This method maps to the URI pattern <base-uri>/concerts.
	 * 
	 * 
	 * @return a Response object containing the status code 204.
	 */
	@DELETE
	public Response deleteAllConcerts() {

		entityManager.getTransaction().begin();

		TypedQuery<Concert> concertQuery = entityManager.createQuery("select c from Concert c", Concert.class);
		List<Concert> concerts = concertQuery.getResultList();

		concerts.stream().forEach(c -> {
			entityManager.remove(c);
		});

		entityManager.getTransaction().commit();
		ResponseBuilder builder = Response.status(204);
		return builder.build();
	}

	@DELETE
	@Path("{id}")
	public Response deleteConcert(@PathParam("id") long id) {
		entityManager.getTransaction().begin();
		Concert concert = entityManager.find(Concert.class, id);
		entityManager.remove(concert);
		entityManager.getTransaction().commit();
		ResponseBuilder builder = Response.status(204);
		return builder.build();
	}
}
