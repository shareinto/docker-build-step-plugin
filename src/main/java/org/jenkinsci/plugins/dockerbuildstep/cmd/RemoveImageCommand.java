package org.jenkinsci.plugins.dockerbuildstep.cmd;

import hudson.Extension;
import hudson.model.AbstractBuild;

import org.jenkinsci.plugins.dockerbuildstep.log.ConsoleLogger;
import org.jenkinsci.plugins.dockerbuildstep.util.Resolver;
import org.kohsuke.stapler.DataBoundConstructor;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.DockerException;
import com.github.dockerjava.api.NotFoundException;

/**
 * This command removes specified Docker image.
 * 
 * @see http 
 *      ://docs.docker.com/reference/api/docker_remote_api_v1.13/#remove-an-image
 * 
 * @author draoullig
 * 
 */
public class RemoveImageCommand extends DockerCommand {

	private final String dockerUrl;
	private final String dockerVersion;
	private final String imageName;
	private final String imageId;
	private final boolean ignoreIfNotFound;

	@DataBoundConstructor
	public RemoveImageCommand(String dockerUrl, String dockerVersion,final String imageName, final String imageId,
			final boolean ignoreIfNotFound) {
		this.dockerUrl = dockerUrl;
		this.dockerVersion = dockerVersion;
		this.imageName = imageName;
		this.imageId = imageId;
		this.ignoreIfNotFound = ignoreIfNotFound;
	}

	public String getDockerVersion() {
		return dockerVersion;
	}

	public String getDockerUrl() {
		return dockerUrl;
	}

	public String getImageName() {
		return imageName;
	}

	public String getImageId() {
		return imageId;
	}

	public boolean getIgnoreIfNotFound() {
		return ignoreIfNotFound;
	}

	@Override
	public void execute(@SuppressWarnings("rawtypes") AbstractBuild build,
			ConsoleLogger console) throws DockerException {
		// TODO check it when submitting the form
		if (imageName == null || imageName.isEmpty()) {
			throw new IllegalArgumentException(
					"At least one parameter is required");
		}
		
		final String imageNameRes = Resolver.buildVar(build, imageName);
		final String imageIdRes = Resolver.buildVar(build, imageId);
		
		DockerClient client = getClient(build, null,dockerUrl,dockerVersion);
		try {
			if (imageIdRes == null || imageIdRes.isEmpty()) {
				client.removeImageCmd(imageNameRes).exec();
				console.logInfo("Removed image " + imageNameRes);
			} else {
				client.removeImageCmd(imageNameRes).withImageId(imageIdRes);
				console.logInfo("Removed image " + imageNameRes + " with id "
						+ imageIdRes);
			}

		} catch (NotFoundException e) {
			if (!ignoreIfNotFound) {
				console.logError(String.format("image '%s' not found ",
						imageNameRes + " with id " + imageIdRes));
				throw e;
			} else {
				console.logInfo(String
						.format("image '%s' not found, but skipping this error is turned on, let's continue ... ",
								imageNameRes + " with id " + imageIdRes));
			}
		}

	}
	
	@Extension
	public static class RemoveImageCommandDescriptor extends
			DockerCommandDescriptor {
		@Override
		public String getDisplayName() {
			return "Remove image";
		}
	}

}
