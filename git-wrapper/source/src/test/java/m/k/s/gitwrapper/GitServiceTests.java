package m.k.s.gitwrapper;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;

import junit.framework.TestCase;

public class GitServiceTests extends TestCase {

    public void testGit() throws Exception {
        String url = "https://bitbucket.org/thachln/mks.git";
        String username = "";
        String password = "";
        String checkoutLocation = "D:/Temp/TestGit";
        
        String branch = "my-services-sample-test";

        UsernamePasswordCredentialsProvider userCredential = new UsernamePasswordCredentialsProvider(username, password);
        Git r = Git.cloneRepository().setDirectory(new File(checkoutLocation + "/clonned"))
                .setCredentialsProvider(userCredential).setURI(url).setProgressMonitor(new TextProgressMonitor())
                .setBranch(branch).call();
        System.out.println("Clonning @ " + checkoutLocation);
        r.getRepository().close();
    }

    /**
     * Test clone 1 project.
     * @throws Exception
     */
    public void testClone() throws Exception {
        String url = "https://bitbucket.org/thuanle/ttcnpm-161-01";
        String username = "";
        String password = "";
        String checkoutLocation = "D:/Temp";
        
        GitService gitService = new GitService(url, username, password, checkoutLocation);

        gitService.cloneGit();

        System.out.println("Clonning @ " + checkoutLocation);

    }

    /**
     * Test clone many projects.
     * https://bitbucket.org/thuanle/ttcnpm-151-01
     * to
     * https://bitbucket.org/thuanle/ttcnpm-151-05
     * @throws Exception
     */
    @Test
    public void testCloneN() throws Exception {
    	final int N = 1;  // Maximum index of project
        String rootUrl = "https://bitbucket.org/thuanle/";
        String url = "https://bitbucket.org/thuanle/";
        String username = "";
        String password = "";
        String checkoutLocation = "D:/Temp/bitbucker-bk";
        
        GitService gitService = new GitService(url, username, password, rootUrl);

        
        
        // Format of URL: https://bitbucket.org/thuanle/ttcnpm-151-NN
        // Ex: https://bitbucket.org/thuanle/ttcnpm-151-16
        final String prefixUrl = "https://bitbucket.org/thuanle/ttcnpm-151-";
        final String prefixCheckoutLocation = checkoutLocation + "/ttcnpm-151-";
        String[] urls = new String[N];
        
        String[] checkoutLocations = new String[N];
        
        // Build value for urls and checkoutLocations
        String nn; 
        for (int i = 0; i < N; i++) {
            nn = String.format("%02d", i + 1);
            urls[i] = prefixUrl + nn + ".git";
            checkoutLocations[i] = prefixCheckoutLocation + nn;
        }
        
        // Perform to checkout
        gitService.cloneGit(urls, checkoutLocations);
    }
}
