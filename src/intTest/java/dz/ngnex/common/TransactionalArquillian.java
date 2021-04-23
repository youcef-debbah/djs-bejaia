package dz.ngnex.common;

import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.io.File;

public abstract class TransactionalArquillian extends Arquillian {

  public static final String PATH_SEPARATOR = System.getProperty("path.separator", ";");
  public static final String TEST_WAR_CLASSPATH_SYSTEM_PROPERTY = "intTest.testWar.classpath";

  @Inject
  protected UserTransaction userTransaction;

  @Inject
  protected EntityManager em;

//  @BeforeMethod
//  public void setup(ITestResult result) throws Exception {
//    System.out.println("running test method: " + result.getMethod().getQualifiedName());
//    if (userTransaction != null) {
//      userTransaction.begin();
//      em.joinTransaction();
//    }
//  }

//  @AfterMethod
//  public void tear(ITestResult result) throws Exception {
//    if (userTransaction != null)
//      if (result.isSuccess())
//        userTransaction.commit();
//      else
//        userTransaction.rollback();
//  }

  protected void commitTransaction() throws Exception {
    userTransaction.commit();
    userTransaction.begin();
    em.joinTransaction();
  }

  public static WebArchive createTestWar() {
    long t0 = System.nanoTime();
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
        .addAsLibraries(getAllLibrariesFiles())
        .addPackages(true, "dz/ngnex")
        .addPackages(true, "dz/jsoftware95")
        .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
        .addAsWebInfResource("test-beans.xml", "beans.xml")
        .addPackage(TransactionalArquillian.class.getPackage());

    System.out.println("archive built in: " + formatDuration(t0));
    System.out.println(webArchive.toString(false));
    return webArchive;
  }

  public static String formatDuration(long t0) {
    long t = System.nanoTime() - t0;
    return (t / 1_000_000) + " ms";
  }

  private static File[] getAllLibrariesFiles() {
    String classpath = System.getProperty(TEST_WAR_CLASSPATH_SYSTEM_PROPERTY);
    if (classpath != null) {
      String[] paths = classpath.split(PATH_SEPARATOR);

      File[] files = new File[paths.length];
      for (int i = 0; i < paths.length; i++) {
        files[i] = new File(paths[i]);
      }

      System.out.println("count of files got from gradle classpath: " + files.length);

      return files;
    } else
      return new File[0];
  }
}
