package aQute.xlaunchpad;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import aQute.launchpad.Launchpad;
import aQute.launchpad.LaunchpadBuilder;
import aQute.launchpad.Service;
import aQute.launchpad.junit.LaunchpadRunner;

@RunWith(LaunchpadRunner.class)
public class BeforeAfterTest {

	static final String		org_apache_felix_framework	= "org.apache.felix.framework;version='[6.0.5,7)'";
	static final String		org_apache_felix_scr		= "org.apache.felix.scr;version='[2.1.12,2.1.13)'";

	@SuppressWarnings("resource")
	static LaunchpadBuilder	builder						= new LaunchpadBuilder().runfw(org_apache_felix_framework)
		.bundles("assertj-core")
		.bundles("net.bytebuddy.byte-buddy")
		.bundles(org_apache_felix_scr)
		.debug();

	@Service
	Launchpad				framework;

	@Before
	public void before() {
		System.out.println("Before");
	}

	@After
	public void after() throws Exception {
		System.out.println("After");
	}

	@Test
	public void test() {
		assertNotNull(framework.getBundleContext());
	}

	@Test(expected = Exception.class)
	public void testExceptions() throws Exception {
		throw new Exception();
	}
}
