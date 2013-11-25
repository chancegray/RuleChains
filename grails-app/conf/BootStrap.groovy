import grails.util.GrailsNameUtils
import grails.util.GrailsUtil
import edu.usf.RuleChains.Chain
import edu.usf.RuleChains.JobService
import edu.usf.RuleChains.JobController
import edu.usf.RuleChains.LinkService
import edu.usf.RuleChains.SQLQuery
import edu.usf.RuleChains.RuleSet
import edu.usf.RuleChains.Link
import edu.usf.RuleChains.LinkMeta
import edu.usf.RuleChains.JobMeta
import edu.usf.RuleChains.Groovy
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.InitCommand
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.Repository

class BootStrap {
    def grailsApplication
    def quartzScheduler
    def jobService
    def springSecurityService
    def linkMeta = new LinkMeta()
    def jobMeta = new JobMeta()
    def gitRepository = null
    def init = { servletContext ->
        if(!!!quartzScheduler) {
            print "Didn't get set!"
        } else {
            // Building the Meta Programing
            linkMeta.buildMeta(grailsApplication)
            jobMeta.buildMeta(quartzScheduler)
            print jobService.listChainJobs()
        }
        def baseFolder = grailsApplication.getMainContext().getResource("/").getFile().toString()
        def command = Git.init()
        command.directory = new File(baseFolder + '/git/')

        def repository
        
        try {
            repository = command.call().repository
            println "Initialised empty git repository for the project."
        }
        catch (Exception ex) {
            println "Unable to initialise git repository - ${ex.message}"
            exit 1
        }
    
        // Now commit the files that aren't ignored to the repository.
        def git = new Git(repository)
        git.add().addFilepattern(".").call()
        git.commit().setMessage("Initial commit of RuleChains code sources.").call()
        println "Committed initial code to the git repository."

        gitRepository = repository

//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
//        Repository repository = builder.setGitDir(new File("/my/git/directory"))
//            .readEnvironment() // scan environment GIT_* variables
//            .findGitDir() // scan up the file system tree
//            .build();
//        Git git = new Git(myRepo);
//        git.commit().setMessage("Fix393").setAuthor(developerIdent).call();
        switch(GrailsUtil.environment){
            case "development":
                println "#### Development Mode (Start Up)"
                break
            case "test":
                println "#### Test Mode (Start Up)"
                break
            case "production":
                println "#### Production Mode (Start Up)"
                break
        }        
    }
    def destroy = {
        switch(GrailsUtil.environment){
            case "development":
                println "#### Development Mode (Shut Down)"
                break
            case "test":
                println "#### Test Mode (Shut Down)"
                break
            case "production":
                println "#### Production Mode (Shut Down)"
                break
        }        
    }
}
