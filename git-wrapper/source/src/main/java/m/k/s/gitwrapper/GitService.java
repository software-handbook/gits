package m.k.s.gitwrapper;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * This service provides interfaces to: <br/>
 * - clone source code from git.
 */
@Component
public class GitService {
    /** For logging. */
    private final static Logger LOG = Logger.getLogger(GitService.class);
    
    /** URL of the project. Ex: https://bitbucket.org/thachln/mks.git. */
    private String url;

    /** Account name. */
    private String username;

    /** Account password. */
    private String password;

    /** Local folder to clone data from git. */
    private String checkoutLocation;

    /**
     * Create an instance of git service.
     * @param url project path
     * @param username account name
     * @param password account password
     * @param checkoutLocation local folder to contain project
     */
    public GitService(String url, String username, String password, String checkoutLocation) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.checkoutLocation = checkoutLocation;
    }

    /**
     * Clone or checkout.
     */
    public void cloneGit() {
        boolean cloneSubmodules = true;

        UsernamePasswordCredentialsProvider userCredential = new UsernamePasswordCredentialsProvider(username, password);
        Git git = null;
        File fileCheckoutLocation = new File(checkoutLocation);
        try {
        	

            git = Git.cloneRepository().setDirectory(fileCheckoutLocation)
                    .setCredentialsProvider(userCredential)
                    .setURI(url)
                    .setProgressMonitor(new TextProgressMonitor())
                    .setCloneSubmodules(cloneSubmodules)
                    .call();
            getInfo(git);
        } catch (InvalidRemoteException ex) {
            LOG.error("Could not clone project '" + url + "'", ex);
        } catch (TransportException ex) {
            LOG.error("Could not clone project '" + url + "'", ex);
        } catch (GitAPIException ex) {
            LOG.error("Could not clone project '" + url + "'", ex);
        } catch (JGitInternalException ex) {
        	LOG.warn("Could not clone project '" + url + "'. Will try to pull", ex);
        	// Close current connection
            // Close git connection
            pullGit(fileCheckoutLocation);
        } finally {
            // Close git connection
            if (git != null) {
                git.getRepository().close();
            }
        }
    }

	/**
	 * Analysis log of git.
	 * @param git
	 * @see http://wiki.eclipse.org/JGit/User_Guide#RevCommit
	 * <br/>
	 * Section: LogCommand (git-log)
	 */
	private void getInfo(Git git) {
	    try {
            Iterable<RevCommit> listCommit = git.log().call();
            
            Gson gson = new Gson();
            for (RevCommit revCommit : listCommit) {
                LOG.debug("Json log=" + gson.toJson(revCommit));
            }
        } catch (NoHeadException ex) {
            LOG.error("", ex);
        } catch (GitAPIException ex) {
            LOG.error("", ex);
        }
    }

    public boolean pullGit(File fileCheckoutLocation) {
		Git git = null;
		try {
			FileRepository localRepo = new FileRepository(fileCheckoutLocation);
			git = new Git(localRepo);
			PullCommand pullCmd = git.pull();
			pullCmd.call();
			return true;
		} catch (Exception ex) {
			LOG.error("Could not Pull for project '" + fileCheckoutLocation.getPath() + "'");
			return false;
		} finally {
			if (git != null) {
				git.getRepository().close();
			}
		}
	}
    
    /**
     * Checkout many URLS into given local paths.
     * @param urls array of URLs
     * @param checkoutLocations array of local path.
     * <br/>
     * URL of urls[i] will be checkout to checkoutLocations[i].
     * Assumption size of urls and checkoutLocations are same.
     */
    public void cloneGit(String[] urls, String[] checkoutLocations) {
        boolean cloneSubmodules = true;

        UsernamePasswordCredentialsProvider userCredential = new UsernamePasswordCredentialsProvider(username, password);
        Git git = null;
        try {
            CloneCommand cloneCmd = Git.cloneRepository()
                    .setURI(url)
                    .setCredentialsProvider(userCredential)
                    .setProgressMonitor(new TextProgressMonitor());
            
            // Scan each url, checkoutLocation
            // Perform clone
            int i = 0;
            File fileCheckoutLocation;
            for (String url : urls) {
            	
            	fileCheckoutLocation = new File (checkoutLocations[i]);
            	
//            	if (fileCheckoutLocation.exists()) {
//            		LOG.debug("Pull code from '" + url + "' to '" + checkoutLocations[i] + "'");
//            		// Run Pull command
//            	} else {
            		LOG.debug("Clone code from '" + url + "' to '" + checkoutLocations[i] + "'");
            		// Run Clone command
	            	cloneCmd.setCloneSubmodules(cloneSubmodules);
	                cloneCmd.setURI(url);
	                cloneCmd.setDirectory(fileCheckoutLocation);
	                cloneCmd.call();
//            	}
                i++;
            }
        } catch (InvalidRemoteException ex) {
            LOG.error("Could not clone project '" + url + "'", ex);
        } catch (TransportException ex) {
            LOG.error("Could not clone project '" + url + "'", ex);
        } catch (GitAPIException ex) {
            LOG.error("Could not clone project '" + url + "'", ex);
        } finally {
            // Close git connection
            if (git != null) {
                // git.getRepository().close();
            }
        }
    }
}
