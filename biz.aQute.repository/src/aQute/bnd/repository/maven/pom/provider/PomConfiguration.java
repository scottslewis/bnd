package aQute.bnd.repository.maven.pom.provider;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface PomConfiguration {

	/**
	 * The url to the remote release repository. Can be a comma separated list
	 * of urls.
	 *
	 * @deprecated use releaseUrl instead
	 */
	@Deprecated
	String releaseUrls();

	/**
	 * The url to the remote release repository. Can be a comma separated list
	 * of urls.
	 */
	String releaseUrl();

	/**
	 * The url to the remote snapshot repository. If this is not specified, it
	 * falls back to the release repository or just local if this is also not
	 * specified. Can be a comma separated list of urls.
	 *
	 * @deprecated use snapshotUrl instead
	 */
	@Deprecated
	String snapshotUrls();

	/**
	 * The url to the remote snapshot repository. If this is not specified, it
	 * falls back to the release repository or just local if this is also not
	 * specified. Can be a comma separated list of urls.
	 */
	String snapshotUrl();

	/**
	 * The path to the local repository
	 */
	// default "~/.m2/repository"
	String local(String deflt);

	/**
	 * Coordinates of a maven revision. I.e. g:a[:c]:v. Can be a comma separated
	 * list of gavs.
	 */
	String revision();

	/**
	 * Points to a file that is used as the cache. It will be in OSGi format.
	 */
	String location(String deflt);

	/**
	 * Points to a file that holds a pom. This is exclusive with revision. Can
	 * be a comma separated list of files.
	 */
	String pom();

	/**
	 * The name of the repo. Required.
	 */
	String name();

	/**
	 * The query used to search Maven Central Search.
	 */
	String query();

	/**
	 * The url of the Maven Central Search.
	 */
	// default "http://search.maven.org/solrsearch/select"
	String queryUrl(String deflf);

	/**
	 * Allow transitive dependencies
	 */
	boolean transitive(boolean deflt);

	/**
	 * Sets the time in seconds when to check for changes in the pom-files
	 */
	// default: 5 Minutes
	int poll_time(int pollTimeInSecs);

	/**
	 * Also considers the dependency management section of a pom. Default is
	 * false.
	 */
	boolean dependencyManagement(boolean deflt);

	/**
	 * @return a comma separated list of tags.
	 */
	String tags();

}
