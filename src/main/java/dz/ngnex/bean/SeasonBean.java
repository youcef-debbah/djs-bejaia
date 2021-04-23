package dz.ngnex.bean;

import dz.ngnex.entity.SeasonEntity;
import dz.ngnex.entity.SeasonStats;

import javax.ejb.Local;
import java.util.List;

@Local
public interface SeasonBean {

  SeasonStats getSeasonStats(Integer id);

  List<SeasonEntity> getAllSeasons();

  SeasonEntity addSeason(String name) throws IntegrityException;

  void deleteSeason(Integer id);

  void updateCurrentSeason(Integer newSeasonID);

  void clear();
}
