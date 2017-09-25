package nz.ac.auckland.concert.service.domain.jpa;

import java.util.HashSet;
import java.util.Set;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.User;

public class Mapper {
	public static Set<ConcertDTO> toConcertDTO (Set<Concert> concerts) {
		Set<ConcertDTO> concertDTOs=new HashSet<ConcertDTO>();
		for(Concert concert:concerts) {
			ConcertDTO concertDTO = new ConcertDTO(concert.getId(),concert.getTitle(),concert.getDates(),concert.getTariff(),concert.getPerformerIds());
			concertDTOs.add(concertDTO);
		}
		return concertDTOs;
	}
	
	public static Set<PerformerDTO> toPerformerDTO (Set<Performer> performers) {
		Set<PerformerDTO> performerDTOs=new HashSet<PerformerDTO>();
		for(Performer performer:performers) {
			PerformerDTO performerDTO = new PerformerDTO(performer.getId(),performer.getName(),performer.getImageName(),performer.getGenre(),performer.getConcertIds());
			performerDTOs.add(performerDTO);
		}
		return performerDTOs;
	}
	
	public static UserDTO toUserDTO(User user) {
		return new UserDTO(user.getUsername(),user.getPassword(),user.getLastname(),user.getFirstname());
	}
}
