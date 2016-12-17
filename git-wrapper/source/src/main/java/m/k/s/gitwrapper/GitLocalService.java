/**
 * Licensed to Open-Ones Group under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Open-Ones Group licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package m.k.s.gitwrapper;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.google.gson.Gson;

/**
 * @author lengocthach
 * @see http://wiki.eclipse.org/JGit/User_Guide#RevCommit
 *
 */
public class GitLocalService {
    /** Logging. */
    final private static Logger LOG = Logger.getLogger(GitLocalService.class);
    
    private Repository repository;
    private Git git;
    private IOutput outputter;
    /**
     * Open the git repository.
     * <br/>
     * Warning: close the git after used
     * @param repoPath full path of the repository
     * 
     */
    public GitLocalService(String repoPath, IOutput outputter) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        
        try {
            repository = builder.setGitDir(new File(repoPath))
                    .setMustExist(true)
//                    .readEnvironment() // scan environment GIT_* variables
//                    .findGitDir() // scan up the file system tree
                    .build();
            
            git = new Git(repository);
            this.outputter = outputter;
        } catch (IOException ex) {
            LOG.error("Could not open git repository '" + repoPath + "'", ex);
        }
    }

    public void getInfo() throws IOException {
        try {
            Iterable<RevCommit> listCommit = git.log().call();
            
            RevCommit revCommit;
            for (Iterator<RevCommit> iterator = listCommit.iterator(); iterator.hasNext();) {
                revCommit = iterator.next();
                getInfo(revCommit);
            }
        } catch (NoHeadException ex) {
            LOG.error("", ex);
        } catch (GitAPIException ex) {
            LOG.error("", ex);
        }
    }

    private void getInfo(RevCommit revCommit) {
        String commitName;
        Date commitDate;
        String filePath;
        String authorEmail;
        
        String authorName;
        String message;
        
        
        Gson gson = new Gson();
//        LOG.debug("author=" + gson.toJson(author));
        LOG.debug("---Log Commit---");
        PersonIdent author = revCommit.getAuthorIdent();

        commitName = revCommit.getName();
        LOG.debug("revCommit.getName() = " + commitName);
        
        commitDate = author.getWhen();
        authorEmail = author.getEmailAddress();
        authorName = author.getName();

        LOG.debug("author.getWhen()=" + author.getWhen());
        LOG.debug("author.toExternalString()=" + author.toExternalString());
        
        LOG.debug("authorName=" + authorName);
        
        LOG.debug("author.getTimeZone()=" + author.getTimeZone());
        LOG.debug("revCommit.getParentCount()=" + revCommit.getParentCount());
        
        LOG.debug("revCommit.getShortMessage() = " + revCommit.getShortMessage());
        message = revCommit.getFullMessage();
        
        
        LOG.debug("message = " + message);
        
        LOG.debug("-------------------");
        
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        
        if (revCommit.getParentCount() > 0) {
            for (RevCommit parentCommit : revCommit.getParents()) {
            
                try {
                    List<DiffEntry> diffs = df.scan(parentCommit.getTree(), revCommit.getTree());
                    for (DiffEntry diff : diffs) {
                        LOG.debug(MessageFormat.format("({0} {1} {2}", diff.getChangeType().name(), diff.getNewMode().getBits(), diff.getNewPath()));
                        filePath = diff.getNewPath();
                        outputter.write(commitName, commitDate, authorName, authorEmail, filePath, message);
                    }
                } catch (IOException ex) {
                    LOG.error("Could not get comitted files of commit '" + commitName + "'", ex);
                }
            }
        } else {
            TreeWalk treeWalk = new TreeWalk(repository);
            RevTree tree = revCommit.getTree();
            
            try {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(TreeFilter.ANY_DIFF);
                
                while (treeWalk.next()) {
                    LOG.debug("treeWalk.getPathString()=" + treeWalk.getPathString());
                    filePath = treeWalk.getPathString();
                    outputter.write(commitName, commitDate, authorName, authorEmail, filePath, message);
                }
            } catch (Exception ex) {
                LOG.error("Could not get comitted files of commit '" + commitName + "'", ex);
            } finally {
                treeWalk.close();
            }
            
        }
        
    }
}
