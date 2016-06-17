package org.jenkinsci.plugins.dockerbuildstep.cmd;

import hudson.Extension;
import hudson.model.AbstractBuild;

import java.util.List;

import org.jenkinsci.plugins.dockerbuildstep.log.ConsoleLogger;
import org.kohsuke.stapler.DataBoundConstructor;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.DockerException;
import com.github.dockerjava.api.model.Container;

/**
 * This command removes all Docker containers. Before removing them, it kills all them in case some of them are running.
 * 
 * @author vjuranek
 * 
 */
public class RemoveAllCommand extends DockerCommand {

    private final String dockerUrl;
    private final String dockerVersion;
    private final boolean removeVolumes;
    private final boolean force;

    @DataBoundConstructor
    public RemoveAllCommand(String dockerUrl, String dockerVersion,boolean removeVolumes, boolean force) {
        this.dockerUrl = dockerUrl;
        this.dockerVersion = dockerVersion;
        this.removeVolumes = removeVolumes;
        this.force = force;
    }

    public String getDockerVersion() {
        return dockerVersion;
    }

    public String getDockerUrl() {
        return dockerUrl;
    }

    public boolean isRemoveVolumes() {
        return removeVolumes;
    }

    public boolean isForce() {
        return force;
    }

	@Override
    public void execute(@SuppressWarnings("rawtypes") AbstractBuild build, ConsoleLogger console)
            throws DockerException {
        DockerClient client = getClient(build, null,dockerUrl,dockerVersion);
        List<Container> containers = client.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            client.removeContainerCmd((container.getId())).withForce(force).withRemoveVolumes(removeVolumes).exec();
            console.logInfo("removed container id " + container.getId());
        }
    }

    @Extension
    public static class RemoveAllCommandDescriptor extends DockerCommandDescriptor {
        @Override
        public String getDisplayName() {
            return "Remove all containers";
        }
    }

}
