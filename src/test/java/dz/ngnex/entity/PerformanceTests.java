package dz.ngnex.entity;

import dz.ngnex.bean.SeasonBean;
import dz.ngnex.testkit.DatabaseTest;
import org.junit.Test;

import javax.ejb.EJB;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PerformanceTests extends DatabaseTest {

  @EJB
  private SeasonBean seasonBean;

  @Test
  public void test() {
    List<SeasonEntity> seasons = em.createQuery("select s from SeasonEntity s", SeasonEntity.class)
        .getResultList();

    for (SeasonEntity season : seasons) {
      seasonBean.clear();
      long t0 = System.nanoTime();
      seasonBean.deleteSeason(season.getId());
      long t = System.nanoTime() - t0;
      System.out.println("### season: " + season.getName() + " deleted in: " + TimeUnit.NANOSECONDS.toMillis(t) + " ms");
    }
  }
}
