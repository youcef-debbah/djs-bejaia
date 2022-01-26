package dz.ngnex.entity;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import dz.ngnex.bean.IntegrityException;
import dz.ngnex.testkit.DatabaseTest;
import dz.ngnex.testkit.TestConfig;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.List;

/**
 * although I'm not using the JMH tool to do this benchmark it should be fairly
 * precise, first, I will do enough warmups runs to let the JIT optimize the
 * calls to the maximum, also before every call the GC will run, a volatile var
 * is used to prevent the calls from being totally removed by the JIT and small
 * paused are implicitly included to give the JIT and GC time to work before
 * the actual benchmark calls, the  only problem is the I'm using a single
 * JVM ideally I would do the benchmark from many JVM forks then combine the
 * result but it doesn't really have that impact (did it before and didn't saw
 * a significant changes so this time I didn't bother myself with it!)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "out/test/benchmark.ToManyFetchingTest")
@BenchmarkOptions(benchmarkRounds = TestConfig.BENCHMARKS, warmupRounds = TestConfig.WARMUPS)
public class ToManyFetchingTest extends DatabaseTest {

    private static volatile int blackHole;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    @EJB
    private SandboxBean sandboxBean;

    // @Test
    // @BenchmarkOptions(benchmarkRounds = 12, warmupRounds = 0)
    public void _00_addPost1WithTwoImages() throws IntegrityException {
        // in real code I get more info from the javascript client
        // but these parameters here are just for test (no image content)
        Picture1InfoEntity firstPic = sandboxBean.uploadPicture1("image/jpeg", "first-photo.jpg", "joseph");
        Picture1InfoEntity secondPic = sandboxBean.uploadPicture1("image/jpeg", "second-photo.jpg", "joseph");

        Post1Entity post = new Post1Entity();
        post.setAuthor("joseph");
        post.setTitle_en("english content");
        post.setTitle_fr("french content");
        post.setContent_en("english content");
        post.setContent_fr("french content");
        post.setThumbnail("/image/thumbnails/test.png");
        sandboxBean.addPost1(post, Arrays.asList(firstPic, secondPic));
    }

     @Test
     @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    public void _01_addPost2WithTwoImages() throws IntegrityException {
        // same as the first but the second entity
        Picture2InfoEntity firstPic = sandboxBean.uploadPicture2("image/jpeg", "first-photo.jpg", "joseph");
        Picture2InfoEntity secondPic = sandboxBean.uploadPicture2("image/jpeg", "second-photo.jpg", "joseph");

        Post2Entity post = new Post2Entity();
        post.setAuthor("joseph");
        post.setTitle_en("english content");
        post.setTitle_fr("french content");
        post.setContent_en("english content");
        post.setContent_fr("french content");
        post.setThumbnail("/image/thumbnails/test.png");
        sandboxBean.addPost2(post, Arrays.asList(firstPic, secondPic));
    }

//    @Test
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 100)
    public void _10_noop() throws InterruptedException {
        sandboxBean.noop();
    }

//    @Test
    public void _11_selectPost1() throws InterruptedException {
        List<Post1Entity> posts = sandboxBean.selectPost1();

        // close hibernate session any association that is not loaded yet will trow an exception when accessed
        // only uncomment to test that the above method is actually loading the data, must not be included in the actual benchmark test
//         sandboxBean.clear();

        // accessing the images associations
        // only uncomment to test that the above method is actually loading the data, must not be included in the actual benchmark test
//        int imagesCount = 0;
//        for (Post1Entity post : posts)
//            imagesCount += post.getImages().size();

        // preventing the JIT from removing the contents of this method
        blackHole = posts.size();
    }

    @Test
    public void _12_selectPost2() throws InterruptedException {
        System.out.println("sandboxBean.countPost2() = " + sandboxBean.countPost2());
        List<Post2Entity> posts = sandboxBean.selectPost2();

        // close hibernate session any association that is not loaded yet will trow an exception when accessed
        // only uncomment to test that the above method is actually loading the data, must not be included in the actual benchmark test
//        sandboxBean.clear();

        // accessing the images associations
        // only uncomment to test that the above method is actually loading the data, must not be included in the actual benchmark test
//        int imagesCount = 0;
//        for (Post2Entity post : posts)
//            imagesCount += post.getImages().size();

        // preventing the JIT from removing the contents of this method
        blackHole = posts.size();
    }
}
