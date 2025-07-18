---
layout: default
title: Bundle-ActivationPolicy ::= policy ( ';' directive )*
class: Header
summary: |
   The Bundle-ActivationPolicy specifies how the framework should activate the bundle once started.
note: AUTO-GENERATED FILE - DO NOT EDIT. You can add manual content via same filename in ext folder. 
---

- Example: `Bundle-ActivationPolicy: lazy`

- Values: `lazy`

- Pattern: `lazy`

<!-- Manual content from: ext/bundle_activationpolicy.md --><br /><br />

See [OSGi Specification](https://docs.osgi.org/specification/osgi.core/8.0.0/framework.lifecycle.html#i3270439) for a description of this header.

	public boolean verifyActivationPolicy(String policy) {
		Parameters map = parseHeader(policy);
		if (map.size() == 0)
			warning(Constants.BUNDLE_ACTIVATIONPOLICY + " is set but has no argument %s", policy);
		else if (map.size() > 1)
			warning(Constants.BUNDLE_ACTIVATIONPOLICY + " has too many arguments %s", policy);
		else {
			Map<String,String> s = map.get("lazy");
			if (s == null)
				warning(Constants.BUNDLE_ACTIVATIONPOLICY + " set but is not set to lazy: %s", policy);
			else
				return true;
		}

		return false;
	}
